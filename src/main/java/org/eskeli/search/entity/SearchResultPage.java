package org.eskeli.search.entity;

/**
 * Class Desc : 结果分页
 *
 * @author elisontl
 */
public class SearchResultPage {

    // 当前页
    private Integer pageNum = 1;
    // 每页记录数(默认10)
    private Integer pageSize = 10;
    // 总记录数
    private long totalCount;
    // 总页数
    private Integer pageCount;
    // 索引开始位置
    private Integer start;

    /* 起始索引位置，可调用此方法，也可以自行在逻辑中计算 */
    public Integer getStart() {
        if (null != pageNum) {
            start = (pageNum - 1) * pageSize;
        } else {
            start = 0;
        }
        return start;
    }

    // 总页数
    public Integer getPageCount() {
        if (totalCount % pageSize != 0) {
            pageCount = (int) (totalCount / pageSize + 1);
        } else {
            pageCount = (int) (totalCount / pageSize);
        }
        return pageCount;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public SearchResultPage(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public SearchResultPage() {

    }

    /**
     * 有参构造，传递 pageNum 及 pageSize 为参数
     *
     * @param pageNum
     * @param pageSize
     */
    public SearchResultPage(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

}
