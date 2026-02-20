package com.example.mailadmin.dto;

import lombok.Data;

/**
 * 添加分类DTO
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Data
public class AddCategoryDTO {
    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序权重
     */
    private Integer sort;
}
