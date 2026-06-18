package com.study.app.domains.faq;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.admin.dto.FaqDTO;

@Service
public class FaqService {
	
	@Autowired
	private FaqDAO faqDAO;
	
	public List<FaqDTO> getFaqList(){
		return faqDAO.getFaqList();
	}
	
	public void addFaq(FaqDTO dto) {
		faqDAO.addFaq(dto);
	}
	
	@Transactional
	public void deleteFaq(Long faq_id) {
		FaqDTO faq = faqDAO.getFaqById(faq_id);
		if(faq != null) {
			faqDAO.deleteFaq(faq_id);
			faqDAO.updateOrdersOnDelete(faq.getDisplay_order());
		}
	}
	
	@Transactional
	public void updateFaq(FaqDTO dto) {
		FaqDTO oldFaq = faqDAO.getFaqById(dto.getFaq_id());
		if(oldFaq != null) {
			Long oldOrder = oldFaq.getDisplay_order();
			Long newOrder = dto.getDisplay_order();
			
			if(!oldOrder.equals(newOrder)) {
				faqDAO.shiftOrdersForUpdate(oldOrder, newOrder);
			}
			faqDAO.updateFaq(dto);
		}
	}

}
