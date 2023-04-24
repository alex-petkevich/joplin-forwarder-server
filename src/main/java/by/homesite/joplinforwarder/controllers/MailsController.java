package by.homesite.joplinforwarder.controllers;

import by.homesite.joplinforwarder.controllers.dto.request.MailRequest;
import by.homesite.joplinforwarder.controllers.dto.request.RuleRequest;
import by.homesite.joplinforwarder.controllers.dto.response.MailResponse;
import by.homesite.joplinforwarder.controllers.dto.response.RuleResponse;
import by.homesite.joplinforwarder.controllers.mapper.MailMapper;
import by.homesite.joplinforwarder.controllers.mapper.MailRequestMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleMapper;
import by.homesite.joplinforwarder.controllers.mapper.RuleRequestMapper;
import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.Rule;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.MailService;
import by.homesite.joplinforwarder.service.UserService;
import by.homesite.joplinforwarder.service.mailer.MailerService;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.validation.Valid;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mails")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class MailsController
{
	@Autowired
	UserService userService;

	@Autowired
	MailService mailService;

	@Autowired
	MailMapper mailMapper;

	@Autowired
	MailRequestMapper mailRequestMapper;

	@GetMapping("/")
	@ResponseBody
	public ResponseEntity<List<MailResponse>> getUserMails()
	{
		User user = userService.getCurrentUser();
		List<MailResponse> result = mailService.getUserMails(user.getId()).stream().map(mailMapper::toEntity).collect(Collectors.toList());

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
