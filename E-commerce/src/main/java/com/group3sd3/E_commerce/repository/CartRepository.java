package com.group3sd3.E_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group3sd3.E_commerce.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    public Cart findByProductItemIdAndUserId(String productItemId, Integer userId);

    public Integer countByUserId(Integer userId);

    Integer countByUserIdAndPurchased(Integer userId, Boolean purchased);

	public List<Cart> findByUserId(Integer userId);
    
}
