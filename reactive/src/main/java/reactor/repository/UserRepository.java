package reactor.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.model.UserInfo;

public interface UserRepository extends ReactiveMongoRepository<UserInfo, String> {
}
