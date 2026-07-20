package shm.commerce.interactionapi.dto;

import java.util.List;

public class PageableObject {
    private Long offset;
    private List<SortObject> sort;
    private Boolean unpaged;
    private Boolean paged;
    private Integer pageNumber;
    private Integer pageSize;

    public PageableObject() {
    }

    public PageableObject(Long offset, List<SortObject> sort, Boolean unpaged,
                          Boolean paged, Integer pageNumber, Integer pageSize) {
        this.offset = offset;
        this.sort = sort;
        this.unpaged = unpaged;
        this.paged = paged;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public List<SortObject> getSort() {
        return sort;
    }

    public void setSort(List<SortObject> sort) {
        this.sort = sort;
    }

    public Boolean getUnpaged() {
        return unpaged;
    }

    public void setUnpaged(Boolean unpaged) {
        this.unpaged = unpaged;
    }

    public Boolean getPaged() {
        return paged;
    }

    public void setPaged(Boolean paged) {
        this.paged = paged;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
