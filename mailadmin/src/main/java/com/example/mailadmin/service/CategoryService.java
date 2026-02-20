package com.example.mailadmin.service;

import com.example.mailadmin.dto.AddCategoryDTO;
import com.example.mailadmin.dto.EditCategoryDTO;
import com.example.mailadmin.entity.Category;

import java.util.List;

/**
 * 分类Service接口
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
public interface CategoryService {
    /**
     * 查询所有分类
     *
     * @return 分类列表
     */
    List<Category> selectAll();

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    Category selectById(Long id);

    /**
     * 新增分类
     *
     * @param addCategoryDTO 分类信息
     */
    void insertCategory(AddCategoryDTO addCategoryDTO);

    /**
     * 修改分类
     *
     * @param editCategoryDTO 分类信息
     */
    void updateCategory(EditCategoryDTO editCategoryDTO);

    /**
     * 删除分类
     *
     * @param ids 分类ID数组
     */
    void deleteByIds(Long[] ids);
}
