package com.study.app.domains.admin.dto;

import java.util.List;

public class AdminMemberDTO {

	// 검색 필터 쿼리 파라미터를 담을 바인딩 객체
	public static class SearchParam {
		private String keyword;
		private String role;
		private String status;
		private String sortBy;
		private String startDate;
		private String endDate;
		private int page = 1; // 프론트 기본값 1
		private int size = 10;

		// Oracle ROWNUM 또는 OFFSET 변환을 위한 getter
		public int getOffset() {
			return (this.page - 1) * this.size;
		}

		@Override
		public String toString() {
			return "SearchParam [keyword=" + keyword + ", role=" + role + ", status=" + status + ", sortBy=" + sortBy
					+ ", startDate=" + startDate + ", endDate=" + endDate + ", page=" + page + ", size=" + size + "]";
		}

		public SearchParam() {
		}

		public SearchParam(String keyword, String role, String status, String sortBy, String startDate, String endDate,
				int page, int size) {
			super();
			this.keyword = keyword;
			this.role = role;
			this.status = status;
			this.sortBy = sortBy;
			this.startDate = startDate;
			this.endDate = endDate;
			this.page = page;
			this.size = size;
		}

		public String getKeyword() {
			return keyword;
		}

		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getSortBy() {
			return sortBy;
		}

		public void setSortBy(String sortBy) {
			this.sortBy = sortBy;
		}

		public String getStartDate() {
			return startDate;
		}

		public void setStartDate(String startDate) {
			this.startDate = startDate;
		}

		public String getEndDate() {
			return endDate;
		}

		public void setEndDate(String endDate) {
			this.endDate = endDate;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getSize() {
			return size;
		}

		public void setSize(int size) {
			this.size = size;
		}

	}

	// -------------------------------------------

	// 정지 기간 요청용 DTO
	public static class SuspendRequest {
		private int suspensionDays;

		public int getSuspensionDays() {
			return suspensionDays;
		}

		@Override
		public String toString() {
			return "SuspendRequest [suspensionDays=" + suspensionDays + "]";
		}

		public SuspendRequest() {
		}

		public SuspendRequest(int suspensionDays) {
			super();
			this.suspensionDays = suspensionDays;
		}

		public void setSuspensionDays(int suspensionDays) {
			this.suspensionDays = suspensionDays;
		}
	}

	// -------------------------------------------

	// 클라이언트에 반환할 회원 상세 정보 포맷 DTO
	public static class Response {
		private String id;
		private String nickname;
		private String email;
		private String role;
		private String status;
		private String provider;
		private String joinedAt;
		private String lastLogin;
		private int reports;
		private String reportReason;
		private String lastReportDate;
		private String resultStatus;
		private String suspensionEndDate;

		@Override
		public String toString() {
			return "Response [id=" + id + ", nickname=" + nickname + ", email=" + email + ", role=" + role + ", status="
					+ status + ", provider=" + provider + ", joinedAt=" + joinedAt + ", lastLogin=" + lastLogin
					+ ", reports=" + reports + ", reportReason=" + reportReason + ", lastReportDate=" + lastReportDate
					+ ", resultStatus=" + resultStatus + ", suspensionEndDate=" + suspensionEndDate + "]";
		}

		public Response() {
		}

		public Response(String id, String nickname, String email, String role, String status, String provider,
				String joinedAt, String lastLogin, int reports, String reportReason, String lastReportDate,
				String resultStatus, String suspensionEndDate) {
			super();
			this.id = id;
			this.nickname = nickname;
			this.email = email;
			this.role = role;
			this.status = status;
			this.provider = provider;
			this.joinedAt = joinedAt;
			this.lastLogin = lastLogin;
			this.reports = reports;
			this.reportReason = reportReason;
			this.lastReportDate = lastReportDate;
			this.resultStatus = resultStatus;
			this.suspensionEndDate = suspensionEndDate;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getRole() {
			return role;
		}

		public void setRole(String role) {
			this.role = role;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = provider;
		}

		public String getJoinedAt() {
			return joinedAt;
		}

		public void setJoinedAt(String joinedAt) {
			this.joinedAt = joinedAt;
		}

		public String getLastLogin() {
			return lastLogin;
		}

		public void setLastLogin(String lastLogin) {
			this.lastLogin = lastLogin;
		}

		public int getReports() {
			return reports;
		}

		public void setReports(int reports) {
			this.reports = reports;
		}

		public String getReportReason() {
			return reportReason;
		}

		public void setReportReason(String reportReason) {
			this.reportReason = reportReason;
		}

		public String getLastReportDate() {
			return lastReportDate;
		}

		public void setLastReportDate(String lastReportDate) {
			this.lastReportDate = lastReportDate;
		}

		public String getResultStatus() {
			return resultStatus;
		}

		public void setResultStatus(String resultStatus) {
			this.resultStatus = resultStatus;
		}

		public String getSuspensionEndDate() {
			return suspensionEndDate;
		}

		public void setSuspensionEndDate(String suspensionEndDate) {
			this.suspensionEndDate = suspensionEndDate;
		}
	}

	// --------------------------------------------------------

	// 특정 회원의 승인된 '신고 이력' 상세 반환용 DTO
	public static class ReportHistoryResponse {
		private String reportType; // POST, COMMENT 등
		private String reason; // AD_SPAM, ABUSE_SLANDER 등
		private String adminMemo; // 게시글 관리자가 쓴 메모
		private String createdAt; // 신고 승인 처리된 날짜

		@Override
		public String toString() {
			return "ReportHistoryResponse [reportType=" + reportType + ", reason=" + reason + ", adminMemo=" + adminMemo
					+ ", createdAt=" + createdAt + "]";
		}

		public ReportHistoryResponse() {
		}

		public ReportHistoryResponse(String reportType, String reason, String adminMemo, String createdAt) {
			super();
			this.reportType = reportType;
			this.reason = reason;
			this.adminMemo = adminMemo;
			this.createdAt = createdAt;
		}

		public String getReportType() {
			return reportType;
		}

		public void setReportType(String reportType) {
			this.reportType = reportType;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public String getAdminMemo() {
			return adminMemo;
		}

		public void setAdminMemo(String adminMemo) {
			this.adminMemo = adminMemo;
		}

		public String getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
		}
	}

	// -------------------------------------------------

	// 고정되어있는 회원관리 정보
	public static class MainStats {
		private long total;
		private long newToday;
		private long suspended;
		private long blacklisted;

		public MainStats() {
		}

		public MainStats(long total, long newToday, long suspended, long blacklisted) {
			this.total = total;
			this.newToday = newToday;
			this.suspended = suspended;
			this.blacklisted = blacklisted;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

		public long getNewToday() {
			return newToday;
		}

		public void setNewToday(long newToday) {
			this.newToday = newToday;
		}

		public long getSuspended() {
			return suspended;
		}

		public void setSuspended(long suspended) {
			this.suspended = suspended;
		}

		public long getBlacklisted() {
			return blacklisted;
		}

		public void setBlacklisted(long blacklisted) {
			this.blacklisted = blacklisted;
		}
	}

	// --------------------------------------------------------

	// 회원 관리 신고 사유 상세보기

	public static class MemberDetailResponse {
		private Response memberInfo;
		private List<ReportItem> reportHistory;

		public MemberDetailResponse() {
		}

		public MemberDetailResponse(Response memberInfo, List<ReportItem> reportHistory) {
			this.memberInfo = memberInfo;
			this.reportHistory = reportHistory;
		}

		public Response getMemberInfo() {
			return memberInfo;
		}

		public void setMemberInfo(Response memberInfo) {
			this.memberInfo = memberInfo;
		}

		public List<ReportItem> getReportHistory() {
			return reportHistory;
		}

		public void setReportHistory(List<ReportItem> reportHistory) {
			this.reportHistory = reportHistory;
		}
	}

	public static class ReportItem {
		private Long historyId;
		private Long reportId; // 원본 신고 번호
		private String reporterId;
		private String reportType;
		private String reason;
		private String resultStatus;
		private String adminMemo;
		private String createdAt;

		public ReportItem() {
		}

		public ReportItem(Long historyId, Long reportId, String reporterId, String reportType, String reason,
				String resultStatus, String adminMemo, String createdAt) {
			this.historyId = historyId;
			this.reportId = reportId;
			this.reporterId = reporterId;
			this.reportType = reportType;
			this.reason = reason;
			this.resultStatus = resultStatus;
			this.adminMemo = adminMemo;
			this.createdAt = createdAt;
		}

		public Long getHistoryId() {
			return historyId;
		}

		public void setHistoryId(Long historyId) {
			this.historyId = historyId;
		}

		public Long getReportId() {
			return reportId;
		}

		public void setReportId(Long reportId) {
			this.reportId = reportId;
		}

		public String getReporterId() {
			return reporterId;
		}

		public void setReporterId(String reporterId) {
			this.reporterId = reporterId;
		}

		public String getReportType() {
			return reportType;
		}

		public void setReportType(String reportType) {
			this.reportType = reportType;
		}

		public String getReason() {
			return reason;
		}

		public void setReason(String reason) {
			this.reason = reason;
		}

		public String getResultStatus() {
			return resultStatus;
		}

		public void setResultStatus(String resultStatus) {
			this.resultStatus = resultStatus;
		}

		public String getAdminMemo() {
			return adminMemo;
		}

		public void setAdminMemo(String adminMemo) {
			this.adminMemo = adminMemo;
		}

		public String getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(String createdAt) {
			this.createdAt = createdAt;
		}
	}
}
