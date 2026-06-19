package com.study.app.domains.admin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.study.app.domains.board.dto.CommentReportDTO;
import com.study.app.domains.board.dto.PostCommentDTO;

@Repository
public class AdminCommentDAO {


	private static final String NAMESPACE = "AdminComment.";

	private final SqlSessionTemplate mybatis;

	public AdminCommentDAO(SqlSessionTemplate mybatis) {
		this.mybatis = mybatis;
	}

	public Map<String, Object> selectSummary() {
		return mybatis.selectOne(
				NAMESPACE + "selectSummary"
				);
	}

	public List<PostCommentDTO> selectComments(
			Map<String, Object> params
			) {
		return mybatis.selectList(
				NAMESPACE + "selectComments",
				params
				);
	}

	public int countComments(Map<String, Object> params) {
		return mybatis.selectOne(
				NAMESPACE + "countComments",
				params
				);
	}

	public List<Map<String, Object>> selectWaitingReports(
			Map<String, Object> params
			) {
		return mybatis.selectList(
				NAMESPACE + "selectWaitingReports",
				params
				);
	}

	public int countWaitingReports() {
		return mybatis.selectOne(
				NAMESPACE + "countWaitingReports"
				);
	}

	public PostCommentDTO selectCommentDetail(Long commentId) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentId", commentId);

		return mybatis.selectOne(
				NAMESPACE + "selectCommentDetail",
				params
				);
	}

	public List<CommentReportDTO> selectReportsByCommentId(
			Long commentId
			) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentId", commentId);

		return mybatis.selectList(
				NAMESPACE + "selectReportsByCommentId",
				params
				);
	}

	public int updateReportStatus(
			Map<String, Object> params
			) {
		return mybatis.update(
				NAMESPACE + "updateReportStatus",
				params
				);
	}

	public void lockHistoryTable() {
		mybatis.update(
				NAMESPACE + "lockHistoryTable"
				);
	}

	public int insertReportHistory(
			Map<String, Object> params
			) {
		return mybatis.insert(
				NAMESPACE + "insertReportHistory",
				params
				);
	}

	public List<Long> selectCommentTreeIds(
			List<Long> rootCommentIds
			) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentIds", rootCommentIds);

		return mybatis.selectList(
				NAMESPACE + "selectCommentTreeIds",
				params
				);
	}

	public int deleteCommentReportsByIds(
			List<Long> commentIds
			) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentIds", commentIds);

		return mybatis.delete(
				NAMESPACE + "deleteCommentReportsByIds",
				params
				);
	}

	public int deleteCommentLikesByIds(
			List<Long> commentIds
			) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentIds", commentIds);

		return mybatis.delete(
				NAMESPACE + "deleteCommentLikesByIds",
				params
				);
	}

	public int deleteCommentsByIds(
			List<Long> commentIds
			) {
		Map<String, Object> params = new HashMap<>();
		params.put("commentIds", commentIds);

		return mybatis.delete(
				NAMESPACE + "deleteCommentsByIds",
				params
				);
	}

}
