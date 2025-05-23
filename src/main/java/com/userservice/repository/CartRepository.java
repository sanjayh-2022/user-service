package com.userservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.userservice.model.Cart;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

}