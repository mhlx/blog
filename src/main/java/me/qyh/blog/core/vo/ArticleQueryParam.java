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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import me.qyh.blog.core.entity.Article.ArticleFrom;
import me.qyh.blog.core.entity.Article.ArticleStatus;
import me.qyh.blog.core.entity.Space;
import me.qyh.blog.core.util.Validators;

/**
 * 文章分页查询参数
 * 
 * @author Administrator
 *
 */
public class ArticleQueryParam extends PageQueryParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Space space;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date begin;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date end;
	private String query;
	private ArticleStatus status;
	private ArticleFrom from;
	private boolean ignoreLevel;// 忽略置顶
	private boolean queryPrivate;// 查询私人博客
	private String tag;
	// 是否查询带锁文章
	private boolean queryLock = true;
	private Sort sort;
	private boolean highlight = true;// 查询是否高亮显示

	private boolean ignorePaging = false;

	/**
	 * 根据<b>别名</b>，只查询某些空间
	 * 
	 * @since 5.5.4
	 */
	private Set<String> spaces = new HashSet<>();
	private Set<Integer> spaceIds = new HashSet<>();

	public enum Sort {
		HITS, PUBDATE, LASTMODIFYDATE
	}

	/**
	 * @since 5.5.5
	 */
	private Integer tagId;

	public ArticleQueryParam() {
		super();
	}

	// clone
	public ArticleQueryParam(ArticleQueryParam param) {
		super(param);
		this.begin = param.begin;
		this.end = param.end;
		this.from = param.from;
		this.highlight = param.highlight;
		this.ignoreLevel = param.ignoreLevel;
		this.query = param.query;
		this.queryLock = param.queryLock;
		this.queryPrivate = param.queryPrivate;
		this.sort = param.sort;
		this.space = param.space;
		this.status = param.status;
		this.tag = param.tag;
		this.ignorePaging = param.ignorePaging;
		this.spaces = param.spaces;
		this.spaceIds = param.spaceIds;
		this.tagId = param.tagId;
	}

	public Space getSpace() {
		return space;
	}

	public void setSpace(Space space) {
		this.space = space;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public ArticleStatus getStatus() {
		return status;
	}

	public void setStatus(ArticleStatus status) {
		this.status = status;
	}

	public boolean isIgnoreLevel() {
		return ignoreLevel;
	}

	public void setIgnoreLevel(boolean ignoreLevel) {
		this.ignoreLevel = ignoreLevel;
	}

	public ArticleFrom getFrom() {
		return from;
	}

	public void setFrom(ArticleFrom from) {
		this.from = from;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public boolean isQueryPrivate() {
		return queryPrivate;
	}

	public void setQueryPrivate(boolean queryPrivate) {
		this.queryPrivate = queryPrivate;
	}

	public boolean isQueryLock() {
		return queryLock;
	}

	public void setQueryLock(boolean queryLock) {
		this.queryLock = queryLock;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	public boolean hasQuery() {
		return !Validators.isEmptyOrNull(query, true);
	}

	public boolean isHighlight() {
		return highlight;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public Set<String> getSpaces() {
		return spaces;
	}

	public void setSpaces(Set<String> spaces) {
		this.spaces = spaces;
	}

	public Set<Integer> getSpaceIds() {
		return spaceIds;
	}

	public void setSpaceIds(Set<Integer> spaceIds) {
		this.spaceIds = spaceIds;
	}

	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public boolean isIgnorePaging() {
		return ignorePaging;
	}

	public void setIgnorePaging(boolean ignorePaging) {
		this.ignorePaging = ignorePaging;
	}

	@Override
	public String toString() {
		return "ArticleQueryParam [space=" + space + ", begin=" + begin + ", end=" + end + ", query=" + query
				+ ", status=" + status + ", from=" + from + ", ignoreLevel=" + ignoreLevel + ", queryPrivate="
				+ queryPrivate + ", tag=" + tag + ", queryLock=" + queryLock + ", sort=" + sort + ", highlight="
				+ highlight + "]";
	}

}
