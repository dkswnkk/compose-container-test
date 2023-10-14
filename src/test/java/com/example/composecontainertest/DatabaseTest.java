package com.example.composecontainertest;

import com.example.composecontainertest.entity.User;
import com.example.composecontainertest.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;

class DatabaseTest extends IntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAndRetrieveUserTest() {
        // given
        User user = new User(null, "dkswnkk");
        userRepository.save(user);

        // when
        entityManager.flush();
        entityManager.clear();
        var retrievedUser = userRepository.findById(1L);

        // then
        Assertions.assertEquals("dkswnkk", retrievedUser.get().getName());
    }
}