package com.example.mailadmin.service.impl;

import com.example.mailadmin.dto.AddCategoryDTO;
import com.example.mailadmin.dto.EditCategoryDTO;
import lombok.extern.slf4j.Slf4j;
import com.example.mailadmin.entity.Category;
import com.example.mailadmin.mapper.CategoryMapper;
import com.example.mailadmin.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类Service实现类
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String CATEGORIES_CACHE_KEY = "categories:all";

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
        // 清除分类列表缓存
        clearCategoriesCache();
    }

    @Override
    public void updateCategory(EditCategoryDTO editCategoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(editCategoryDTO, category);
        categoryMapper.update(category);
        // 清除分类列表缓存
        clearCategoriesCache();
    }

    @Override
    public void deleteByIds(Long[] ids) {
        categoryMapper.deleteByIds(ids);
        // 清除分类列表缓存
        clearCategoriesCache();
    }

    /**
     * 清除分类列表缓存
     */
    private void clearCategoriesCache() {
        try {
            redisTemplate.delete(CATEGORIES_CACHE_KEY);
            log.info("分类列表缓存已清除");
        } catch (Exception e) {
            // 缓存清除失败不影响主业务流程
            log.warn("清除分类列表缓存失败，不影响分类操作: {}", e.getMessage());
        }
    }
}
