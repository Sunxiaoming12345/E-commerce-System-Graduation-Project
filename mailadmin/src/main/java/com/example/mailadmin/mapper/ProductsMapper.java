package com.example.mailadmin.mapper;


import com.example.mailadmin.dto.ProductsPageQueryDTO;
import com.example.mailadmin.entity.Products;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品Mapper接口
 *
 * @author sunxiaoming
 * @date 2026-01-31
 */
@Mapper
public interface ProductsMapper
{
    /**
     * 查询商品
     *
     * @param id 商品主键
     * @return 商品
     */
    @Select("select * from products where id = #{id}")
    public Products selectProductsById(Long id);


    /**
     * 新增商品
     *
     * @param products 商品
     */
    @Insert("insert into products(name,description,image_url,category_id,price,stock,status,create_time,update_time) values(#{name},#{description},#{imageUrl},#{categoryId},#{price},#{stock},#{status},#{createTime},#{updateTime})")
    public void insertProducts(Products products);

    /**
     * 修改商品
     *
     * @param products 商品
     * @return 结果
     */
    public void updateProducts(Products products);


    /**
     * 批量删除商品
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public void deleteProductsByIds(Long[] ids);


    /**
    * 分页查询商品
    *
    * @param productsPageQueryDTO
    * @return
     */
    Page<Products> PageQuery(ProductsPageQueryDTO productsPageQueryDTO) ;

    /**
     * 更新商品库存
     *
     * @param id 商品ID
     * @param stock 库存数量
     * @return 结果
     */
    void updateStock(@Param("id") Long id, @Param("stock") Integer stock);

    /**
     * 批量更新商品库存
     *
     * @param stockUpdateList 库存更新列表
     * @return 结果
     */
    void batchUpdateStock(@Param("stockUpdateList") List<Map<String, Object>> stockUpdateList);

    /**
     * 上架商品
     *
     * @param ids 商品ID数组
     * @return 结果
     */
    void enable(@Param("ids") Long[] ids);

    /**
     * 下架商品
     *
     * @param ids 商品ID数组
     * @return 结果
     */
    void disable(@Param("ids") Long[] ids);


}

