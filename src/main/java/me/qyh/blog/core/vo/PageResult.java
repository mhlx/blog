/*
 * Copyright 2016 qyh.me
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.qyh.blog.core.vo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

public class PageResult<T> {

	private List<T> datas = new ArrayList<>();
	private int totalPage;// 总页码
	private int offset;
	private int currentPage;// 当前页
	private int liststep = 10;
	private int listbegin;
	private int listend;
	private int pageSize;// 每页显示数量
	private int totalRow;// 总纪录数
	private PageQueryParam param;

	public PageResult() {

	}

	public PageResult(PageQueryParam param, int totalRow, List<T> datas) {
		this.pageSize = param.getPageSize();
		this.offset = param.getOffset();
		this.currentPage = offset / pageSize + 1;
		this.totalRow = totalRow;
		this.totalPage = totalRow % pageSize == 0 ? totalRow / pageSize : totalRow / pageSize + 1;
		countListbeginAndListend();
		this.datas = datas;
		this.param = param;
	}

	private void countListbeginAndListend() {
		int listbegin = (currentPage - (int) Math.ceil((double) liststep / 2));
		listbegin = listbegin < 1 ? 1 : listbegin;
		int listend = currentPage + liststep / 2;
		if (listend > totalPage) {
			listend = totalPage + 1;
		}
		int cha = listend - listbegin + 1 - liststep;
		if (cha <= 0) {
			if (currentPage + liststep / 2 > totalPage) {
				listbegin = listbegin + cha - 1;
				if (listbegin <= 0) {
					listbegin = 1;
				}
			} else {
				listend = listend - cha + 1;
				if (listend > totalPage) {
					listend = totalPage + 1;
				}
			}
		}
		this.listbegin = listbegin;
		this.listend = listend;
	}

	public List<T> getDatas() {
		return datas;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getOffset() {
		return offset;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public int getListstep() {
		return liststep;
	}

	public int getListbegin() {
		return listbegin;
	}

	public int getListend() {
		return listend;
	}

	public int getPageSize() {
		return pageSize;
	}

	public int getTotalRow() {
		return totalRow;
	}

	public void setDatas(List<T> datas) {
		this.datas = datas;
	}

	public boolean hasResult() {
		return !CollectionUtils.isEmpty(datas);
	}

	public PageQueryParam getParam() {
		return param;
	}

	public void setParam(PageQueryParam param) {
		this.param = param;
	}

	public void setListstep(int liststep) {
		this.liststep = liststep;
		countListbeginAndListend();
	}

	/**
	 * @since 7.0
	 * @param param
	 * @return
	 */
	public static <T> PageResult<T> empty(PageQueryParam param) {
		return new PageResult<>(param, 0, new ArrayList<>());
	}

	/**
	 * @since 7.0
	 * @return
	 */
	public boolean hasNext() {
		return totalPage > 1 && totalPage > currentPage;
	}

	/**
	 * @since 7.0
	 * @return
	 */
	public boolean hasPrevious() {
		return currentPage <= totalPage && currentPage > 1;
	}

}
