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
     * 当收到消息时，清除推荐商品缓存
     */
    @RabbitListener(queues = RabbitMQConstant.STOCK_QUEUE_NAME)
    public void handleStockUpdateMessage(Object message) {
        log.info("收到库存更新消息：{}", message);
        // 清除推荐商品缓存
        productService.clearRecommendedProductsCache();
    }
}
