package by.homesite.joplinforwarder.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import by.homesite.joplinforwarder.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FilesystemFilesStorageServiceImpl implements FilesStorageService
{
	private final ApplicationProperties applicationProperties;
	private final Path userDir;

	public FilesystemFilesStorageServiceImpl(@Autowired ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
		this.userDir = getUserDir();
	}

	@Override
	public void init()
	{
		if (this.userDir == null) {
			return;
		}
		try
		{
			Files.createDirectory(this.userDir);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	@Override
	public void save(MultipartFile file)
	{
		if (this.userDir == null || file.getOriginalFilename() == null) {
			return;
		}

		init();

		try
		{
			Files.copy(file.getInputStream(), this.userDir.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}
	}

	@Override
	public Resource load(String filename)
	{
		if (this.userDir == null) {
			return null;
		}

		try
		{
			Path file = this.userDir.resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable())
			{
				return resource;
			}
			else
			{
				throw new RuntimeException("Could not read the file!");
			}
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException("Error: " + e.getMessage());
		}
	}

	@Override
	public void deleteAll()
	{
		if (this.userDir == null) {
			return;
		}

		FileSystemUtils.deleteRecursively(this.userDir.toFile());
	}

	@Override
	public List<Path> loadAll()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return Collections.emptyList();
		}

		return loadAllByUsername(authentication.getName());
	}

	@Override
	public List<Path> loadAllByUsername(String userId) {
		Path userDir = Paths.get(getUploadDir(userId));

		try
		{
			return Files
					.walk(userDir, 1)
					.filter(path -> !path.equals(userDir))
					.map(userDir::relativize)
					.collect(Collectors.toList());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not load the files!");
		}
	}

	private Path getUserDir()
	{
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			return Paths.get( getUploadDir(authentication.getName()));
		}
		return null;
	}

	private String getUploadDir(String userId) {
		return applicationProperties.getUpload().getLocalPath() + applicationProperties.getUpload().getUploadDir() + File.separator + userId + File.separator;
	}
}
