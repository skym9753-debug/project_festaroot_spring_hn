package com.study.app.domains.admin.dto;

import java.util.List;

public class AdminPageResponseDTO {
    private List<AdminMemberDTO.Response> memberList; // Admin 전용 Response DTO로 수정
    private int totalPages;      // 전체 페이지 수
    private long totalElements;  // 전체 검색 결과 수
    private int currentPage;     // 현재 페이지 번호

    public AdminPageResponseDTO() {}

    public AdminPageResponseDTO(List<AdminMemberDTO.Response> memberList, int totalPages, long totalElements, int currentPage) {
        this.memberList = memberList;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
    }

    public List<AdminMemberDTO.Response> getMemberList() { return memberList; }
    public void setMemberList(List<AdminMemberDTO.Response> memberList) { this.memberList = memberList; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    @Override
    public String toString() {
        return "MemberPageResponseDTO [memberList=" + memberList + ", totalPages=" + totalPages 
                + ", totalElements=" + totalElements + ", currentPage=" + currentPage + "]";
    }
}