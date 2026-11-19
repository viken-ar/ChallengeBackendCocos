package ar.com.cocos.challenge.infrastructure.database.mappers;

import ar.com.cocos.challenge.domain.model.User;
import ar.com.cocos.challenge.infrastructure.database.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserInfrastructureMapper {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
