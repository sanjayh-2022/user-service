package com.userservice.controller;
import com.userservice.model.Cart;
import com.userservice.model.CartItem;
import com.userservice.model.User;
import com.userservice.service.UserService;
import com.userservice.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/user")


public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserController(UserService userService, CartService cartService) {
        this.userService = userService;
        this.cartService = cartService;
    }


    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/id")
    public ResponseEntity<User> getUserById(@RequestHeader("X-User-ID") String userId) {
        try {
            return userService.getUserById(userId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    
    @PostMapping("/cart/add-item")
    public ResponseEntity<Map<String, Object>> addItemToCart(
        @RequestHeader("X-User-ID") String userId,
        @RequestBody CartItem newItem) {
    try {
        String filteredJson = objectMapper.writeValueAsString(Map.of(
            "id", newItem.getId(),
            "productName", newItem.getProductName(),
            "price", newItem.getPrice(),
            "image", newItem.getImage()
        ));
        logger.debug("Filtered JSON: {}", filteredJson);
        CartItem filteredItem = objectMapper.readValue(filteredJson, CartItem.class);
        logger.debug("Deserialized CartItem: {}", filteredItem);

        logger.info("Product: {}", filteredItem);

        CartItem addedItem = cartService.addItemToCart(userId, filteredItem);
        logger.info("Added item: {}", addedItem);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Item added to cart successfully");

        Map<String, Object> itemDetails = new HashMap<>();
        itemDetails.put("itemId", addedItem.getId());
        itemDetails.put("itemName", addedItem.getProductName());
        itemDetails.put("quantity", addedItem.getQuantity());
        itemDetails.put("price", addedItem.getPrice());
        itemDetails.put("image", addedItem.getImage());
        response.put("itemDetails", itemDetails);
        logger.info("Response: {}", response);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        logger.error("Unexpected error: ", e);
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", (e.getMessage() != null) ? e.getMessage() : "An unexpected error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
    
    @GetMapping("/cartdetails")
    public ResponseEntity<Cart> getCartDetails(@RequestHeader("X-User-ID") String userId) {
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            String cartId = user.get().getCartId();
            if (cartId == null || cartId.isEmpty()) {
                return ResponseEntity.ok(new Cart());
            }
            Cart cart = cartService.getCartById(cartId);
            return Optional.ofNullable(cart).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/cart/remove-item")
        public ResponseEntity<Map<String, Object>> removeItemFromCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody Map<String, String> requestBody) {
        String cartId = requestBody.get("cartId");
        String itemId = requestBody.get("itemId");

        // Validate input
        if (cartId == null || itemId == null) {
            Map<String, Object> errorResponse = Map.of(
                "message", "Invalid request: cartId and itemId are required"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Cart updatedCart = cartService.removeItemFromCart(userId, cartId, itemId);
            if (updatedCart == null || updatedCart.getItems().isEmpty()) {
                return ResponseEntity.ok().body(null);
            }
            Map<String, Object> response = Map.of(
                "message", "Item removed successfully",
                "cart", updatedCart
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                "message", "Failed to remove item: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

