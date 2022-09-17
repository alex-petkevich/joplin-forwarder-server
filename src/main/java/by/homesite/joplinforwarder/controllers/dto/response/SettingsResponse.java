package by.homesite.joplinforwarder.controllers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SettingsResponse {
    private Long id;

    private Integer user_id;

    private String name;

    private String value;

    private OffsetDateTime createdAt;

    private OffsetDateTime lastModifiedAt;
}
