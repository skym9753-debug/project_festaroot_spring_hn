package com.study.app.domains.storage;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;

@Service
public class UploadService {
	

    @Value("${gcp.bucket-name}")
    private String bucketName;

    @Autowired
    private Storage storage;
    
    /**
     * GCP 스토리지 파일 업로드
     */
    public String upload(MultipartFile file, String folder) throws IOException {

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String objectName = folder + "/" + fileName;

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return String.format("https://storage.googleapis.com/%s/%s", bucketName, objectName);
    }

    /**
     * GCP 스토리지 파일 삭제
     * @param fileUrl 삭제할 파일의 전체 URL
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        try {
            // URL에서 objectName 추출
            // 예: https://storage.googleapis.com/bucket-name/inquiry/uuid_file.jpg
            String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);
            
            if (!fileUrl.startsWith(prefix)) {
                // 버킷명이 다르거나 경로가 다르면 로그 출력 후 종료
                System.err.println("삭제 실패: 잘못된 파일 경로입니다. -> " + fileUrl);
                return false;
            }

            String objectName = fileUrl.substring(prefix.length());
            BlobId blobId = BlobId.of(bucketName, objectName);
            
            boolean deleted = storage.delete(blobId);
            if (deleted) {
                System.out.println("GCP 파일 삭제 성공: " + objectName);
            } else {
                System.err.println("GCP 파일 삭제 실패 (파일 없음): " + objectName);
            }
            return deleted;
        } catch (Exception e) {
            System.err.println("GCP 파일 삭제 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 본문 내용에서 GCP 이미지 URL을 찾아 모두 삭제
     */
    public void deleteImagesFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return;
        }

        // GCP URL 패턴 (예: https://storage.googleapis.com/bucket-name/folder/uuid_file.jpg)
        String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);
        // 정규식: prefix로 시작하고 따옴표, 공백, > 가 아닌 문자들이 이어짐
        String patternString = java.util.regex.Pattern.quote(prefix) + "[^\"'\\s>]+";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);
        java.util.regex.Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String fileUrl = matcher.group();
            deleteFile(fileUrl);
        }
    }

    /**
     * 기존 본문과 새 본문을 비교하여, 삭제된 이미지만 GCP에서 제거
     */
    public void deleteRemovedImages(String oldContent, String newContent) {
        if (oldContent == null || oldContent.isEmpty()) {
            return;
        }

        // GCP URL 추출을 위한 패턴
        String prefix = String.format("https://storage.googleapis.com/%s/", bucketName);
        String patternString = java.util.regex.Pattern.quote(prefix) + "[^\"'\\s>]+";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternString);

        // 1. 기존 본문의 모든 이미지 URL 추출
        java.util.HashSet<String> oldUrls = new java.util.HashSet<>();
        java.util.regex.Matcher oldMatcher = pattern.matcher(oldContent);
        while (oldMatcher.find()) {
            oldUrls.add(oldMatcher.group());
        }

        if (oldUrls.isEmpty()) return;

        // 2. 새 본문의 모든 이미지 URL 추출
        java.util.HashSet<String> newUrls = new java.util.HashSet<>();
        if (newContent != null && !newContent.isEmpty()) {
            java.util.regex.Matcher newMatcher = pattern.matcher(newContent);
            while (newMatcher.find()) {
                newUrls.add(newMatcher.group());
            }
        }

        // 3. 기존에는 있었으나 새 본문에는 없는 URL 삭제
        for (String url : oldUrls) {
            if (!newUrls.contains(url)) {
                deleteFile(url);
            }
        }
    }
}
