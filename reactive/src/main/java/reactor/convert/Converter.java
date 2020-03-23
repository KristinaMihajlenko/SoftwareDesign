package reactor.convert;

import reactor.model.Product;

import java.util.HashMap;
import java.util.Map;

public class Converter {
    private final static Map<String, Double> CURRENCIES = new HashMap<>();

    static {
        CURRENCIES.put("dollar", 80.0);
        CURRENCIES.put("euro", 85.0);
        CURRENCIES.put("ruble", 1.0);
    }

    public Product convertFromRuble(Product product, String currency) {
        final Double coeff = CURRENCIES.getOrDefault(currency, 1.0);
        final Double prevPrice = product.getPrice();

        product.setPrice(prevPrice / coeff);
        return product;
    }

    public Product convertToRuble(Product product, String currency) {
        final Double coeff = CURRENCIES.getOrDefault(currency, 1.0);
        final Double prevPrice = product.getPrice();

        product.setPrice(prevPrice * coeff);
        return product;
    }
}
