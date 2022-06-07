package by.homesite.joplinforwarder.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
	
import by.homesite.joplinforwarder.controllers.dto.response.MessageResponse;
import by.homesite.joplinforwarder.model.FileInfo;
import by.homesite.joplinforwarder.service.FilesStorageService;

@Controller
@RequestMapping("/api/files")
public class FilesController
{
	@Autowired
	FilesStorageService storageService;

	@Value("${joplinforwarder.app.baseUrl}")
	String baseUrl;

	@PostMapping("/upload")
	public ResponseEntity<MessageResponse> uploadFile(@RequestParam("file") MultipartFile file)
	{
		String message = "";
		try
		{
			storageService.save(file);
			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(new MessageResponse(message));
		}
		catch (Exception e)
		{
			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse(message));
		}
	}

	@GetMapping("/list")
	public ResponseEntity<List<FileInfo>> getListFiles()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
			String filename = path.getFileName().toString();
			String url = baseUrl + "api/files/avatar/" + URLEncoder.encode(authentication.getName(), StandardCharsets.UTF_8);
			return new FileInfo(filename, url);
		}).collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}
	
	@GetMapping("/avatar/{userId}")
	public ResponseEntity<?> getAvatar(@PathVariable String userId) {
		
		return null;
	}

	@GetMapping("/list/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename)
	{
		Resource file = storageService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
	}
}
