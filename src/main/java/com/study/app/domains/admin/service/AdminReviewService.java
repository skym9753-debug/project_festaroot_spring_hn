package com.study.app.domains.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.study.app.domains.review.ReviewDAO;
import com.study.app.domains.review.dto.FestivalReviewDTO;

import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminReviewService {

    @Autowired
    private ReviewDAO reviewDAO;

    public Map<String, Object> getAdminReviews(int page, int size, String status, String rating, String keyword) {
        int startRow = (page - 1) * size + 1;
        int endRow = page * size;
        Map<String, Object> params = new HashMap<>();
        params.put("startRow", startRow);
        params.put("endRow", endRow);
        params.put("status", status);
        params.put("rating", rating);
        params.put("keyword", keyword);

        int totalCount = reviewDAO.countAdminReviews(params);
        List<FestivalReviewDTO> list = reviewDAO.selectAdminReviews(params);
        if (list != null) {
            for (FestivalReviewDTO review : list) {
                review.setImages(reviewDAO.selectReviewImages(review.getReview_id()));
                if (review.getReport_count() != null && review.getReport_count() >= 1) {
                    review.setReports(reviewDAO.selectReportsByReviewId(review.getReview_id()));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pageInfo", getPageInfo(page, size, totalCount));
        return result;
    }

    @Transactional
    public boolean updateReviewStatus(Long reviewId, String isDeleted) {
        return reviewDAO.updateReviewStatus(reviewId, isDeleted) > 0;
    }

    @Transactional
    public boolean dismissReviewReports(Long reviewId) {
        reviewDAO.dismissReviewReportsCount(reviewId);
        reviewDAO.deleteReportsByReviewId(reviewId);
        return true;
    }

    @Transactional
    public boolean deleteReview(Long reviewId) {
        reviewDAO.deleteReviewImages(reviewId);
        reviewDAO.deleteReportsByReviewId(reviewId);
        return reviewDAO.deleteReviewPermanently(reviewId) > 0;
    }

    // Pagination helper (similar to GatheringService)
    private Map<String, Object> getPageInfo(int currentPage, int size, int totalCount) {
        int pageBlock = 5;
        int totalPage = (int) Math.ceil((double) totalCount / size);
        int endPage = (int) (Math.ceil(currentPage / (double) pageBlock) * pageBlock);
        int startPage = endPage - pageBlock + 1;
        if (endPage > totalPage) endPage = totalPage;
        if (startPage < 1) startPage = 1;
        boolean existPrev = startPage > 1;
        boolean existNext = endPage < totalPage;
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("currentPage", currentPage);
        pageInfo.put("startPage", startPage);
        pageInfo.put("endPage", endPage);
        pageInfo.put("totalPage", totalPage);
        pageInfo.put("totalCount", totalCount);
        pageInfo.put("existPrev", existPrev);
        pageInfo.put("existNext", existNext);
        return pageInfo;
    }
}
