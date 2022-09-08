package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.request.SignupRequest;
import by.homesite.joplinforwarder.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserSignupRequestMapper extends BasicMapper<User, SignupRequest>
{
}
