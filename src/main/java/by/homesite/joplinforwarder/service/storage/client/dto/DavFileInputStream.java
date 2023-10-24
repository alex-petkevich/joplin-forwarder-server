package by.homesite.joplinforwarder.service.storage.client.dto;

import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DavFileInputStream extends InputStream implements DavElement {

    private static final Logger log = LoggerFactory.getLogger(DavFileInputStream.class);

    private final DavPropertySet propertiesPresent;
    private final InputStream inputStream;
    private final Path tmpFile;

    public DavFileInputStream(DavPropertySet propertiesPresent, InputStream inputStream, Path tmpFile) {
        this.propertiesPresent = propertiesPresent;
        this.tmpFile = tmpFile;
        this.inputStream = loadingInputStreamToTmpFile( inputStream, tmpFile);
    }

    private InputStream loadingInputStreamToTmpFile(InputStream inputStream, Path tmpFile) {
        try {
            Files.delete(tmpFile);
            Files.copy(inputStream, tmpFile);
        } catch (IOException e) {
            log.error("Couldn't load file {} due to io exception.", tmpFile, e);
        }
        try {
            return new FileInputStream(tmpFile.toFile());
        } catch (FileNotFoundException e) {
            log.error("Cannot open input stream of local file {}", tmpFile , e);
        }
        return null;
    }

    @Override
    public DavPropertySet getPropertiesPresent() {
        return propertiesPresent;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return inputStream.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        Files.delete(tmpFile);
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return inputStream.transferTo(out);
    }
}