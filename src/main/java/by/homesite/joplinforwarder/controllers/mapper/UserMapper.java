package by.homesite.joplinforwarder.controllers.mapper;

import org.mapstruct.Mapper;

import by.homesite.joplinforwarder.controllers.dto.response.UserResponse;
import by.homesite.joplinforwarder.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper extends BasicMapper<User, UserResponse>
{
}
