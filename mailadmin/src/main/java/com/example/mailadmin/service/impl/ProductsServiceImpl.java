package com.example.mailadmin.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.constant.MessageConstant;
import com.example.constant.StatusConstant;
import com.example.mailadmin.dto.AddProductsDTO;
import com.example.mailadmin.dto.EditProductsDTO;
import com.example.mailadmin.dto.ProductsPageQueryDTO;
import com.example.mailadmin.entity.Products;
import com.example.mailadmin.mapper.ProductsMapper;
import com.example.mailadmin.dto.StockUpdateDTO;
import com.example.mailadmin.service.ProductsService;
import com.example.result.PageResult;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ProductsServiceImpl implements ProductsService
{
    @Autowired
    private ProductsMapper productsMapper;

    // 分页查询商品
    @Override
    public PageResult PageQuery(ProductsPageQueryDTO productsPageQueryDTO) {
        PageHelper.startPage(productsPageQueryDTO.getPage(),productsPageQueryDTO.getPageSize());
        Page<Products> page = productsMapper.PageQuery(productsPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());


    }
    // 获取单个商品详细信息
    @Override
    public Products selectProductsById(Long id) {

        return productsMapper.selectProductsById(id);

    }

    // 新增商品
    @Override
    public void insertProducts(AddProductsDTO addProductsDTO) {
        Products products = new Products();
        BeanUtils.copyProperties(addProductsDTO,products);
        products.setCreateTime(LocalDateTime.now());
        products.setUpdateTime(LocalDateTime.now());
        productsMapper.insertProducts(products);


    }
    // 修改商品
    @Override
    public void updateProducts(EditProductsDTO editProductsDTO) {
        Products products = new Products();
        BeanUtils.copyProperties(editProductsDTO,products);
        products.setUpdateTime(LocalDateTime.now());
        productsMapper.updateProducts(products);
    }

    // 删除商品(单个或多个)
    @Override
    public void deleteProductsByIds(Long[] ids) {

        for (Long id : ids) {
           Products products = productsMapper.selectProductsById(id);
           if (products.getStatus()== StatusConstant.ENABLE)
           {
               throw new RuntimeException(MessageConstant.PRODUCTS_ON_SALE);
           }
           productsMapper.deleteProductsByIds(ids);
        }

    }

    // 更新商品库存
    @Override
    public void updateStock(Long id, Integer stock) {
        productsMapper.updateStock(id, stock);
    }

    // 批量更新商品库存
    @Override
    public void batchUpdateStock(List<StockUpdateDTO> stockUpdateDTOs) {
        List<Map<String, Object>> stockUpdateList = new ArrayList<>();
        for (StockUpdateDTO dto : stockUpdateDTOs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dto.getId());
            map.put("stock", dto.getStock());
            stockUpdateList.add(map);
        }
        productsMapper.batchUpdateStock(stockUpdateList);
    }

    // 上架商品
    @Override
    public void enable(Long[] ids) {
        productsMapper.enable(ids);
    }

    // 下架商品
    @Override
    public void disable(Long[] ids) {
        productsMapper.disable(ids);
    }
}
