package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.controllers.dto.request.MailRequest;
import by.homesite.joplinforwarder.controllers.dto.response.MailResponse;
import by.homesite.joplinforwarder.controllers.mapper.MailMapper;
import by.homesite.joplinforwarder.controllers.mapper.MailRequestMapper;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.MailService;
import by.homesite.joplinforwarder.service.UserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import by.homesite.joplinforwarder.util.ControllerUtil;
import jakarta.validation.Valid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/mails")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class MailsController
{
	private static final int MAIL_RECORDS_LIMIT = 20;

	final
	UserService userService;

	final
	MailService mailService;

	final
	MailMapper mailMapper;

	final
	MailRequestMapper mailRequestMapper;

	public MailsController(UserService userService, MailService mailService, MailMapper mailMapper, MailRequestMapper mailRequestMapper) {
		this.userService = userService;
		this.mailService = mailService;
		this.mailMapper = mailMapper;
		this.mailRequestMapper = mailRequestMapper;
	}

	@GetMapping("/")
	@ResponseBody
	public ResponseEntity<Page<MailResponse>> getUserMails(@RequestParam(required = false) String fsubject,
			@RequestParam(required = false)  String ftext,
			@RequestParam(required = false)  Boolean fattachments,
			@RequestParam (required = false) Boolean fexported,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "received-desc") String sort)
	{
		User user = userService.getCurrentUser();

		Pageable paging = PageRequest.of(page, MAIL_RECORDS_LIMIT, ControllerUtil.getSortOrder(sort));
		
		Page<MailResponse> result = mailService.getUserMails(user.getId(), fsubject, ftext, fattachments, fexported, paging).map(mailMapper::toEntity);

		return ResponseEntity.ok(result);
	}

	@GetMapping("/{id}")
	@ResponseBody
	public ResponseEntity<MailResponse> getMail(@Valid @PathVariable String id)
	{
		User user = userService.getCurrentUser();
		MailResponse result = mailMapper.toEntity(mailService.getMail(Integer.parseInt(id), user.getId()));

		return ResponseEntity.ok(result);
	}

	@DeleteMapping("/{id}")
	@ResponseBody
	public ResponseEntity<?> deleteMail(@Valid @PathVariable String id)
	{
		User user = userService.getCurrentUser();
		mailService.deleteMail(Integer.parseInt(id), user.getId());

		return ResponseEntity.ok().build();
	}

	@PostMapping("/")
	public ResponseEntity<MailResponse> save(@Valid @RequestBody MailRequest userRule)
	{
		User user = userService.getCurrentUser();
		Mail result = mailService.save(user, mailRequestMapper.toDto(userRule));

		return ResponseEntity.ok(mailMapper.toEntity(result));
	}

	@PostMapping("/resync/")
	public ResponseEntity<MailResponse> resync(@Valid @RequestBody MailRequest[] mails)
	{
		User user = userService.getCurrentUser();
		List<Mail> mailList = new ArrayList<>();
		for(MailRequest mail: mails) {
			mailList.add(mailService.getMail(Math.toIntExact(mail.getId()), user.getId()));
		}
		mailService.storeMails(user, mailList);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{id}/download")
	public byte[] downloadAttachment(@PathVariable String id, @RequestParam String f) throws IOException
	{
		User user = userService.getCurrentUser();
		Mail mail = mailService.getMail(Integer.parseInt(id), user.getId());
		if (mail == null) {
			return "".getBytes();
		}
		
		return mailService.getAttachment(mail.getAttachments(), f);
	}
}
