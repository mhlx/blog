package me.qyh.blog.vo;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PageResult<T> {

    private List<T> datas = new ArrayList<>();
    private int totalPage;// 总页码
    private int offset;
    private int currentPage;// 当前页
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
        this.datas = datas;
        this.param = param;
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

    public boolean isHasNext() {
        return totalPage > 1 && totalPage > currentPage;
    }

    public boolean isHasPrevious() {
        return currentPage <= totalPage && currentPage > 1;
    }

    public boolean isHasPaging() {
        return totalPage > 1;
    }
}
