package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.User;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findById(Integer id);
    Optional<User> findByEmail(String email);
    Optional<User> findByAccountNumber(String accountNumber);
    User save(User user);
}
