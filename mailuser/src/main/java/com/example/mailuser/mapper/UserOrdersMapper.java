package com.example.mailuser.mapper;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.entity.Orders;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserOrdersMapper {
  /*  //预下单
   @Insert("insert into orders(user_id,order_number,order_status,total_amount,create_time) values(#{userId},#{orderNumber},#{orderStatus},#{totalAmount},#{createTime})")
    void prepurchase(Orders orders);*/

   /* //查询用户id查询订单
   @Select("select * from orders where user_id=#{userId}")
    Orders selectByUserId(long userId);*/

   @Insert("insert into orders(user_id, order_number, order_status, payment_method, total_amount, shipping_address, receiver_name, receiver_phone, create_time) values(#{userId},#{orderNumber},#{orderStatus},#{paymentMethod},#{totalAmount},#{shippingAddress},#{receiverName},#{receiverPhone},#{createTime}) ")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    void pay(Orders orders);

    Page<Orders> PageQuery(MyOrdersPageQueryDTO myOrdersPageQueryDTO);

    // 根据订单ID和用户ID查询订单
    Orders getOrderByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // 根据订单ID查询订单
    Orders getOrderById(@Param("orderId") Long orderId);

    // 更新订单状态
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus);

    // 更新订单支付信息
    void updateOrderForPayment(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus, @Param("paymentMethod") Integer paymentMethod);
    
    // 保存订单项
    void saveOrderItem(@Param("orderId") Long orderId, @Param("productId") Long productId, @Param("productName") String productName, @Param("productPrice") java.math.BigDecimal productPrice, @Param("quantity") Integer quantity, @Param("subtotal") java.math.BigDecimal subtotal);
    
    // 根据订单ID查询订单项列表
    java.util.List<com.example.mailadmin.entity.OrderItems> selectOrderItemsByOrderId(@Param("orderId") Long orderId);
    
    // 更新订单总金额
    void updateOrderTotalAmount(@Param("orderId") Long orderId, @Param("totalAmount") java.math.BigDecimal totalAmount);

    // 根据订单号查询订单
    Orders selectByOrderNumber(@Param("orderNumber") String orderNumber);

    long countByUserId(@Param("userId") Long userId);

    java.math.BigDecimal sumConsumptionByUserId(@Param("userId") Long userId);
}
