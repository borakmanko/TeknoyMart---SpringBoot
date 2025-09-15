package com.group3sd3.E_commerce.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.group3sd3.E_commerce.model.Cart;
import com.group3sd3.E_commerce.model.Product;
import com.group3sd3.E_commerce.model.User;
import com.group3sd3.E_commerce.repository.CartRepository;
import com.group3sd3.E_commerce.repository.ProductRepository;
import com.group3sd3.E_commerce.repository.UserRepository;
import com.group3sd3.E_commerce.service.CartService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductRepository productRepository;

	@Override
	public Cart saveCart(String productItemId, Integer userId) {
		User userDtls = userRepository.findById(userId).orElse(null);
		Product product = productRepository.findByItemId(productItemId).orElse(null);

		if (userDtls == null || product == null) {
			return null;
		}

		Cart cartStatus = cartRepository.findByProductItemIdAndUserId(productItemId, userId);

		if (cartStatus != null && cartStatus.getPurchased()) {
			cartRepository.delete(cartStatus);
			cartStatus = null;
		}

		Cart cart = cartStatus != null ? cartStatus : new Cart();
		cart.setProduct(product);
		cart.setUser(userDtls);
		cart.setPurchased(false);

		if (cart.getId() == null) {
			cart.setQuantity(1);
			cart.setTotalPrice(product.getDiscountPrice());
		} else {
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * product.getDiscountPrice());
		}

		return cartRepository.save(cart);
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);

		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();
		for (Cart c : carts) {
			if (c.getPurchased() == null || !c.getPurchased()) {
				Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
				c.setTotalPrice(totalPrice);
				totalOrderPrice += totalPrice;
				c.setTotalOrderPrice(totalOrderPrice);
				updateCarts.add(c);
			}
		}

		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		Integer countByUserId = cartRepository.countByUserId(userId);
		return countByUserId;
	}

	@Override
	public Integer getCountUnpurchasedCart(Integer userId) {
		return cartRepository.countByUserIdAndPurchased(userId, false);
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {

		Cart cart = cartRepository.findById(cid).get();
		int updateQuantity;

		if (sy.equalsIgnoreCase("de")) {
			updateQuantity = cart.getQuantity() - 1;

			if (updateQuantity <= 0) {
				cartRepository.delete(cart);
			} else {
				cart.setQuantity(updateQuantity);
				cartRepository.save(cart);
			}

		} else {
			updateQuantity = cart.getQuantity() + 1;
			cart.setQuantity(updateQuantity);
			cartRepository.save(cart);
		}

	}

	public void markItemsAsPurchased(Integer userId) {
		List<Cart> carts = cartRepository.findByUserId(userId);
		for (Cart cart : carts) {
			if (!cart.getPurchased()) {
				cart.setPurchased(true);
				cartRepository.save(cart);
			}
		}
	}

}
