package reactor.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.model.Product;

public interface ProductRepository extends ReactiveMongoRepository<Product, String> {
}
