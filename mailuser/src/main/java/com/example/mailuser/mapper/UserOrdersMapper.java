package com.example.mailuser.mapper;

import com.example.mailuser.dto.MyOrdersPageQueryDTO;
import com.example.mailuser.entity.Orders;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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
    void pay(Orders orders);

    Page<Orders> PageQuery(MyOrdersPageQueryDTO myOrdersPageQueryDTO);

    // 根据订单ID和用户ID查询订单
    Orders getOrderByIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    // 更新订单状态
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus);

    // 更新订单支付信息
    void updateOrderForPayment(@Param("orderId") Long orderId, @Param("orderStatus") Integer orderStatus, @Param("paymentMethod") Integer paymentMethod);
}
