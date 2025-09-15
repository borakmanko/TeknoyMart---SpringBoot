package com.group3sd3.E_commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.group3sd3.E_commerce.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    public User findByEmail(String email);

    public List<User> findByRole(String role);

    public List<User> findByRoleAndIsEnable(String role, Boolean isEnable);

    public User findByResetToken(String token);

}
