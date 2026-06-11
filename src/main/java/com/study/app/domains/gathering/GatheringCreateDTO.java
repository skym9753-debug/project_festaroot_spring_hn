package com.study.app.domains.gathering;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class GatheringCreateDTO {
	private Long room_id;
	private String room_type;
	private Long festival_id;
	private String owner_id;
	private String room_title;
	private String room_description;
	private String free_location;
	private LocalDate free_date;
	private Integer max_capacity; // 최대 인원 수
	private LocalDateTime created_at;

	// 백엔드에서 조인 및 서브쿼리로 채워줄 3가지 필드 추가
	private String nickname; // 닉네임
	private String profile_image_url; // 프로필 이미지
	private Integer current_count; // 참여 인원 수

	@Override
	public String toString() {
		return "GatheringCreateDTO [room_id=" + room_id + ", room_type=" + room_type + ", festival_id=" + festival_id
				+ ", owner_id=" + owner_id + ", room_title=" + room_title + ", room_description=" + room_description
				+ ", free_location=" + free_location + ", free_date=" + free_date + ", max_capacity=" + max_capacity
				+ ", created_at=" + created_at + ", nickname=" + nickname + ", profile_image_url=" + profile_image_url
				+ ", current_count=" + current_count + "]";
	}

	public GatheringCreateDTO() {}

	public GatheringCreateDTO(Long room_id, String room_type, Long festival_id, String owner_id, String room_title,
			String room_description, String free_location, LocalDate free_date, Integer max_capacity,
			LocalDateTime created_at, String nickname, String profile_image_url, Integer current_count) {
		super();
		this.room_id = room_id;
		this.room_type = room_type;
		this.festival_id = festival_id;
		this.owner_id = owner_id;
		this.room_title = room_title;
		this.room_description = room_description;
		this.free_location = free_location;
		this.free_date = free_date;
		this.max_capacity = max_capacity;
		this.created_at = created_at;
		this.nickname = nickname;
		this.profile_image_url = profile_image_url;
		this.current_count = current_count;
	}

	public Long getRoom_id() {
		return room_id;
	}

	public void setRoom_id(Long room_id) {
		this.room_id = room_id;
	}

	public String getRoom_type() {
		return room_type;
	}

	public void setRoom_type(String room_type) {
		this.room_type = room_type;
	}

	public Long getFestival_id() {
		return festival_id;
	}

	public void setFestival_id(Long festival_id) {
		this.festival_id = festival_id;
	}

	public String getOwner_id() {
		return owner_id;
	}

	public void setOwner_id(String owner_id) {
		this.owner_id = owner_id;
	}

	public String getRoom_title() {
		return room_title;
	}

	public void setRoom_title(String room_title) {
		this.room_title = room_title;
	}

	public String getRoom_description() {
		return room_description;
	}

	public void setRoom_description(String room_description) {
		this.room_description = room_description;
	}

	public String getFree_location() {
		return free_location;
	}

	public void setFree_location(String free_location) {
		this.free_location = free_location;
	}

	public LocalDate getFree_date() {
		return free_date;
	}

	public void setFree_date(LocalDate free_date) {
		this.free_date = free_date;
	}

	public Integer getMax_capacity() {
		return max_capacity;
	}

	public void setMax_capacity(Integer max_capacity) {
		this.max_capacity = max_capacity;
	}

	public LocalDateTime getCreated_at() {
		return created_at;
	}

	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getProfile_image_url() {
		return profile_image_url;
	}

	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}

	public Integer getCurrent_count() {
		return current_count;
	}

	public void setCurrent_count(Integer current_count) {
		this.current_count = current_count;
	}

}
