package com.multi.matchon.common.util;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsS3Utils {

    private final S3Operations s3Operations;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;



    public void saveFile(String dirName, String fileName, MultipartFile multipartFile) {

        // String replaceFileName = fileName + "." + FilenameUtils.getExtension(multipartFile.getResource().getFilename());

        String s3Key = dirName + fileName;

        System.out.println("======>    "+s3Key);
        try (InputStream inputStream = multipartFile.getInputStream()) {
            ObjectMetadata metadata = ObjectMetadata.builder()
                    .contentType(multipartFile.getContentType())
                    .build();

            s3Operations.upload(bucket, s3Key, inputStream, metadata);
            // 2. 업로드 후 ACL을 public-read로 변경
            PutObjectAclRequest aclRequest = PutObjectAclRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObjectAcl(aclRequest); //개별권한설정
            log.info("[AwsS3Utils] File Uploaded Successfully: " + s3Key);

            //return replaceFileName;

        } catch (IOException e) {
            log.error("[AwsS3Utils] File Upload Failed: " + s3Key, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }
    }

    public S3Resource downloadFile(String dirName, String savedName) {

        S3Resource resource =  s3Operations.download(bucket,dirName+savedName);
        return resource;
    }

    /* Create a pre-signed URL to download an object in a subsequent GET request. */
    public String createPresignedGetUrl(String dirName, String savedName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(dirName+savedName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            log.info("Presigned URL: [{}]", presignedRequest.url().toString());
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }



}
