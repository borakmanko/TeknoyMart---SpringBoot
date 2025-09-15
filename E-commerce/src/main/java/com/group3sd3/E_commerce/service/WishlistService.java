package com.group3sd3.E_commerce.service;

import java.util.List;

import com.group3sd3.E_commerce.model.Product;
import com.group3sd3.E_commerce.model.User;
import com.group3sd3.E_commerce.model.Wishlist;

public interface WishlistService {

    public List<Wishlist> getWishlistByUser(User user);

    public Wishlist saveWishlist(User user, Product product);

    public void deleteWishlist(User user, Product product);

    public List<Integer> getWishlistProductIdsByUser(User user);
}
