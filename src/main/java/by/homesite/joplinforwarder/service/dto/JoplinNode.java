package by.homesite.joplinforwarder.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JoplinNode {
    String id;

    String name;

    String parentId;

    LocalDateTime createdTime;

    LocalDateTime updatedTime;

    LocalDateTime userCreatedTime;

    LocalDateTime userUpdatedTime;

    String encryptionCipherText;

    int encryptionApplied;

    int isShared;

    String shareId;

    String masterKeyId;

    int type_;
}
