package com.bsp.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public String uploadFile(MultipartFile file) {
        try {
            // 1. Kiểm tra bucket tồn tại chưa, chưa thì tạo mới
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // Cài đặt Policy để ảnh có thể xem public trên web
                String policy = """
                        {
                          "Statement": [
                            {
                              "Action": "s3:GetObject",
                              "Effect": "Allow",
                              "Principal": "*",
                              "Resource": "arn:aws:s3:::%s/*"
                            }
                          ],
                          "Version": "2012-10-17"
                        }
                        """.formatted(bucketName);
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            }

            // 2. Tạo tên file độc nhất để không bị trùng
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".png";
            String fileName = UUID.randomUUID().toString() + extension;

            // 3. Upload lên MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 4. Trả về Public URL
            return minioUrl + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi upload file lên Storage: " + e.getMessage());
        }
    }
}