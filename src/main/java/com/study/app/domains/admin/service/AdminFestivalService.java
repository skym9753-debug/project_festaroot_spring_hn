package com.study.app.domains.admin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.study.app.domains.festival.FestivalDAO;
import com.study.app.domains.festival.dto.FestivalDTO;

@Service
public class AdminFestivalService {
	
	@Autowired
	private FestivalDAO festivalDAO;
	
	public List<FestivalDTO> getAllFestivals(){
		return festivalDAO.getAllFestivalAdmin();
	}
	
	@Transactional
	public void updateVisibility(Long contentId, String isVisible) {
		festivalDAO.updateVisibility(contentId, isVisible);
	}
	
}
