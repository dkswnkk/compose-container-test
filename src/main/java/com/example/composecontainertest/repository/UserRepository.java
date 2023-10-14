package com.example.composecontainertest.repository;

import com.example.composecontainertest.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
}
