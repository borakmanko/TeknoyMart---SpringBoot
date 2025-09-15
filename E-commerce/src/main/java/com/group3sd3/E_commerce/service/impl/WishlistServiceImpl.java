package com.group3sd3.E_commerce.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.group3sd3.E_commerce.model.Product;
import com.group3sd3.E_commerce.model.User;
import com.group3sd3.E_commerce.model.Wishlist;
import com.group3sd3.E_commerce.repository.WishlistRepository;
import com.group3sd3.E_commerce.service.WishlistService;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    public List<Wishlist> getWishlistByUser(User user) {
        return wishlistRepository.findByUser(user);
    }

    public Wishlist saveWishlist(User user, Product product) {
        Wishlist wishlist = wishlistRepository.findByUserAndProduct(user, product);
        if (wishlist == null) {
            wishlist = new Wishlist();
            wishlist.setUser(user);
            wishlist.setProduct(product);
            wishlist.setAddedDate(LocalDate.now());
            wishlistRepository.save(wishlist);
        }
        return wishlist;
    }

    public void deleteWishlist(User user, Product product) {
        Wishlist wishlist = wishlistRepository.findByUserAndProduct(user, product);
        wishlistRepository.delete(wishlist);
    }

    public List<Integer> getWishlistProductIdsByUser(User user) {
        List<Wishlist> wishlists = wishlistRepository.findByUser(user);
        return wishlists.stream().map(Wishlist::getProduct).map(Product::getId).collect(Collectors.toList());
    }
}
