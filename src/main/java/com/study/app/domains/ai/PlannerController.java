package com.study.app.domains.ai;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.app.domains.ai.dto.AIPlannerDTO;
import com.study.app.utils.JWTUtil;

@RestController
@RequestMapping("/ai")
public class PlannerController {

    private final PlannerService plannerService;
    private final JWTUtil jwtUtil;

    public PlannerController(PlannerService plannerService, JWTUtil jwtUtil) {
        this.plannerService = plannerService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authorization 헤더에서 JWT 토큰을 꺼내 member_id 추출
     */
    private String resolveMemberId(String authorization) {
        if (authorization == null || authorization.trim().isEmpty()) {
            return null;
        }

        String token = authorization.trim();

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            return jwtUtil.getSubject(token);
        } catch (Exception e) {
            System.out.println("JWT 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    /**
     * 인증 실패 응답 공통 처리
     */
    private ResponseEntity<Map<String, Object>> unauthorizedResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "인증이 필요합니다.");
        return ResponseEntity.status(401).body(result);
    }

    /**
     * AI 축제 코스 추천 생성
     *
     * POST /ai/planner
     */
    @PostMapping("/planner")
    public ResponseEntity<Map<String, Object>> createPlanner(
            @RequestBody AIPlannerDTO plannerDTO,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String memberId = resolveMemberId(authorization);

        if (memberId == null) {
            return unauthorizedResponse();
        }

        Map<String, Object> result = plannerService.createPlannerWithDummySteps(plannerDTO, memberId);

        /*
         * success=false는 서버 에러가 아니라
         * 입력값 검증 실패, 축제 기간 불일치, 축제 정보 없음 같은 업무 로직 실패이므로
         * 500이 아니라 200으로 내려준다.
         */
        return ResponseEntity.ok(result);
    }

    /**
     * 내 AI 플래너 목록 조회
     *
     * GET /ai/planners/my
     */
    @GetMapping("/planners/my")
    public ResponseEntity<Map<String, Object>> getMyPlanners(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String memberId = resolveMemberId(authorization);

        if (memberId == null) {
            return unauthorizedResponse();
        }

        List<AIPlannerDTO> list = plannerService.getMyPlanners(memberId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", list);
        result.put("count", list.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 기존 프론트가 /ai/planners/mypage를 쓰고 있다면 호환용으로 유지
     *
     * GET /ai/planners/mypage
     */
    @GetMapping("/planners/mypage")
    public ResponseEntity<Map<String, Object>> getMyPlannersForMypage(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return getMyPlanners(authorization);
    }

    /**
     * AI 플래너 상세 조회
     *
     * GET /ai/planners/{plannerId}
     */
    @GetMapping("/planners/{plannerId}")
    public ResponseEntity<Map<String, Object>> getPlannerById(
            @PathVariable Long plannerId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String memberId = resolveMemberId(authorization);

        if (memberId == null) {
            return unauthorizedResponse();
        }

        HashMap<String, Object> planner = plannerService.getPlannerById(plannerId, memberId);

        if (planner == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "해당 플래너를 찾을 수 없습니다.");
            return ResponseEntity.status(404).body(result);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", planner);

        return ResponseEntity.ok(result);
    }

    /**
     * AI 플래너 삭제
     *
     * DELETE /ai/planners/{plannerId}
     */
    @DeleteMapping("/planners/{plannerId}")
    public ResponseEntity<Map<String, Object>> deletePlanner(
            @PathVariable Long plannerId,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String memberId = resolveMemberId(authorization);

        if (memberId == null) {
            return unauthorizedResponse();
        }

        int deleted = plannerService.deletePlanner(plannerId, memberId);

        if (deleted == 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "삭제할 플래너가 없거나 권한이 없습니다.");
            return ResponseEntity.status(403).body(result);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "플래너가 삭제되었습니다.");

        return ResponseEntity.ok(result);
    }
}