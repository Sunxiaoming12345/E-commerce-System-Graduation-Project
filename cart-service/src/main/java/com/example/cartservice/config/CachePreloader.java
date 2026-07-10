package com.example.cartservice.config;

import com.example.cartservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Redis 缓存预热：应用启动时主动加载热点数据到 Redis，
 * 避免首个用户请求触发冷查询直接打到数据库。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CachePreloader implements CommandLineRunner {

    private final ProductService productService;

    @Override
    public void run(String... args) {
        log.info("开始预热缓存...");
        long start = System.currentTimeMillis();

        try {
            // ① 推荐商品列表
            var products = productService.getRecommendedProducts();
            log.info("推荐商品缓存已预热 ({} 件)", products.size());

            // ② 全部分类
            productService.getAllCategories();
            log.info("分类列表缓存已预热");

            // ③ 所有分类下的商品列表
            var categories = productService.getAllCategories();
            for (var cat : categories) {
                try {
                    productService.getProductsByCategoryId(cat.getId());
                } catch (Exception e) {
                    log.warn("分类 {} 商品缓存预热失败", cat.getName());
                }
            }
            log.info("全部分类商品缓存已预热");

            // ④ 所有商品详情（25 件以内全量预热）
            for (var p : products) {
                try {
                    productService.getProductById(p.getId());
                } catch (Exception e) {
                    log.warn("商品 {} 详情缓存预热失败", p.getId());
                }
            }
            log.info("商品详情缓存已预热 ({} 件)", products.size());

        } catch (Exception e) {
            log.error("缓存预热异常，启动后首次请求将回源数据库: {}", e.getMessage());
        }

        log.info("缓存预热完成，耗时 {}ms", System.currentTimeMillis() - start);
    }
}
