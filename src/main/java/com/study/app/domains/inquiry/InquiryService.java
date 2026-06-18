package com.study.app.domains.inquiry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.study.app.domains.inquiry.dto.InquiryAnswerDTO;
import com.study.app.domains.inquiry.dto.InquiryAttachmentDTO;
import com.study.app.domains.inquiry.dto.InquiryDTO;
import com.study.app.domains.storage.UploadService;

@Service
public class InquiryService {

    @Autowired
    private InquiryDAO inquiryDAO;
    
    @Autowired
    private InquiryAnswerDAO inquiryAnswerDAO;

    @Autowired
    private UploadService uploadService;

    /**
     * [관리자] 문의 답변 등록 및 상태 변경
     */
    @Transactional
    public void saveInquiryAnswer(InquiryAnswerDTO answerDTO) {
        // 1. 답변 저장
    		inquiryAnswerDAO.insertInquiryAnswer(answerDTO);

        // 2. 문의 상태 업데이트 (PENDING -> ANSWERED)
        Map<String, Object> params = new HashMap<>();
        params.put("inquiry_id", answerDTO.getInquiry_id());
        params.put("status", "ANSWERED");
        inquiryDAO.updateInquiryStatus(params);
    }

    /**
     * 문의 등록 및 파일 업로드 처리
     */
    @Transactional
    public void addInquiry(InquiryDTO inquiryDTO) throws IOException {
        // 1. 문의 본문 저장
        inquiryDAO.insertInquiry(inquiryDTO);
        
        // 2. 파일 처리
        List<MultipartFile> files = inquiryDTO.getFiles();
        if (files != null && !files.isEmpty()) {
            List<InquiryAttachmentDTO> attachments = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String filePath = uploadService.upload(file, "inquiry/file");
                    
                    InquiryAttachmentDTO attachDTO = new InquiryAttachmentDTO();
                    attachDTO.setInquiry_id(inquiryDTO.getInquiry_id());
                    attachDTO.setFile_name(file.getOriginalFilename());
                    attachDTO.setFile_path(filePath);
                    attachDTO.setFile_size(file.getSize());
                    attachDTO.setFile_type(file.getContentType());
                    
                    attachments.add(attachDTO);
                }
            }
            if (!attachments.isEmpty()) {
                inquiryDAO.insertAttachments(attachments);
            }
        }
    }

    /**
     * 내 문의 리스트 조회
     */
    public List<InquiryDTO> getMyInquiryList(String memberId) {
        return inquiryDAO.getMyInquiryList(memberId);
    }

    /**
     * 문의 상세 조회
     */
    public InquiryDTO inquiryDetail(Long inquiryId) {
        InquiryDTO dto = inquiryDAO.inquiryDetail(inquiryId);
        if (dto != null) {
            List<InquiryAttachmentDTO> attachments = inquiryDAO.selectAttachmentsByInquiryId(inquiryId);
            dto.setAttachments(attachments);
            InquiryAnswerDTO iadto = inquiryAnswerDAO.getAnswerByInquiryId(inquiryId);
            dto.setAnswer(iadto);
        }
        return dto;
    }

    /**
     * 문의 수정
     */
    @Transactional
    public void updateInquiry(Long inquiryId, InquiryDTO inquiryDTO) throws IOException {
        // 1. 기존 데이터 조회 및 권한 확인
        InquiryDTO existing = inquiryDAO.selectInquiryById(inquiryId);
        if (existing == null) {
            throw new RuntimeException("해당 문의를 찾을 수 없습니다.");
        }
        
        // 2. 본문 업데이트 전, 본문에서 삭제된 이미지 처리
        uploadService.deleteRemovedImages(existing.getContent(), inquiryDTO.getContent());
        
        // 3. 본문 업데이트 (카테고리, 제목, 내용)
        inquiryDTO.setInquiry_id(inquiryId);
        inquiryDAO.updateInquiry(inquiryDTO);

        // 4. 명시적으로 삭제 요청된 첨부파일 처리
        if (inquiryDTO.getDeleteFileIds() != null) {
            for (Long attachId : inquiryDTO.getDeleteFileIds()) {
                InquiryAttachmentDTO attach = inquiryDAO.selectAttachmentById(attachId);
                if (attach != null) {
                    // GCP 파일 삭제
                    uploadService.deleteFile(attach.getFile_path());
                    // DB 삭제
                    inquiryDAO.deleteAttachmentById(attachId);
                }
            }
        }

        // 5. 새로운 파일 추가
        List<MultipartFile> files = inquiryDTO.getFiles();
        if (files != null && !files.isEmpty()) {
            List<InquiryAttachmentDTO> attachments = new ArrayList<>();
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String filePath = uploadService.upload(file, "inquiry/file");
                    
                    InquiryAttachmentDTO attachDTO = new InquiryAttachmentDTO();
                    attachDTO.setInquiry_id(inquiryId);
                    attachDTO.setFile_name(file.getOriginalFilename());
                    attachDTO.setFile_path(filePath);
                    attachDTO.setFile_size(file.getSize());
                    attachDTO.setFile_type(file.getContentType());
                    
                    attachments.add(attachDTO);
                }
            }
            if (!attachments.isEmpty()) {
                inquiryDAO.insertAttachments(attachments);
            }
        }
    }

    /**
     * 문의 삭제 (GCP 파일 + DB 데이터)
     */
    @Transactional
    public void deleteInquiry(Long inquiryId, String memberId) {
        // 1. 본인 확인 및 데이터 조회
        InquiryDTO inquiry = inquiryDAO.selectInquiryById(inquiryId);
        if (inquiry == null) {
            throw new RuntimeException("해당 문의를 찾을 수 없습니다.");
        }
        if (!inquiry.getMember_id().equals(memberId)) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        // 2. GCP 실제 파일 삭제 (DB 삭제 전에 수행)
        // 2-1. 첨부파일 삭제
        List<InquiryAttachmentDTO> attachments = inquiryDAO.selectAttachmentsByInquiryId(inquiryId);
        if (attachments != null) {
            for (InquiryAttachmentDTO attach : attachments) {
                uploadService.deleteFile(attach.getFile_path());
            }
        }
        
        // 2-2. 본문 삽입 이미지 삭제
        uploadService.deleteImagesFromContent(inquiry.getContent());

        // 3. DB 데이터 삭제 (자식 테이블 -> 부모 테이블)
        inquiryDAO.deleteAttachmentsByInquiryId(inquiryId);
        inquiryDAO.deleteInquiry(inquiryId);
    }
    
    public List<InquiryDTO> getInquiryList(){
    		return inquiryDAO.getInquiryList();
    }
}
