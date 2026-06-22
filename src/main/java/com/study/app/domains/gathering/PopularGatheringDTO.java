package com.study.app.domains.gathering;

import java.util.Date;

public class PopularGatheringDTO {
	private Long roomId;
	private String roomType;
	private Long festivalId;
	private String ownerId;
	private String roomTitle;
	private String roomDescription;
	private String freeLocation;
	private Date freeDate;
	private Date endDate; // 축제 종료일 컬럼 매핑용 필드
	private int maxCapacity;
	private String roomImage;
	private int participants; // 참여자 수 집계 필드

	@Override
	public String toString() {
		return "PopularGatheringDTO [roomId=" + roomId + ", roomType=" + roomType + ", festivalId=" + festivalId
				+ ", ownerId=" + ownerId + ", roomTitle=" + roomTitle + ", roomDescription=" + roomDescription
				+ ", freeLocation=" + freeLocation + ", freeDate=" + freeDate + ", endDate=" + endDate
				+ ", maxCapacity=" + maxCapacity + ", roomImage=" + roomImage + ", participants=" + participants + "]";
	}

	public PopularGatheringDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public PopularGatheringDTO(Long roomId, String roomType, Long festivalId, String ownerId, String roomTitle,
			String roomDescription, String freeLocation, Date freeDate, Date endDate, int maxCapacity, String roomImage,
			int participants) {
		super();
		this.roomId = roomId;
		this.roomType = roomType;
		this.festivalId = festivalId;
		this.ownerId = ownerId;
		this.roomTitle = roomTitle;
		this.roomDescription = roomDescription;
		this.freeLocation = freeLocation;
		this.freeDate = freeDate;
		this.endDate = endDate;
		this.maxCapacity = maxCapacity;
		this.roomImage = roomImage;
		this.participants = participants;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public String getRoomType() {
		return roomType;
	}

	public void setRoomType(String roomType) {
		this.roomType = roomType;
	}

	public Long getFestivalId() {
		return festivalId;
	}

	public void setFestivalId(Long festivalId) {
		this.festivalId = festivalId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getRoomTitle() {
		return roomTitle;
	}

	public void setRoomTitle(String roomTitle) {
		this.roomTitle = roomTitle;
	}

	public String getRoomDescription() {
		return roomDescription;
	}

	public void setRoomDescription(String roomDescription) {
		this.roomDescription = roomDescription;
	}

	public String getFreeLocation() {
		return freeLocation;
	}

	public void setFreeLocation(String freeLocation) {
		this.freeLocation = freeLocation;
	}

	public Date getFreeDate() {
		return freeDate;
	}

	public void setFreeDate(Date freeDate) {
		this.freeDate = freeDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public int getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(int maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public String getRoomImage() {
		return roomImage;
	}

	public void setRoomImage(String roomImage) {
		this.roomImage = roomImage;
	}

	public int getParticipants() {
		return participants;
	}

	public void setParticipants(int participants) {
		this.participants = participants;
	}

}
