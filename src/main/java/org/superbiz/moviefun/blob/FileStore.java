package org.superbiz.moviefun.blob;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

public class FileStore implements BlobStore {
    @Override
    public void put(Blob blob) throws IOException {
        /*byte[] buffer = new byte[blob.getInputStream().available()];
        blob.getInputStream().read(buffer);*/

        String coverFileName = format("covers/%s", blob.getName());
        File localFile =  new File(coverFileName);

        localFile.delete();
        localFile.getParentFile().mkdirs();
        localFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(localFile)) {
            IOUtils.copy(blob.getInputStream(), outputStream);
        }
    }

    @Override
    public Optional<Blob> get(String name) throws IOException, URISyntaxException {
        File file = new File("covers/" + name);

        if(!file.exists()){
            return Optional.empty();
        }

        return  Optional.of(new Blob(name, new FileInputStream(file), MediaType.IMAGE_JPEG_VALUE));
    }

    @Override
    public void deleteAll() {
        String coverFileDir = "covers/";
        File coverFileDirFile = new File(coverFileDir);
        coverFileDirFile.delete();

    }

    private Path getExistingCoverPath(String albumName) throws URISyntaxException {
        File coverFile = getCoverFile(albumName);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

    private File getCoverFile(String albumName) {
        String coverFileName = format("covers/%d", albumName);
        return new File(coverFileName);
    }
}
