package com.study.app.domains.inquiry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.study.app.domains.inquiry.dto.InquiryAttachmentDTO;
import com.study.app.domains.inquiry.dto.InquiryDTO;
import com.study.app.domains.storage.uploadService;

@Service
public class InquiryService {

    @Autowired
    private InquiryDAO inquiryDAO;

    @Autowired
    private uploadService uploadService;

    /**
     * 문의 등록 및 파일 업로드 처리
     */
    @Transactional
    public void addInquiry(InquiryDTO inquiryDTO) throws IOException {
        
        // 1. 문의 본문 저장 (MyBatis selectKey에 의해 inquiry_id가 DTO에 채워짐)
        inquiryDAO.insertInquiry(inquiryDTO);
        
        // 2. 파일 처리
        List<MultipartFile> files = inquiryDTO.getFiles();
        if (files != null && !files.isEmpty()) {
            List<InquiryAttachmentDTO> attachments = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    // GCP 업로드 (inquiry 폴더)
                    String filePath = uploadService.upload(file, "inquiry");
                    
                    // 첨부파일 DTO 생성 및 리스트 추가
                    InquiryAttachmentDTO attachDTO = new InquiryAttachmentDTO();
                    attachDTO.setInquiry_id(inquiryDTO.getInquiry_id()); // 외래키 설정
                    attachDTO.setFile_name(file.getOriginalFilename());
                    attachDTO.setFile_path(filePath);
                    attachDTO.setFile_size(file.getSize());
                    attachDTO.setFile_type(file.getContentType());
                    
                    attachments.add(attachDTO);
                }
            }
            
            // 3. 첨부파일 정보 DB 저장
            if (!attachments.isEmpty()) {
                inquiryDAO.insertAttachments(attachments);
            }
        }
    }
    // 유저별 문의내역 리스트
    public List<InquiryDTO> getMyInquiryList(String memberId){
    		return inquiryDAO.getMyInquiryList(memberId);
    }
    // 문의내역 디테일
    public InquiryDTO inquiryDetail(Long inquiryId) {
    		return inquiryDAO.inquiryDetail(inquiryId);
    }
}
