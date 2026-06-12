package com.study.app.domains.board.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.board.dao.PostDAO;
import com.study.app.domains.board.dao.PostReportDAO;
import com.study.app.domains.board.dto.PostReportDTO;

@Service
public class PostReportService {
	
    @Autowired
    private PostReportDAO reportDAO;
    
    @Autowired
    private PostDAO postDAO;

    public boolean addReport(PostReportDTO dto) {
        int count = reportDAO.countReport(dto);

        if (count > 0) {
            return false;
        }
        
        postDAO.increaseReportCount(dto.getPost_id());

        int result = reportDAO.insertReport(dto);

        return result > 0;
    }

}
