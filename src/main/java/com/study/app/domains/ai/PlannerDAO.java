package com.study.app.domains.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.study.app.domains.ai.dto.AIPlannerDTO;
import com.study.app.domains.ai.dto.AIPlannerStepDTO;

@Repository
public class PlannerDAO {

    private final SqlSessionTemplate mybatis;

    public PlannerDAO(SqlSessionTemplate mybatis) {
        this.mybatis = mybatis;
    }

    /**
     * AI 플래너 마스터 저장
     * - seq_ai_planner.NEXTVAL로 planner_id 생성
     * - 생성된 planner_id는 DTO에 다시 세팅됨
     */
    public int insertPlanner(AIPlannerDTO dto) {
        return mybatis.insert("Planner.insertPlanner", dto);
    }

    /**
     * AI 플래너 step 저장
     * - seq_ai_planner_step.NEXTVAL로 step_id 생성
     */
    public int insertPlannerStep(AIPlannerStepDTO dto) {
        return mybatis.insert("Planner.insertPlannerStep", dto);
    }

    /**
     * 로그인한 사용자의 AI 플래너 목록 조회
     */
    public List<AIPlannerDTO> selectMyPlanners(String member_id) {
        return mybatis.selectList("Planner.selectMyPlanners", member_id);
    }

    /**
     * AI 플래너 상세 조회
     * - 본인 플래너만 조회되도록 planner_id + member_id 조건 사용
     */
    public AIPlannerDTO selectPlannerById(Long planner_id, String member_id) {
        Map<String, Object> params = new HashMap<>();
        params.put("planner_id", planner_id);
        params.put("member_id", member_id);

        return mybatis.selectOne("Planner.selectPlannerById", params);
    }

    /**
     * AI 플래너 step 목록 조회
     * - step_order 기준 정렬은 mapper에서 처리
     */
    public List<AIPlannerStepDTO> selectPlannerSteps(Long planner_id) {
        return mybatis.selectList("Planner.selectPlannerSteps", planner_id);
    }

    /**
     * AI 플래너 삭제
     * - ai_planner_step은 FK ON DELETE CASCADE로 같이 삭제됨
     */
    public int deletePlanner(Long planner_id, String member_id) {
        Map<String, Object> params = new HashMap<>();
        params.put("planner_id", planner_id);
        params.put("member_id", member_id);

        return mybatis.delete("Planner.deletePlanner", params);
    }
}