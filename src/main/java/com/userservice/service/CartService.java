package com.userservice.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.userservice.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.userservice.model.User;
import java.util.Optional;
import com.userservice.repository.CartRepository;
import com.userservice.model.CartItem;
import com.userservice.controller.UserController;
import com.userservice.model.Cart;

@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    public CartItem addItemToCart(String userId, CartItem newItem) {
        logger.info("userId: " + userId);
        logger.info("newItem: " + newItem);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String cartId = user.getCartId();
        Cart cart;

        if (cartId == null || cartId.isEmpty()) {
            cart = new Cart(new ArrayList<>());
            cart = cartRepository.save(cart);
            user.setCartId(cart.getId());
            userRepository.save(user);
        } else {
            cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));
        }
        List<CartItem> items = cart.getItems();
        if (items == null) {
            items = new ArrayList<>();
        }
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> Objects.equals(item.getProductName(), newItem.getProductName()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().incrementQuantity();
        } else {
            newItem.setQuantity(1);
            items.add(newItem);
        }
        cart.setItems(items);
        cartRepository.save(cart);
        return existingItem.orElse(newItem);
    }

    public Cart getCartById(String cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public Cart removeItemFromCart(String userId, String providedCartId, String itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user found with ID: " + userId));
        String cartId = user.getCartId();
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("No cart found for the user with ID: " + userId));
        if (!cart.getId().equals(providedCartId)) {
            throw new IllegalArgumentException("The provided cart ID does not match the cart ID for the user.");
        }
        List<CartItem> items = cart.getItems();
        Optional<CartItem> optionalItem = items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
    
        if (optionalItem.isEmpty()) {
            throw new IllegalArgumentException("Item not found in cart with ID: " + itemId);
        }
        CartItem item = optionalItem.get();
        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            items.remove(item);
        }
        return cartRepository.save(cart);
    }
}