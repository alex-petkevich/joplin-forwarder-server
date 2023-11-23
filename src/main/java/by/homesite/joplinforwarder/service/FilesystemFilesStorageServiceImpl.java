package by.homesite.joplinforwarder.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import by.homesite.joplinforwarder.config.ApplicationProperties;

@Service
public class FilesystemFilesStorageServiceImpl implements FilesStorageService
{
	private final ApplicationProperties applicationProperties;

	public FilesystemFilesStorageServiceImpl(@Autowired ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	@Override
	public void init()
	{
		if (getUserDir() == null) {
			return;
		}
		try
		{
			Files.createDirectory(getUserDir());
		}
		catch (IOException e)
		{
			throw new RuntimeException("Could not initialize folder for upload!");
		}
	}

	@Override
	public void save(MultipartFile file)
	{
		Path dir = getUserDir();
		if (dir == null || file.getOriginalFilename() == null) {
			return;
		}

		init();

		try
		{
			Files.copy(file.getInputStream(), dir.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
		}
	}

	@Override
	public Resource load(String filename)
	{
		Path dir = getUserDir();
		if (dir == null) {
			return null;
		}

		try
		{
			Path file = dir.resolve(filename);
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
		Path dir = getUserDir();
		if (dir == null) {
			return;
		}

		FileSystemUtils.deleteRecursively(dir.toFile());
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
	@Cacheable(cacheNames = LOAD_AVATAR)
	public List<Path> loadAllByUsername(String userId) {
		Path userDir = Paths.get(getUploadDir(userId));

		try (Stream<Path> rs = Files.walk(userDir, 1)) {
			return rs.filter(path -> !path.equals(userDir)).toList();
		}
		catch(NoSuchFileException e) {
			init();
			return this.loadAllByUsername(userId);
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
