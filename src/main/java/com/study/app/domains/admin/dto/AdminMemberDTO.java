package com.study.app.domains.admin.dto;

public class AdminMemberDTO {

	// 검색 필터 쿼리 파라미터를 담을 바인딩 객체
	public static class SearchParam {
		private String keyword;
		private String role;
		private String status;
		private String sortBy;
		private String startDate;
		private String endDate;

		@Override
		public String toString() {
			return "SearchParam [keyword=" + keyword + ", role=" + role + ", status=" + status + ", sortBy=" + sortBy
					+ ", startDate=" + startDate + ", endDate=" + endDate + "]";
		}

		public SearchParam() {
		}

		public SearchParam(String keyword, String role, String status, String sortBy, String startDate,
				String endDate) {
			super();
			this.keyword = keyword;
			this.role = role;
			this.status = status;
			this.sortBy = sortBy;
			this.startDate = startDate;
			this.endDate = endDate;
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

}
