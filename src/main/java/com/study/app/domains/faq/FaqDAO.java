package com.study.app.domains.faq;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.admin.dto.FaqDTO;

@Repository
public class FaqDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
	public List<FaqDTO> getFaqList(){
		return mybatis.selectList("Faq.getFaqList");
	}
	
	public void addFaq(FaqDTO dto) {
		mybatis.insert("Faq.addFaq", dto);
	}
	
	public FaqDTO getFaqById(Long faq_id) {
		return mybatis.selectOne("Faq.getFaqById", faq_id);
	}
	
	public void deleteFaq(Long faq_id) {
		mybatis.delete("Faq.deleteFaq", faq_id);
	}
	
	public void updateFaq(FaqDTO dto) {
		mybatis.update("Faq.updateFaq", dto);
	}
	
	public void updateOrdersOnDelete(Long display_order) {
		mybatis.update("Faq.updateOrdersOnDelete", display_order);
	}
	
	public void shiftOrdersForUpdate(Long oldOrder, Long newOrder) {
		java.util.Map<String, Object> params = new java.util.HashMap<>();
		params.put("oldOrder", oldOrder);
		params.put("newOrder", newOrder);
		mybatis.update("Faq.shiftOrdersForUpdate", params);
	}
	
}
