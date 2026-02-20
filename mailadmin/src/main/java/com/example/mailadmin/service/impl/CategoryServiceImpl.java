package com.example.mailadmin.service.impl;

import com.example.mailadmin.dto.AddCategoryDTO;
import com.example.mailadmin.dto.EditCategoryDTO;
import com.example.mailadmin.entity.Category;
import com.example.mailadmin.mapper.CategoryMapper;
import com.example.mailadmin.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类Service实现类
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> selectAll() {
        return categoryMapper.selectAll();
    }

    @Override
    public Category selectById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void insertCategory(AddCategoryDTO addCategoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(addCategoryDTO, category);
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(EditCategoryDTO editCategoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(editCategoryDTO, category);
        categoryMapper.update(category);
    }

    @Override
    public void deleteByIds(Long[] ids) {
        categoryMapper.deleteByIds(ids);
    }
}
