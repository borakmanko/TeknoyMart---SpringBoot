package com.group3sd3.E_commerce.service;

import java.util.List;

import com.group3sd3.E_commerce.model.OrderReq;
import com.group3sd3.E_commerce.model.ProductOrder;

public interface OrderService {

    public void saveOrder(Integer userid, OrderReq orderRequest);

    public List<ProductOrder> getOrdersByUser(Integer userId);

    public Boolean updateOrderStatus(Integer id, String status);

}
