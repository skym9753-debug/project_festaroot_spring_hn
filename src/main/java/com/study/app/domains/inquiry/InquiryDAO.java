package com.study.app.domains.inquiry;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.study.app.domains.inquiry.dto.InquiryAttachmentDTO;
import com.study.app.domains.inquiry.dto.InquiryDTO;

@Mapper
public interface InquiryDAO {
    
    /**
     * 문의글 본문 저장 (User_inquiry 테이블)
     */
    int insertInquiry(InquiryDTO dto);
    
    /**
     * 첨부파일 정보 저장 (Inquiry_attachment 테이블)
     */
    int insertAttachments(List<InquiryAttachmentDTO> list);
    
    List<InquiryDTO> getMyInquiryList(String member_id);
    
    InquiryDTO inquiryDetail(Long inquiry_id);
}
