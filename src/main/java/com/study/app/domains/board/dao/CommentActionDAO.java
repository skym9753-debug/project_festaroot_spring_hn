package com.study.app.domains.board.dao;

import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CommentActionDAO {
	
    @Autowired
    private SqlSessionTemplate mybatis;

    // 댓글 좋아요 여부 확인
    public int countCommentLike(Long comment_id, String member_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("comment_id", comment_id);
        param.put("member_id", member_id);

        return mybatis.selectOne("CommentAction.countCommentLike", param);
    }

    // 댓글 좋아요 추가
    public int insertCommentLike(Long comment_id, String member_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("comment_id", comment_id);
        param.put("member_id", member_id);

        return mybatis.insert("CommentAction.insertCommentLike", param);
    }

    // 댓글 좋아요 취소
    public int deleteCommentLike(Long comment_id, String member_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("comment_id", comment_id);
        param.put("member_id", member_id);

        return mybatis.delete("CommentAction.deleteCommentLike", param);
    }

    // 댓글 좋아요 수 증가
    public int increaseCommentLikeCount(Long comment_id) {
        return mybatis.update("CommentAction.increaseCommentLikeCount", comment_id);
    }

    // 댓글 좋아요 수 감소
    public int decreaseCommentLikeCount(Long comment_id) {
        return mybatis.update("CommentAction.decreaseCommentLikeCount", comment_id);
    }

    // 현재 댓글 좋아요 수 조회
    public Long getCommentLikeCount(Long comment_id) {
        return mybatis.selectOne("CommentAction.getCommentLikeCount", comment_id);
    }

    // 댓글 신고 중복 확인
    public int countCommentReport(Long comment_id, String member_id) {
        Map<String, Object> param = new HashMap<>();
        param.put("comment_id", comment_id);
        param.put("member_id", member_id);

        return mybatis.selectOne("CommentAction.countCommentReport", param);
    }

    // 댓글 신고 등록
    public int insertCommentReport(
            Long comment_id,
            String member_id,
            String reason
    ) {
        Map<String, Object> param = new HashMap<>();
        param.put("comment_id", comment_id);
        param.put("member_id", member_id);
        param.put("reason", reason);

        return mybatis.insert("CommentAction.insertCommentReport", param);
    }

    // 댓글 신고 수 증가
    public int increaseCommentReportCount(Long comment_id) {
        return mybatis.update("CommentAction.increaseCommentReportCount", comment_id);
    }

}
