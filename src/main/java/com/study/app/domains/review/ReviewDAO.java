package com.study.app.domains.review;

import java.util.HashMap;
import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.study.app.domains.review.dto.FestivalReviewDTO;
import com.study.app.domains.review.dto.FestivalReviewImageDTO;
import com.study.app.domains.review.dto.ReviewReportDTO;

@Repository
public class ReviewDAO {
	
	@Autowired
    private SqlSessionTemplate mybatis;

    public int countAdminReviews(java.util.Map<String, Object> params) {
        return mybatis.selectOne("Review.countAdminReviews", params);
    }

    public List<com.study.app.domains.review.dto.FestivalReviewDTO> selectAdminReviews(java.util.Map<String, Object> params) {
        return mybatis.selectList("Review.selectAdminReviews", params);
    }

    public List<FestivalReviewDTO> selectReviews(Long content_id, String sortType) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("content_id", content_id);
        params.put("sortType", sortType);

        return mybatis.selectList("Review.selectReviews", params);
    }

    public FestivalReviewDTO selectReviewById(Long review_id) {
        return mybatis.selectOne("Review.selectReviewById", review_id);
    }

    public int insertReview(FestivalReviewDTO dto) {
        return mybatis.insert("Review.insertReview", dto);
    }

    public int updateReview(FestivalReviewDTO dto) {
        return mybatis.update("Review.updateReview", dto);
    }

    public int deleteReview(Long review_id, String member_id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("review_id", review_id);
        params.put("member_id", member_id);

        return mybatis.update("Review.deleteReview", params);
    }

    public List<FestivalReviewImageDTO> selectReviewImages(Long review_id) {
        return mybatis.selectList("Review.selectReviewImages", review_id);
    }

    public int insertReviewImage(FestivalReviewImageDTO imageDTO) {
        return mybatis.insert("Review.insertReviewImage", imageDTO);
    }

    public int deleteReviewImages(Long review_id) {
        return mybatis.delete("Review.deleteReviewImages", review_id);
    }

    public int countReportByMember(Long review_id, String member_id) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("review_id", review_id);
        params.put("member_id", member_id);

        return mybatis.selectOne("Review.countReportByMember", params);
    }

    public int insertReport(ReviewReportDTO dto) {
        return mybatis.insert("Review.insertReport", dto);
    }

    public int increaseReportCount(Long review_id) {
        return mybatis.update("Review.increaseReportCount", review_id);
    }

    public List<ReviewReportDTO> selectReportsByReviewId(Long review_id) {
        return mybatis.selectList("Review.selectReportsByReviewId", review_id);
    }

    public int updateReviewStatus(Long reviewId, String isDeleted) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("review_id", reviewId);
        params.put("is_deleted", isDeleted);
        return mybatis.update("Review.updateReviewStatus", params);
    }

    public int dismissReviewReportsCount(Long reviewId) {
        return mybatis.update("Review.dismissReviewReportsCount", reviewId);
    }

    public int deleteReportsByReviewId(Long reviewId) {
        return mybatis.delete("Review.deleteReportsByReviewId", reviewId);
    }

    public int deleteReviewPermanently(Long reviewId) {
        return mybatis.delete("Review.deleteReviewPermanently", reviewId);
    }
}
