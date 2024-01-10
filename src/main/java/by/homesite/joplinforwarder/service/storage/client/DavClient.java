package by.homesite.joplinforwarder.service.storage.client;

import by.homesite.joplinforwarder.service.storage.client.dto.DavDirectory;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFile;
import by.homesite.joplinforwarder.service.storage.client.dto.DavFileInputStream;
import by.homesite.joplinforwarder.service.storage.client.dto.DavList;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpOptions;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

public class DavClient {
    private static final Logger logger = LoggerFactory.getLogger(DavClient.class);
    public static final String ERROR_ACCESS_FAILED = "Access to %s failed with a non 2xx status. Status was %d";

    private HttpClientContext context;
    private CloseableHttpClient client;
    private URI baseUri;
    private String root;
    private boolean isReady = false;

    /**
     * prepare the client
     *
     * @param uri      - base URI
     * @param username - (sic)
     * @param password - (sic)
     */
    public void init(URI uri, String username, String password) {
        this.root = uri.toASCIIString();
        if (!this.root.endsWith("/")) {
            this.root += "/";
        }

        baseUri = uri;

        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(username, password));

        AuthCache authCache = new BasicAuthCache();

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        this.context = HttpClientContext.create();
        this.context.setCredentialsProvider(credsProvider);
        this.context.setAuthCache(authCache);

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        SSLContext sslContext = null;
        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
        clientBuilder.setSslcontext( sslContext);

        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
        clientBuilder.setConnectionManager( connMgr);

        this.client = clientBuilder.build();
        this.isReady = true;
    }

    /**
     * @param uri - the URI to query
     * @return the list of DAV compliance classes
     */
    public Set<String> getDavComplianceClasses(URI uri) throws IOException {
        HttpOptions options = new HttpOptions(uri);
        HttpResponse response = this.client.execute(options, this.context);
        int status = response.getStatusLine().getStatusCode();

        if (!Integer.valueOf(200).equals(status)) {
            logger.error("ERROR! INSTEAD OF HTTP 200 I GOT {}", status);
            throw new DavAccessFailedException("EXPECTED HTTP 200. GOT " + status);
        }

        return options.getDavComplianceClasses(response);
    }

    /**
     * @param file the file to load
     * @return an inputstream to the file content
     */
    public DavFileInputStream readFile(DavFile file) {
        DavFileInputStream result = null;
        try {
            Path tmp = Files.createTempFile(file.getName() + "-", null);
            logger.debug("Using temp file {}", tmp);

            URI source = file.getURI();

            HttpGet get = new HttpGet(source);
            CloseableHttpResponse response = this.client.execute(get, this.context);

            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {

                InputStream stream = response.getEntity().getContent();
                result = new DavFileInputStream(file.getPropertiesPresent(), stream, tmp);

            } else {
                logger.error("ERROR! INSTEAD OF HTTP 200 I GOT {}", status);
                throw new DavAccessFailedException("EXPECTED HTTP 200. GOT " + status);
            }
        } catch (IOException e) {
            logger.error("Error while reading file", e);
        }
        return result;
    }

    /**
     * list a directory
     *
     * @param resource e.g. a subdirectory relative to the base URL
     * @return lsResult containing all subdirs and files
     */
    public DavList list(String resource) throws IOException, DavException {
        DavList result = new DavList();

        DavPropertyNameSet set = new DavPropertyNameSet();
        set.add(DavPropertyName.create(DavConstants.PROPERTY_DISPLAYNAME));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_RESOURCETYPE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_SOURCE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTLENGTH));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETCONTENTTYPE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_CREATIONDATE));
        set.add(DavPropertyName.create(DavConstants.PROPERTY_GETLASTMODIFIED));

        String uri = baseUri.toString() + ("/" + resource).replace("//", "/");
        URI baseURI = URI.create(uri);
        HttpPropfind propfind = new HttpPropfind(uri, set, 1);
        HttpResponse resp = this.client.execute(propfind, this.context);
        int status = resp.getStatusLine().getStatusCode();
        if (status / 100 != 2) {
            throw new DavAccessFailedException(
                    ERROR_ACCESS_FAILED.formatted(propfind.toString(), status));
        }

        MultiStatus multistatus = propfind.getResponseBodyAsMultiStatus(resp);
        MultiStatusResponse[] responses = multistatus.getResponses();

        for (MultiStatusResponse respons : responses) {
            DavPropertySet found = respons.getProperties(200);
            DavPropertySet notfound = respons.getProperties(404);

            if (notfound.contains(DavPropertyName.GETCONTENTLENGTH)) {
                result.addDirectory(new DavDirectory(baseURI, found));
            } else {
                result.addFile(new DavFile(baseURI, found));
            }
        }
        return result;
    }

    /**
     * create a directory
     *
     * @param resource e.g. the subdirectory relative to the base URL
     * @return directory containing the properties
     */
    public DavDirectory mkdir(String resource) throws IOException, DavException {
        String uri = baseUri.toString() + ("/" + resource).replace("//", "/");
        HttpMkcol mkcol = new HttpMkcol(uri);
        DavList davList = getServerResponse(resource, this.client.execute(mkcol, this.context), mkcol.toString());
        List<DavDirectory> webDavDirectories = davList.getDirectories();
        if (webDavDirectories.isEmpty()) {
            throw new DavAccessFailedException(
                    "Directory %s will not be found.".formatted(mkcol.toString()));
        }
        return webDavDirectories.get(0);


    }

    /**
     * put data to a directory
     *
     * @param fileName the file name relative to the base URL
     * @return file containing the properties
     */
    public DavFile put(byte[] content, String fileName) throws IOException, DavException {
        String uri = baseUri.toString() + ("/" + fileName).replace("//", "/");
        HttpPut httpPut = new HttpPut(uri);
        ByteArrayEntity entity = new ByteArrayEntity(content);
        httpPut.setEntity(entity);
        DavList davList = getServerResponse(fileName, this.client.execute(httpPut, this.context), httpPut.toString());
        List<DavFile> webDavFileList = davList.getFiles();
        if (webDavFileList.isEmpty()) {
            throw new DavAccessFailedException(
                    "No File %s will be found.".formatted(httpPut.toString()));
        }
        return webDavFileList.get(0);
    }

    /**
     * delete file
     *
     * @param fileName the file name relative to the base URL
     * @return file containing the properties
     */
    public boolean delete(String fileName) throws IOException {
        String uri = baseUri.toString() + ("/" + fileName).replace("//", "/");
        HttpDelete httpDelete = new HttpDelete(uri);
        int status = this.client.execute(httpDelete, this.context).getStatusLine().getStatusCode();
        return (status / 100 == 2);
    }

    private DavList getServerResponse(String fileName, CloseableHttpResponse execute, String string) throws IOException, DavException {
        int status = execute.getStatusLine().getStatusCode();
        if (status / 100 != 2) {
            throw new DavAccessFailedException(
                    ERROR_ACCESS_FAILED.formatted(string, status));
        }
        return list(fileName);
    }

    public boolean isReady() {
        return isReady;
    }

    /**
     * Exception fired if an DAV access screws up
     */
    public static final class DavAccessFailedException extends RuntimeException {

        public DavAccessFailedException(String message) {
            super(message);
        }
    }

    public String getRoot() {
        return root;
    }
}
