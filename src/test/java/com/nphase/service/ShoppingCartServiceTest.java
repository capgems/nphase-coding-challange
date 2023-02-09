package com.nphase.service;


import com.nphase.entity.Category;
import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;

@SpringBootTest
public class ShoppingCartServiceTest {
    private final ShoppingCartService service = new ShoppingCartService();

    @Test
    public void calculatesPrice() {
        ShoppingCart cart = new ShoppingCart(Arrays.asList(
                new Product("Tea", BigDecimal.valueOf(5.0), 2, new Category("drinks")),
                new Product("Coffee", BigDecimal.valueOf(6.5), 1, new Category("drinks"))
        ));

        BigDecimal result = service.calculateTotalPrice(cart);

        Assertions.assertEquals(result, BigDecimal.valueOf(16.5));
    }

    @Test
    public void calculatesDiscountedPrices() {
        ReflectionTestUtils.setField(service, "ITEM_DISCOUNT", 0.10);
        ShoppingCart cart = new ShoppingCart(Arrays.asList(
                new Product("Tea", BigDecimal.valueOf(5.0), 5, new Category("drinks")),
                new Product("Coffee", BigDecimal.valueOf(3.5), 3, new Category("drinks"))
        ));

        BigDecimal result = service.calculateTotalPriceWithDiscount(cart);

        Assertions.assertEquals(33.00, result.doubleValue());
    }

    @Test
    public void calculatesDiscountedPricesForSameCategories() {
        ReflectionTestUtils.setField(service, "CAT_DISCOUNT", 0.10);
        ShoppingCart cart = new ShoppingCart(Arrays.asList(
                new Product("Tea", BigDecimal.valueOf(5.3), 2, new Category("drinks")),
                new Product("Coffee", BigDecimal.valueOf(3.5), 2, new Category("drinks")),
                new Product("Cheese", BigDecimal.valueOf(8), 2, new Category("food"))
        ));

        BigDecimal result = service.calculateTotalPriceWithCategoryWiseDiscount(cart);
        Assertions.assertEquals(31.84, result.doubleValue());
    }

}