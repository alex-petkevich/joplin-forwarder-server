package by.homesite.joplinforwarder.service.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class JoplinItem {
    String id;

    String name;

    String content;

    String parentId;

    LocalDateTime createdTime;

    LocalDateTime updatedTime;

    int isConflict;

    String latitude;

    String longitude;

    String altitude;

    String author;

    String sourceUrl;

    int isTodo;

    int todoDue;

    int todoCompleted;

    String source;

    String sourceApplication;

    String applicationData;

    Long order;

    LocalDateTime userCreatedTime;

    LocalDateTime userUpdatedTime;

    String encryptionCipherText;

    int encryptionApplied;

    int markupLanguage;

    int isShared;

    String shareId;

    String conflictOriginalId;

    String masterKeyId;

    int type_;
}
