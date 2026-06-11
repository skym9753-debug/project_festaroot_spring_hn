package com.study.app.domains.gathering;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GatheringCreateDTO {
	private Long roomId;
	private String roomType;
	private Long festivalId;
	private String ownerId;
	private String roomTitle;
	private String roomDescription;
	private String freeLocation;
	private LocalDate freeDate;
	private Integer maxCapacity;
	private LocalDateTime createdAt;

	@Override
	public String toString() {
		return "GatheringCreateDTO [roomId=" + roomId + ", roomType=" + roomType + ", festivalId=" + festivalId
				+ ", ownerId=" + ownerId + ", roomTitle=" + roomTitle + ", roomDescription=" + roomDescription
				+ ", freeLocation=" + freeLocation + ", freeDate=" + freeDate + ", maxCapacity=" + maxCapacity
				+ ", createdAt=" + createdAt + "]";
	}

	public GatheringCreateDTO() {}

	public GatheringCreateDTO(Long roomId, String roomType, Long festivalId, String ownerId, String roomTitle,
			String roomDescription, String freeLocation, LocalDate freeDate, Integer maxCapacity,
			LocalDateTime createdAt) {
		super();
		this.roomId = roomId;
		this.roomType = roomType;
		this.festivalId = festivalId;
		this.ownerId = ownerId;
		this.roomTitle = roomTitle;
		this.roomDescription = roomDescription;
		this.freeLocation = freeLocation;
		this.freeDate = freeDate;
		this.maxCapacity = maxCapacity;
		this.createdAt = createdAt;
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

	public LocalDate getFreeDate() {
		return freeDate;
	}

	public void setFreeDate(LocalDate freeDate) {
		this.freeDate = freeDate;
	}

	public Integer getMaxCapacity() {
		return maxCapacity;
	}

	public void setMaxCapacity(Integer maxCapacity) {
		this.maxCapacity = maxCapacity;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
