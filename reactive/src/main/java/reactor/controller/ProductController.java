package reactor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.convert.Converter;
import reactor.exception.AlreadyExistsException;
import reactor.model.Product;
import reactor.model.UserInfo;
import reactor.repository.ProductRepository;
import reactor.repository.UserRepository;

import javax.validation.Valid;

@RestController
public class ProductController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/products")
    public Flux<Product> getAllProducts(@RequestParam String id) {
        Flux<Product> products = productRepository.findAll();
        return userRepository.findById(id)
                .flatMapMany(user -> products
                        .map(product -> convertFromRuble(user, product)));
    }

    @PostMapping("/products")
    public Mono<Product> createProduct(@Valid @RequestBody Product product, @RequestParam String id) {
        return productRepository
                .existsById(product.getName())
                .flatMap(alreadyExists -> {
                    if (alreadyExists) {
                        throw new AlreadyExistsException();
                    }
                    return productRepository
                            .saveAll(userRepository.findById(id)
                            .map(user -> convertToRuble(user, product)))
                            .next();
                });
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity handleAlreadyExistsException(AlreadyExistsException ex) {
        return ResponseEntity.badRequest().build();
    }

    private Product convertToRuble(UserInfo user, Product product) {
        return new Converter().convertToRuble(product, user.getCurrency());
    }

    private Product convertFromRuble(UserInfo user, Product product) {
        return new Converter().convertFromRuble(product, user.getCurrency());
    }
}
