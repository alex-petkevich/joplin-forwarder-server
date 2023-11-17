package by.homesite.joplinforwarder.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JoplinAttachment {
    String id;

    String name;

    LocalDateTime createdTime;

    LocalDateTime updatedTime;

    LocalDateTime userCreatedTime;

    LocalDateTime userUpdatedTime;

    String encryptionCipherText;

    int encryptionApplied;

    int isShared;

    String shareId;

    String masterKeyId;

    String mime;

    String filename;

    String fileExtension;

    String encryptionBlobEncrypted;

    long size;

    int type_;
}
