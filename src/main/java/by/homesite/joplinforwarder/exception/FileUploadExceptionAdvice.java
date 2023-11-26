package by.homesite.joplinforwarder.exception;

import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.service.TranslateService;

@ControllerAdvice
public class FileUploadExceptionAdvice
{
	final
	TranslateService translate;

	public FileUploadExceptionAdvice(TranslateService translate) {
		this.translate = translate;
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<MessageResponse> handleMaxSizeException(MaxUploadSizeExceededException exc)
	{
		return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse(translate.get("file-upload.file-too-large")));
	}
}
