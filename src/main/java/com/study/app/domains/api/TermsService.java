package com.study.app.domains.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TermsService {
	
	@Autowired
    private TermsDAO termsDAO;

    public List<TermsDTO> selectActiveTerms() {
        return termsDAO.selectActiveTerms();
    }

}
