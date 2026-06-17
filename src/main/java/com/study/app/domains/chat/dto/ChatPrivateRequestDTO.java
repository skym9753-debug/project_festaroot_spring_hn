package com.study.app.domains.chat.dto;

public class ChatPrivateRequestDTO {

	private String currentUserId; // 1:1 채팅 신청인
	private String targetMemberId;

	@Override
	public String toString() {
		return "ChatPrivateRequestDTO [currentUserId=" + currentUserId + ", targetMemberId=" + targetMemberId + "]";
	}

	public ChatPrivateRequestDTO() {
	}

	public ChatPrivateRequestDTO(String currentUserId, String targetMemberId) {
		super();
		this.currentUserId = currentUserId;
		this.targetMemberId = targetMemberId;
	}

	public String getCurrentUserId() {
		return currentUserId;
	}

	public void setCurrentUserId(String currentUserId) {
		this.currentUserId = currentUserId;
	}

	public String getTargetMemberId() {
		return targetMemberId;
	}

	public void setTargetMemberId(String targetMemberId) {
		this.targetMemberId = targetMemberId;
	}

}
