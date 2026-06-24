package com.study.app.domains.board.dao;

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

	@Value("${gcp.bucket-name}")
	private String bucketName;

	public void insertPostAttachments(Long post_id, List<MultipartFile> files) {

		if (post_id == null) {
			throw new IllegalArgumentException("첨부파일 저장 실패: post_id가 null입니다.");
		}

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

					PostAttachmentDTO attachmentDTO = new PostAttachmentDTO();

					attachmentDTO.setPost_id(post_id);
					attachmentDTO.setFile_name(originalName);
					attachmentDTO.setFile_path(sysName);
					attachmentDTO.setFile_size(file.getSize());
					attachmentDTO.setFile_type(file.getContentType());

					System.out.println("첨부파일 저장 post_id = " + attachmentDTO.getPost_id());
					System.out.println("첨부파일 저장 file_name = " + attachmentDTO.getFile_name());
					System.out.println("첨부파일 저장 file_path = " + attachmentDTO.getFile_path());

					mybatis.insert("PostAttachment.insertFiles", attachmentDTO);

				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("첨부파일 저장 중 오류 발생", e);
				}
			}
		}
	}

	public void deletePostAttachByPostId(Long post_id) {
		mybatis.delete("PostAttachment.deletePostAttachByPostId", post_id);
	}

	public List<PostAttachmentDTO> selectPostAttachByPostId(Long post_id) {

		// 첨부파일 목록 조회 시
		List<PostAttachmentDTO> files = mybatis.selectList("PostAttachment.selectPostAttachByPostId", post_id);

		for (PostAttachmentDTO file : files) { // 조회 시 전체 경로 완성
			String path = file.getFile_path();

			if (path != null && !path.startsWith("http")) {
				file.setFile_path(
						"https://storage.googleapis.com/"
								+ bucketName
								+ "/"
								+ path
						);
			}
		}

		return files;

	}

	public void deletePostAttachById(Long id) {
		mybatis.delete("PostAttachment.deletePostAttachById", id);
	}

	public PostAttachmentDTO selectPostAttachById(Long id) {
		return mybatis.selectOne("PostAttachment.selectPostAttachById", id);
	}

}
