package com.example.mailadmin.mapper;

import com.example.mailadmin.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 分类Mapper接口
 *
 * @author sunxiaoming
 * @date 2026-02-06
 */
@Mapper
public interface CategoryMapper {
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
     * @param category 分类信息
     * @return 影响行数
     */
    int insert(Category category);

    /**
     * 修改分类
     *
     * @param category 分类信息
     * @return 影响行数
     */
    int update(Category category);

    /**
     * 删除分类
     *
     * @param ids 分类ID数组
     * @return 影响行数
     */
    int deleteByIds(Long[] ids);
}
