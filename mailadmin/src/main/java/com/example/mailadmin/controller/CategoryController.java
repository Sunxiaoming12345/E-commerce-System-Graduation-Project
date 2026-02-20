package com.example.mailadmin.controller;

import com.example.mailadmin.dto.AddCategoryDTO;
import com.example.mailadmin.dto.EditCategoryDTO;
import com.example.mailadmin.entity.Category;
import com.example.mailadmin.service.CategoryService;
import com.example.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类Controller
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@RestController
@RequestMapping("/admin/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取所有分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.selectAll();
        return Result.success(categories);
    }

    /**
     * 获取分类详细信息
     */
    @GetMapping(value = "/{id}")
    public Result<Category> getInfo(@PathVariable("id") Long id) {
        return Result.success(categoryService.selectById(id));
    }

    /**
     * 新增分类
     */
    @PostMapping("/add")
    public Result add(@RequestBody AddCategoryDTO addCategoryDTO) {
        categoryService.insertCategory(addCategoryDTO);
        return Result.success();
    }

    /**
     * 修改分类
     */
    @PutMapping("/edit")
    public Result edit(@RequestBody EditCategoryDTO editCategoryDTO) {
        categoryService.updateCategory(editCategoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/remove/{ids}")
    public Result remove(@PathVariable Long[] ids) {
        categoryService.deleteByIds(ids);
        return Result.success();
    }
}
