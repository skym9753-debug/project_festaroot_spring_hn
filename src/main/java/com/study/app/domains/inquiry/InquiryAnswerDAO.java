package com.study.app.domains.inquiry;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.inquiry.dto.InquiryAnswerDTO;

@Repository
public class InquiryAnswerDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
	public int insertInquiryAnswer(InquiryAnswerDTO dto) {
		return mybatis.insert("InquiryAnswer.insertInquiryAnswer",dto);
	}
	public InquiryAnswerDTO getAnswerByInquiryId(Long inquiryId) {
		return mybatis.selectOne("InquiryAnswer.getAnswerByInquiryId",inquiryId);
	}
	public int updateAnswer(InquiryAnswerDTO dto) {
		return mybatis.update("InquiryAnswer.updateAnswer",dto);
	}

}
