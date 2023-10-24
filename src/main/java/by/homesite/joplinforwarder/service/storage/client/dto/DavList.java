package by.homesite.joplinforwarder.service.storage.client.dto;

import java.util.ArrayList;
import java.util.List;

public class DavList {
    private final List<DavFile> files = new ArrayList<>();
    private final List<DavDirectory> directories = new ArrayList<>();

    public void addFile(DavFile file) {
        files.add(file);
    }

    public void addDirectory(DavDirectory dir) {
        directories.add(dir);
    }

    public List<DavElement> getAllSubElements() {
        ArrayList<DavElement> objects = new ArrayList<>();
        objects.addAll(files);
        objects.addAll(directories);
        return objects;
    }

    public List<DavFile> getFiles() {
        return files;
    }

    public List<DavDirectory> getDirectories() {
        return directories;
    }
}
