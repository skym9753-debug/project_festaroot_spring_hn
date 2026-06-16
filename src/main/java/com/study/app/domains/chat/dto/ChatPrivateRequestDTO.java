package com.study.app.domains.chat.dto;

public class ChatPrivateRequestDTO {
	
	private Long currentUserId; // 1:1 채팅 신청인
	private Long targetMemberId;

	@Override
	public String toString() {
		return "ChatPrivateRequestDTO [currentUserId=" + currentUserId + ", targetMemberId=" + targetMemberId + "]";
	}

	public ChatPrivateRequestDTO() {}

	public ChatPrivateRequestDTO(Long currentUserId, Long targetMemberId) {
		super();
		this.currentUserId = currentUserId;
		this.targetMemberId = targetMemberId;
	}

	public Long getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(Long currentUserId) {
		this.currentUserId = currentUserId;
	}

	public Long getTargetMemberId() {
		return targetMemberId;
	}

	public void setTargetMemberId(Long targetMemberId) {
		this.targetMemberId = targetMemberId;
	}

}
