package com.campus.delivery.common.api;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 统一分页响应体。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private Long total;
    /** 当前页码（从 1 开始） */
    private Long pageNum;
    /** 每页条数 */
    private Long pageSize;
    /** 当前页数据 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(Long total, Long pageNum, Long pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list == null ? Collections.emptyList() : list;
    }

    public static <T> PageResult<T> of(Long total, Long pageNum, Long pageSize, List<T> list) {
        return new PageResult<>(total, pageNum, pageSize, list);
    }

    public static <T> PageResult<T> empty(Long pageNum, Long pageSize) {
        return new PageResult<>(0L, pageNum, pageSize, Collections.emptyList());
    }
}
