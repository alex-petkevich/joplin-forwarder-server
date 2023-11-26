package by.homesite.joplinforwarder.controllers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MailResponse {
    private Long id;

    private Integer user_id;

    private String rule_name;

    private Integer rule_id;

    private String subject;

    private String text;

    private String sender;

    private String recipient;

    private String converted;

    private Integer processed;

    @Column(name = "added_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime created_at;

    private String attachments;

    private String[] attachList;

    @Column(name = "message_id")
    private String messageId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private OffsetDateTime received;
}
