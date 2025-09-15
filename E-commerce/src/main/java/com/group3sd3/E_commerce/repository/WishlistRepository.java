package com.group3sd3.E_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group3sd3.E_commerce.model.Product;
import com.group3sd3.E_commerce.model.User;
import com.group3sd3.E_commerce.model.Wishlist;

public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByUser(User user);

    Wishlist findByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
