package by.homesite.joplinforwarder.controllers.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;

@Getter
@Setter
public class MailRequest
{
	private Long id;

	private Integer converted;
}
