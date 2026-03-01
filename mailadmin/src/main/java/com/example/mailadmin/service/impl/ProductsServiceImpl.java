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
import com.example.constant.RabbitMQConstant;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;



@Service
public class ProductsServiceImpl implements ProductsService
{
    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
        // 获取当前商品信息
        Products currentProduct = productsMapper.selectProductsById(editProductsDTO.getId());
        Integer currentStock = currentProduct.getStock();
        Integer newStock = editProductsDTO.getStock();
        
        // 复制属性并更新
        Products products = new Products();
        BeanUtils.copyProperties(editProductsDTO,products);
        products.setUpdateTime(LocalDateTime.now());
        productsMapper.updateProducts(products);
        
        // 只有当库存从有到0或从0到有时才发送消息
        if ((currentStock > 0 && newStock == 0) || (currentStock == 0 && newStock > 0)) {
            sendStockUpdateMessage(editProductsDTO.getId(), newStock);
        }
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
        // 获取当前库存
        Products product = productsMapper.selectProductsById(id);
        Integer currentStock = product.getStock();
        // 更新库存
        productsMapper.updateStock(id, stock);
        // 只有当库存从有到0或从0到有时才发送消息
        if ((currentStock > 0 && stock == 0) || (currentStock == 0 && stock > 0)) {
            sendStockUpdateMessage(id, stock);
        }
    }

    // 批量更新商品库存
    @Override
    public void batchUpdateStock(List<StockUpdateDTO> stockUpdateDTOs) {
        List<Map<String, Object>> stockUpdateList = new ArrayList<>();
        for (StockUpdateDTO dto : stockUpdateDTOs) {
            // 获取当前库存
            Products product = productsMapper.selectProductsById(dto.getId());
            Integer currentStock = product.getStock();
            // 只有当库存从有到0或从0到有时才发送消息
            if ((currentStock > 0 && dto.getStock() == 0) || (currentStock == 0 && dto.getStock() > 0)) {
                sendStockUpdateMessage(dto.getId(), dto.getStock());
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", dto.getId());
            map.put("stock", dto.getStock());
            stockUpdateList.add(map);
        }
        productsMapper.batchUpdateStock(stockUpdateList);
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock) {
        try {
            String message = "Product " + productId + " stock updated to " + stock;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
