package com.study.app.domains.admin.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.admin.dto.AdminPostDTO;
import com.study.app.domains.board.dto.PostAttachmentDTO;
import com.study.app.domains.board.dto.PostReportDTO;

@Repository
public class AdminPostDAO {
	
	@Autowired
	private SqlSessionTemplate mybatis;
	
    // mapper XML의 namespace
    private static final String NAMESPACE = "AdminPost";
    
    public Map<String, Object> selectSummary() {
        return mybatis.selectOne(NAMESPACE + ".selectSummary");
    }

    public List<AdminPostDTO> selectPosts(Map<String, Object> params) {
        return mybatis.selectList(NAMESPACE + ".selectPosts", params);
    }

    public int countPosts(Map<String, Object> params) {
        return mybatis.selectOne(NAMESPACE + ".countPosts", params);
    }

    public List<PostReportDTO> selectWaitingReports(
            Map<String, Object> params
    ) {
        return mybatis.selectList(
                NAMESPACE + ".selectWaitingReports",
                params
        );
    }

    public int countWaitingReports() {
        return mybatis.selectOne(NAMESPACE + ".countWaitingReports");
    }

    public AdminPostDTO selectPostDetail(Long postId) {
        return mybatis.selectOne(
                NAMESPACE + ".selectPostDetail",
                postId
        );
    }

    public List<PostReportDTO> selectReportsByPostId(Long postId) {
        return mybatis.selectList(
                NAMESPACE + ".selectReportsByPostId",
                postId
        );
    }

    public List<PostAttachmentDTO> selectAttachmentsByPostId(Long postId) {
        return mybatis.selectList(
                NAMESPACE + ".selectAttachmentsByPostId",
                postId
        );
    }

    public void lockHistoryTable() {
        mybatis.update(NAMESPACE + ".lockHistoryTable");
    }

    public int updateReportStatus(Map<String, Object> params) {
        return mybatis.update(
                NAMESPACE + ".updateReportStatus",
                params
        );
    }

    public int insertReportHistory(Map<String, Object> params) {
        return mybatis.insert(
                NAMESPACE + ".insertReportHistory",
                params
        );
    }

    public List<String> selectAttachmentPathsByPostId(Long postId) {
        return mybatis.selectList(
                NAMESPACE + ".selectAttachmentPathsByPostId",
                postId
        );
    }

    public int deleteCommentReportsByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deleteCommentReportsByPostId",
                postId
        );
    }

    public int deleteCommentLikesByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deleteCommentLikesByPostId",
                postId
        );
    }

    public int deleteCommentsByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deleteCommentsByPostId",
                postId
        );
    }

    public int deletePostLikesByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deletePostLikesByPostId",
                postId
        );
    }

    public int deleteAttachmentsByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deleteAttachmentsByPostId",
                postId
        );
    }

    public int deletePostReportsByPostId(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deletePostReportsByPostId",
                postId
        );
    }

    public int deletePostById(Long postId) {
        return mybatis.delete(
                NAMESPACE + ".deletePostById",
                postId
        );
    }

}
