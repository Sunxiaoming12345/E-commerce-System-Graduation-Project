package com.example.mailadmin.service.impl;

import com.example.utils.CacheUtil;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class ProductsServiceImpl implements ProductsService
{
    @Autowired
    private ProductsMapper productsMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private CacheUtil cacheUtil;

    private static final String RECOMMENDED_PRODUCTS_CACHE_KEY = "products:recommended";

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
        Long categoryId = currentProduct.getCategoryId();

        // 复制属性并更新
        Products products = new Products();
        BeanUtils.copyProperties(editProductsDTO,products);
        products.setUpdateTime(LocalDateTime.now());
        productsMapper.updateProducts(products);

        // 更新 Redis 库存缓存
        cacheUtil.updateStockInCache(editProductsDTO.getId(), newStock);
        // 清除商品详情缓存，下次查询时重建
        clearProductDetailCache(editProductsDTO.getId());

        // 只有当库存从有到0或从0到有时才发送消息和清除分类/推荐缓存
        if ((currentStock > 0 && newStock == 0) || (currentStock == 0 && newStock > 0)) {
            sendStockUpdateMessage(editProductsDTO.getId(), newStock, categoryId);
            if (categoryId != null) {
                clearCategoryProductCache(categoryId);
            }
            clearProductCache();
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
        Long categoryId = product.getCategoryId();
        // 更新库存
        productsMapper.updateStock(id, stock);
        // 更新 Redis 库存缓存
        cacheUtil.updateStockInCache(id, stock);
        // 清除商品详情缓存，下次查询时重建
        clearProductDetailCache(id);
        // 只有当库存从有到0或从0到有时才发送消息和清除分类/推荐缓存
        if ((currentStock > 0 && stock == 0) || (currentStock == 0 && stock > 0)) {
            sendStockUpdateMessage(id, stock, categoryId);
            if (categoryId != null) {
                clearCategoryProductCache(categoryId);
            }
            clearProductCache();
        }
    }

    // 批量更新商品库存
    @Override
    public void batchUpdateStock(List<StockUpdateDTO> stockUpdateDTOs) {
        List<Map<String, Object>> stockUpdateList = new ArrayList<>();
        boolean needClearRecommendedCache = false;
        for (StockUpdateDTO dto : stockUpdateDTOs) {
            // 获取当前库存
            Products product = productsMapper.selectProductsById(dto.getId());
            Integer currentStock = product.getStock();
            Long categoryId = product.getCategoryId();
            // 只有当库存从有到0或从0到有时才发送消息和清除缓存
            if ((currentStock > 0 && dto.getStock() == 0) || (currentStock == 0 && dto.getStock() > 0)) {
                sendStockUpdateMessage(dto.getId(), dto.getStock(), categoryId);
                // 清除对应分类的商品缓存
                if (categoryId != null) {
                    clearCategoryProductCache(categoryId);
                }
                needClearRecommendedCache = true;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", dto.getId());
            map.put("stock", dto.getStock());
            stockUpdateList.add(map);
            cacheUtil.updateStockInCache(dto.getId(), dto.getStock());
            clearProductDetailCache(dto.getId());
        }
        productsMapper.batchUpdateStock(stockUpdateList);
        // 如果有商品库存状态发生变化，清除推荐商品缓存
        if (needClearRecommendedCache) {
            clearProductCache();
        }
    }

    // 发送库存更新消息到RabbitMQ
    private void sendStockUpdateMessage(Long productId, Integer stock, Long categoryId) {
        try {
            // 构建包含商品ID、库存和分类ID的消息
            String message = productId + "," + stock + "," + categoryId;
            rabbitTemplate.convertAndSend(RabbitMQConstant.STOCK_EXCHANGE_NAME, RabbitMQConstant.STOCK_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("发送库存更新消息失败", e);
        }
    }

    // 清除商品相关缓存
    private void clearProductCache() {
        try {
            // 清除商品推荐缓存
            redisTemplate.delete(RECOMMENDED_PRODUCTS_CACHE_KEY);
            log.info("商品推荐缓存已清除");
        } catch (Exception e) {
            // 缓存清除失败不影响主业务流程
            log.warn("缓存清除失败，不影响商品操作: {}", e.getMessage());
        }
    }

    // 清除商品详情缓存
    private void clearProductDetailCache(Long productId) {
        try {
            redisTemplate.delete("product:detail:" + productId);
            log.info("商品详情缓存已清除，商品ID：{}", productId);
        } catch (Exception e) {
            log.warn("商品详情缓存清除失败，不影响商品操作: {}", e.getMessage());
        }
    }

    // 清除分类商品缓存
    private void clearCategoryProductCache(Long categoryId) {
        try {
            String categoryProductsCacheKey = "products:category:" + categoryId;
            redisTemplate.delete(categoryProductsCacheKey);
            log.info("分类商品缓存已清除，分类ID：{}", categoryId);
        } catch (Exception e) {
            // 缓存清除失败不影响主业务流程
            log.warn("分类商品缓存清除失败，不影响商品操作: {}", e.getMessage());
        }
    }

    // 上架商品
    @Override
    public void enable(Long[] ids) {
        productsMapper.enable(ids);
        // 清除商品相关缓存
        clearProductCache();
        // 清除对应分类的商品缓存
        for (Long id : ids) {
            Products product = productsMapper.selectProductsById(id);
            if (product != null && product.getCategoryId() != null) {
                clearCategoryProductCache(product.getCategoryId());
            }
        }
    }

    // 下架商品
    @Override
    public void disable(Long[] ids) {
        productsMapper.disable(ids);
        // 清除商品相关缓存
        clearProductCache();
        // 清除对应分类的商品缓存
        for (Long id : ids) {
            Products product = productsMapper.selectProductsById(id);
            if (product != null && product.getCategoryId() != null) {
                clearCategoryProductCache(product.getCategoryId());
            }
        }
    }
}
