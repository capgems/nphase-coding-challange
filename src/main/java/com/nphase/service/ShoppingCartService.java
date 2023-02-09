package com.nphase.service;

import com.nphase.entity.Category;
import com.nphase.entity.Product;
import com.nphase.entity.ShoppingCart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ShoppingCartService {

    @Value("${categoryWiseDiscount}")
    private double CAT_DISCOUNT;
    @Value("${itemWiseDiscount}")
    private double ITEM_DISCOUNT;

    static Function<List<Product>, BigDecimal> computeSumFn = productList -> productList.stream().map(e -> (e.getPricePerUnit().multiply(BigDecimal.valueOf(e.getQuantity()))))
            .reduce(BigDecimal::add).orElse(BigDecimal.ZERO);

    public BigDecimal calculateTotalPrice(ShoppingCart shoppingCart) {
        return shoppingCart.getProducts()
                .stream()
                .map(product -> product.getPricePerUnit().multiply(BigDecimal.valueOf(product.getQuantity())))
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal calculateTotalPriceWithDiscount(ShoppingCart shoppingCart) {
        Map<Integer, List<Product>> collect = shoppingCart.getProducts().stream().collect(Collectors.groupingBy(Product::getQuantity));
        return collect.entrySet().stream()
                .map(elem -> {
                    if (elem.getKey() > 3) {
                        return computeTheDiscountedAmount(elem.getValue(), ITEM_DISCOUNT);
                    }
                    return computeSumFn.apply(elem.getValue());
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateTotalPriceWithCategoryWiseDiscount(ShoppingCart shoppingCart) {
        Map<Category, List<Product>> products = shoppingCart.getProducts()
                .stream()
                .collect(Collectors.groupingBy(Product::getCategory));

        return products.values()
                .stream()
                .map(this::checkAndComputeCategoryWiseTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal checkAndComputeCategoryWiseTotalAmount(List<Product> products) {
        int quantity = products.stream()
                .mapToInt(Product::getQuantity)
                .sum();
        BigDecimal sum = computeSumFn.apply(products);
        if (quantity > 3) {
            sum = computeTheDiscountedAmount(products, CAT_DISCOUNT);
        }
        return sum;
    }

    private static BigDecimal computeTheDiscountedAmount(List<Product> products, Double discount) {
        if (discount != 0) {
            BigDecimal amount = computeSumFn.apply(products);
            return amount.subtract(amount.multiply(BigDecimal.valueOf(discount)));
        }
        return BigDecimal.ZERO;
    }
}
