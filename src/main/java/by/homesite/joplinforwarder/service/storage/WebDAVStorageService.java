package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import by.homesite.joplinforwarder.service.SettingsService;
import by.homesite.joplinforwarder.service.dto.JoplinItem;
import by.homesite.joplinforwarder.service.storage.mapper.JoplinItemMailMapper;
import by.homesite.joplinforwarder.util.JoplinParserUtil;
import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.HttpClientParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Component
public class WebDAVStorageService implements StorageService {

    private final SettingsService settingsService;
    private final JoplinItemMailMapper mailMapper;
    private final JoplinParserUtil joplinParserUtil;

    private static final Logger log = LoggerFactory.getLogger(WebDAVStorageService.class);

    public WebDAVStorageService(SettingsService settingsService, JoplinItemMailMapper mailMapper, JoplinParserUtil joplinParserUtil) {
        this.settingsService = settingsService;
        this.mailMapper = mailMapper;
        this.joplinParserUtil = joplinParserUtil;
    }

    @Override
    public void storeRecord(User user, Mail mail) {

        // connect to the webdav
        String joplinserverdavurl = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavurl");
        String joplinserverdavusername = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavusername");
        String joplinserverdavpassword = settingsService.getSettingValue(user.getSettingsList(), "joplinserverdavpassword");

        if (!StringUtils.hasText(joplinserverdavurl) || !StringUtils.hasText(joplinserverdavusername) || !StringUtils.hasText(joplinserverdavpassword)) {
            return;
        }

        if (mail.getRule() != null && StringUtils.hasText(mail.getRule().getSave_in_parent_id())) {

        }

        //storeNode(joplinserverdavurl, joplinserverdavusername, joplinserverdavpassword, mail);
        getDBItemsList(joplinserverdavurl, joplinserverdavusername, joplinserverdavpassword);
    }

    private void storeNode(String joplinserverdavurl, String joplinserverdavusername, String joplinserverdavpassword, Mail mail) {
        JoplinItem jNode = mailMapper.toDto(mail);

        if (!StringUtils.hasText(jNode.getId())) {
            return;
        }

        String fileName = jNode.getId() + ".md";
        String fileContent = joplinParserUtil.nodeToText(jNode);

        HttpURLConnection connection = connectToWebDavServer(joplinserverdavurl + fileName, joplinserverdavusername, joplinserverdavpassword);
        try {
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("Content-Length", String.valueOf(fileContent.length()));
            connection.setDoOutput(true);

            try (BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {
                outputStream.write(fileContent.getBytes());
            }
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                log.error("WebDAV record saved successfully");
            } else {
                log.error("WebDAV Failed to save the file. Response code:%d".formatted(responseCode));
            }

        } catch (Exception e) {
            log.error("WebDAV connection error");
        } finally {
            connection.disconnect();
        }
    }

    private List<JoplinItem> getDBItemsList(String joplinserverdavurl, String joplinserverdavusername, String joplinserverdavpassword) {

        List<JoplinItem> result = new ArrayList<>();


        Sardine sardine = SardineFactory.begin(joplinserverdavusername, joplinserverdavpassword);
        try
        {
            URI url = URI.create(joplinserverdavurl);
            final List<DavResource> resources = sardine.list(url.toString());

            resources.forEach(item -> {
                String itemFileName = item.getName();
                JoplinItem jItem = new JoplinItem();
                jItem.setName(itemFileName);
                result.add(jItem);
                //result.add(joplinParserUtil.textToNode(itemContent));
            });

            return result;
        }
        catch (IOException e)
        {
            log.error("WebDAV connection error");
        }

        return Collections.emptyList();
    }

    private String readItemContent(String s) {
        return "";
    }

    private List<String> getItems(HttpURLConnection connection) {
        try (InputStream inputStream = connection.getInputStream()) {
            String result = new String(inputStream.readAllBytes());
            return Collections.singletonList(result);
        } catch (IOException e) {
            log.error("WebDAV request error");
        }

        return Collections.emptyList();
    }

    private HttpURLConnection connectToWebDavServer(String joplinserverdavurl, String joplinserverdavusername, String joplinserverdavpassword) {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(joplinserverdavurl);

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){ /* TODO document why this method is empty */ }
                public void checkServerTrusted(X509Certificate[] certs, String authType){ /* TODO document why this method is empty */ }
            }};

            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                log.error("WebDAV unable to override SSL cert request error");
            }
            connection = (HttpURLConnection) url.openConnection();
            String credentials = joplinserverdavusername + ":" + joplinserverdavpassword;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            connection.setRequestProperty("Authorization", "Basic " + encodedCredentials);

            connection.setRequestMethod("PROPFIND");
            connection.setRequestProperty("Depth", "1"); // Set the depth of the request
            connection.setRequestProperty("Content-Type", "text/xml"); // Set the content type
        } catch (Exception e) {
            log.error("WebDAV connection error");
            throw new RuntimeException(e);
        }

        return connection;
    }
}
