package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.request.UserRequest;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.util.BasicMapper;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSignupRequestMapper extends BasicMapper<User, UserRequest>
{
}
