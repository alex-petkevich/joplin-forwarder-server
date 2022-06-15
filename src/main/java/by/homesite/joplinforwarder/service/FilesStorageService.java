package by.homesite.joplinforwarder.service;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import by.homesite.joplinforwarder.model.FileInfo;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService
{
	public void init();

	public void save(MultipartFile file);

	public Resource load(String filename);

	public void deleteAll();

	public Stream<Path> loadAll();

	Stream<Path> loadAllByUsername(String userId);
}
