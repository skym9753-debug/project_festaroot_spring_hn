package com.study.app.domains.board;

import java.util.List;
import java.util.UUID;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.study.app.domains.board.dto.PostAttachmentDTO;
import com.study.app.domains.storage.StorageController;

@Repository
public class FileDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
	@Autowired
	private Storage storage;
	
	@Autowired
	private StorageController storageController;
	
	@Value("${gcp.bucket-name}")
	private String bucketName;
	
	public void insertPostAttachments(Long post_id, List<MultipartFile> files) {

	    for (MultipartFile file : files) {
	        if (!file.isEmpty()) {
	            try {
	                String originalName = file.getOriginalFilename();

	                String sysName = "board/file/" 
	                        + UUID.randomUUID().toString() 
	                        + "_" 
	                        + originalName;

	                BlobId blobId = BlobId.of(bucketName, sysName);

	                BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
	                        .setContentType(file.getContentType())
	                        .build();

	                storage.create(blobInfo, file.getBytes());

	                String fileUrl = storageController.uploadFile(file, "board/file");

	                PostAttachmentDTO attachmentDTO = new PostAttachmentDTO(

	                        post_id,
	                        originalName,
	                        fileUrl,
	                        file.getSize(),
	                        file.getContentType()

	                );

	                mybatis.insert("PostAttachment.insertFiles", attachmentDTO);

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }
	}
	
	
	

}
