package by.homesite.joplinforwarder.controllers.mapper;

import by.homesite.joplinforwarder.controllers.dto.response.SettingsResponse;
import by.homesite.joplinforwarder.model.Settings;
import by.homesite.joplinforwarder.util.BasicMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class})
public interface SettingsMapper extends BasicMapper<Settings, SettingsResponse>
{

    @Mapping(source = "user.id", target = "user_id")
    SettingsResponse toEntity(Settings settings);


}
