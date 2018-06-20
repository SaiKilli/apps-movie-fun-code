package org.superbiz.moviefun;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.superbiz.moviefun.blob.BlobStore;
import org.superbiz.moviefun.blob.S3Store;

@SpringBootApplication
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    private BlobStore getS3BlobStore(String s3AccessKey,
                                     String s3SecretKey,
                                     String s3BucketName,
                                     String s3EndpointUrl) {
        AWSCredentials credentials = new BasicAWSCredentials(s3AccessKey,
                s3SecretKey);
        AmazonS3Client s3Client = new AmazonS3Client(credentials);

        if (s3EndpointUrl != null && !s3EndpointUrl.equals("")) {
            s3Client.setEndpoint(s3EndpointUrl);
        }

        return new S3Store(s3Client, s3BucketName);
    }

    @Profile("cloud")
    @Bean
    public BlobStore cloudS3BlobStore(
            @Value("${vcap.services.moviefun-s3-service.credentials.access_key_id}") String s3AccessKey,
            @Value("${vcap.services.moviefun-s3-service.credentials.secret_access_key}") String s3SecretKey,
            @Value("${vcap.services.moviefun-s3-service.credentials.bucket}") String s3BucketName,
            @Value("${vcap.services.moviefun-s3-service.credentials.s3_endpointurl}") String s3EndpointUrl
    ) {
        return getS3BlobStore(s3AccessKey, s3SecretKey, s3BucketName, s3EndpointUrl);
    }
}
