package com.study.app.domains.festival;

import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TourPortalDAO {
	
    @Autowired
    private SqlSessionTemplate mybatis;

    public void deleteStaging() {
        mybatis.delete("tourPortal.deleteStaging");
    }

    public int insertStaging(Map<String, Object> param) {
        return mybatis.insert("tourPortal.insertStaging", param);
    }

    public int updateRegionMaster() {
        return mybatis.update("tourPortal.updateRegionMaster");
    }

}
