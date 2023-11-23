package by.homesite.joplinforwarder.service;

import java.nio.file.Path;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService
{
	String LOAD_AVATAR = "loadAvatar";

	void init();

	void save(MultipartFile file);

	Resource load(String filename);

	void deleteAll();

	List<Path> loadAll();

	List<Path> loadAllByUsername(String userId);
}
