package com.dropclip.dropclip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.endpoint}")
    private String endpoint;


    // =========================================================
    // GENERATE PRESIGNED UPLOAD URL
    // =========================================================

    public String generatePresignedUploadUrl(
            String userId,
            String dropId
    ) {

        String s3Key = String.format(
                "clips/%s/%s/%s.mp4",
                dropId,
                userId,
                UUID.randomUUID()
        );

        PutObjectRequest putObjectRequest =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .contentType("video/mp4")
                        .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofMinutes(15)
                        )
                        .putObjectRequest(
                                putObjectRequest
                        )
                        .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(
                        presignRequest
                );

        return presignedRequest.url().toString();
    }


    // =========================================================
    // EXTRACT S3 KEY
    // =========================================================

    public String extractS3Key(String presignedUrl) {

        /*
         * Example:
         *
         * https://bucket.account.r2.cloudflarestorage.com/
         * clips/drop/user/video.mp4?X-Amz...
         *
         * We only want:
         *
         * clips/drop/user/video.mp4
         */

        String urlWithoutQuery =
                presignedUrl.split("\\?")[0];

        String marker =
                bucketName + "/";

        int index =
                urlWithoutQuery.indexOf(marker);

        if (index != -1) {

            return urlWithoutQuery.substring(
                    index + marker.length()
            );
        }

        /*
         * If the endpoint is already bucket-specific,
         * find everything after the host.
         */

        int protocolIndex =
                urlWithoutQuery.indexOf("://");

        if (protocolIndex != -1) {

            int pathStart =
                    urlWithoutQuery.indexOf(
                            "/",
                            protocolIndex + 3
                    );

            if (pathStart != -1) {
                return urlWithoutQuery.substring(
                        pathStart + 1
                );
            }
        }

        return urlWithoutQuery;
    }


    // =========================================================
    // GENERATE PRESIGNED GET URL
    // =========================================================

    public String generatePresignedGetUrl(
            String s3Key
    ) {

        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(
                                Duration.ofHours(2)
                        )
                        .getObjectRequest(
                                getObjectRequest
                        )
                        .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(
                        presignRequest
                );

        return presignedRequest.url().toString();
    }


    // =========================================================
    // DELETE CLIP
    // =========================================================

    public void deleteClip(String s3Key) {

        s3Client.deleteObject(
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build()
        );
    }


    // =========================================================
    // OLD PUBLIC URL METHOD
    // DON'T USE THIS FOR PRIVATE R2 OBJECTS
    // =========================================================

    public String getClipUrl(String s3Key) {

        return generatePresignedGetUrl(s3Key);
    }
}