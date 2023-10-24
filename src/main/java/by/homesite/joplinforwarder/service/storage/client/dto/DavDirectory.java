package by.homesite.joplinforwarder.service.storage.client.dto;

import org.apache.jackrabbit.webdav.property.DavPropertySet;

import java.net.URI;

public class DavDirectory implements DavElement {

    private final URI baseURI;
    private final DavPropertySet propertiesPresent;

    public DavDirectory(URI baseURI, DavPropertySet propertiesPresent) {
        this.baseURI = baseURI;
        this.propertiesPresent = propertiesPresent;
    }

    public URI getBaseURI() {
        return baseURI;
    }

    @Override
    public DavPropertySet getPropertiesPresent() {
        return propertiesPresent;
    }
}
