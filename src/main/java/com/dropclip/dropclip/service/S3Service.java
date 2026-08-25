package com.dropclip.dropclip.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    // Generate a presigned URL for uploading a videos
    public String generatePresignedUploadUrl(String userId, String dropId) {

        // Create a unique key for this clip
        String s3Key = String.format("clips/%s/%s/%s.mp4",
                dropId, userId, UUID.randomUUID());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType("video/mp4")
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest =
                s3Presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    // Extract S3 key from a presigned URL
    public String extractS3Key(String presignedUrl) {
        String path = presignedUrl
                .replace(endpoint + "/" + bucketName + "/", "");
        return path.split("\\?")[0];
    }

    // Delete a clip from S3
    public void deleteClip(String s3Key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build());
    }

    // Get the public URL for a clip
    public String getClipUrl(String s3Key) {
        return String.format("%s/%s/%s", endpoint, bucketName, s3Key);
    }
}