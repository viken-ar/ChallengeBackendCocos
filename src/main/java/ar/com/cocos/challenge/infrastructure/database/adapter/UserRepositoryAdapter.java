package ar.com.cocos.challenge.infrastructure.database.adapter;

import ar.com.cocos.challenge.application.outbound.UserRepositoryPort;
import ar.com.cocos.challenge.domain.model.User;
import ar.com.cocos.challenge.infrastructure.database.entities.UserEntity;
import ar.com.cocos.challenge.infrastructure.database.mappers.UserInfrastructureMapper;
import ar.com.cocos.challenge.infrastructure.database.repositories.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserInfrastructureMapper userInfrastructureMapper;

    public UserRepositoryAdapter(
            UserJpaRepository userJpaRepository,
            UserInfrastructureMapper userInfrastructureMapper
    ) {
        this.userJpaRepository = userJpaRepository;
        this.userInfrastructureMapper = userInfrastructureMapper;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userJpaRepository.findById(id).map(userInfrastructureMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(userInfrastructureMapper::toDomain);
    }

    @Override
    public Optional<User> findByAccountNumber(String accountNumber) {
        return userJpaRepository.findByAccountNumber(accountNumber).map(userInfrastructureMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = userInfrastructureMapper.toEntity(user);
        UserEntity saved = userJpaRepository.save(entity);
        return userInfrastructureMapper.toDomain(saved);
    }
}
