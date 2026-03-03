package com.example.mailuser.listener;

import com.example.constant.RabbitMQConstant;
import com.example.mailuser.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存更新消息消费者
 * 用于接收库存更新的消息并更新推荐商品的缓存
 */
@Component
@Slf4j
public class StockUpdateMessageConsumer {

    @Autowired
    private ProductService productService;

    /**
     * 监听库存更新消息
     * 当收到消息时，清除推荐商品缓存和对应分类的商品缓存
     */
    @RabbitListener(queues = RabbitMQConstant.STOCK_QUEUE_NAME)
    public void handleStockUpdateMessage(Object message) {
        log.info("收到库存更新消息：{}", message);
        try {
            // 解析消息格式：productId,stock,categoryId
            String messageStr = message.toString();
            // 清理消息字符串，去除可能的额外信息
            messageStr = messageStr.trim();
            // 如果消息包含多个逗号，只取前三个部分
            String[] parts = messageStr.split(",");
            if (parts.length >= 3) {
                // 清除对应分类的商品缓存
                try {
                    // 清理分类ID部分，只保留数字
                    String categoryIdStr = parts[2].trim().replaceAll("[^0-9]", "");
                    if (!categoryIdStr.isEmpty()) {
                        Long categoryId = Long.parseLong(categoryIdStr);
                        productService.clearCategoryProductsCache(categoryId);
                        log.info("已清除分类商品缓存，分类ID：{}", categoryId);
                    } else {
                        log.warn("分类ID为空，跳过清除分类商品缓存");
                    }
                } catch (NumberFormatException e) {
                    log.error("解析分类ID失败：{}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理库存更新消息失败：{}", e.getMessage());
        }
        // 清除推荐商品缓存
        productService.clearRecommendedProductsCache();
        log.info("已清除推荐商品缓存");
    }
}
