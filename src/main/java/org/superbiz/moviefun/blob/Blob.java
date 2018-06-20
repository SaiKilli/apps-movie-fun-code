package org.superbiz.moviefun.blob;

import java.io.InputStream;

public class Blob {
    private final String name;
    private final InputStream inputStream;
    private final String contentType;

    public Blob(String name, InputStream inputStream, String contentType) {
        this.name = name;
        this.inputStream = inputStream;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getContentType() {
        return contentType;
    }
}
