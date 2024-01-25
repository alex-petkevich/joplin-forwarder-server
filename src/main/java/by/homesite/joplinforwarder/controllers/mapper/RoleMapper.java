package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.response.RoleResponse;
import by.homesite.joplinforwarder.model.Role;
import by.homesite.joplinforwarder.util.BasicMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { RoleMapper.class})
public interface RoleMapper extends BasicMapper<Role, RoleResponse>
{
    RoleResponse toEntity(Role role);
}
