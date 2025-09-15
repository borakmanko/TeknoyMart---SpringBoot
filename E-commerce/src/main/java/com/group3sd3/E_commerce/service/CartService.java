package com.group3sd3.E_commerce.service;

import java.util.List;

import com.group3sd3.E_commerce.model.Cart;

public interface CartService {

    public Cart saveCart(String productItemId, Integer userId);

    public List<Cart> getCartsByUser(Integer userId);

    public Integer getCountCart(Integer userId);

    public Integer getCountUnpurchasedCart(Integer userId);

	public void updateQuantity(String sy, Integer cid);

    public void markItemsAsPurchased(Integer userId);
}
