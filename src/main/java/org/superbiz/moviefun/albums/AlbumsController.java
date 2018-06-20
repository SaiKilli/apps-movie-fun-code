package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blob.Blob;
import org.superbiz.moviefun.blob.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final ResourceLoader resourceLoader;
    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

    public AlbumsController(ResourceLoader resourceLoader, AlbumsBean albumsBean, BlobStore blobStore) {
        this.resourceLoader = resourceLoader;
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        blobStore.put(new Blob(String.valueOf(albumId), new BufferedInputStream(uploadedFile.getInputStream()), MediaType.IMAGE_JPEG_VALUE));
        /*saveUploadToFile(uploadedFile, getCoverFile(albumId));

        return format("redirect:/albums/%d", albumId);*/
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Optional<Blob> albumCover = blobStore.get(String.valueOf(albumId));

        Blob blob;
        if(albumCover.isPresent()){
            blob = albumCover.get();
        }else{
            albumCover = blobStore.get("default-cover.jpg");
            if(albumCover.isPresent()){
                blob = albumCover.get();
            } else{
                InputStream inputStream = resourceLoader.getResource("classpath:/default-cover.jpg").getInputStream();
                blob = new Blob(String.valueOf(albumId), inputStream, new Tika().detect(inputStream));
            }
        }

        byte[] imageBytes = IOUtils.toByteArray(blob.getInputStream());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(imageBytes.length);
        headers.setContentType(MediaType.parseMediaType(blob.getContentType()));

        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}
