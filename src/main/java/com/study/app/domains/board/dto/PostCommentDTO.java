package com.study.app.domains.board.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PostCommentDTO {

	private Long comment_id;
	private Long post_id;
	private String member_id;
	private String content;
	private Long parent_comment_id; // 부모 댓글 없으면 null
	private LocalDateTime created_at;
	private LocalDateTime updated_at;

	private String nickname;
	private String profile_image_url;
	private List<PostCommentDTO> children; // 대댓글

	// 좋아요, 신고
	private Long like_count;
	private Long report_count;

	// 연결 게시글 정보
	private String post_title;
	private String post_category;

	// 관리자 화면 날짜 문자열
	private String created_at_text;
	private String updated_at_text;

	// WAITING 상태 신고 수
	private Long pending_report_count;
	
	private String is_visible;


	public PostCommentDTO() {}


	public PostCommentDTO(Long comment_id, Long post_id, String member_id, String content, Long parent_comment_id,
			LocalDateTime created_at, LocalDateTime updated_at, String nickname, String profile_image_url,
			List<PostCommentDTO> children, Long like_count, Long report_count, String post_title, String post_category,
			String created_at_text, String updated_at_text, Long pending_report_count, String is_visible) {
		super();
		this.comment_id = comment_id;
		this.post_id = post_id;
		this.member_id = member_id;
		this.content = content;
		this.parent_comment_id = parent_comment_id;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.nickname = nickname;
		this.profile_image_url = profile_image_url;
		this.children = children;
		this.like_count = like_count;
		this.report_count = report_count;
		this.post_title = post_title;
		this.post_category = post_category;
		this.created_at_text = created_at_text;
		this.updated_at_text = updated_at_text;
		this.pending_report_count = pending_report_count;
		this.is_visible = is_visible;
	}


	public Long getComment_id() {
		return comment_id;
	}


	public void setComment_id(Long comment_id) {
		this.comment_id = comment_id;
	}


	public Long getPost_id() {
		return post_id;
	}


	public void setPost_id(Long post_id) {
		this.post_id = post_id;
	}


	public String getMember_id() {
		return member_id;
	}


	public void setMember_id(String member_id) {
		this.member_id = member_id;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public Long getParent_comment_id() {
		return parent_comment_id;
	}


	public void setParent_comment_id(Long parent_comment_id) {
		this.parent_comment_id = parent_comment_id;
	}


	public LocalDateTime getCreated_at() {
		return created_at;
	}


	public void setCreated_at(LocalDateTime created_at) {
		this.created_at = created_at;
	}


	public LocalDateTime getUpdated_at() {
		return updated_at;
	}


	public void setUpdated_at(LocalDateTime updated_at) {
		this.updated_at = updated_at;
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


	public List<PostCommentDTO> getChildren() {
		return children;
	}


	public void setChildren(List<PostCommentDTO> children) {
		this.children = children;
	}


	public Long getLike_count() {
		return like_count;
	}


	public void setLike_count(Long like_count) {
		this.like_count = like_count;
	}


	public Long getReport_count() {
		return report_count;
	}


	public void setReport_count(Long report_count) {
		this.report_count = report_count;
	}


	public String getPost_title() {
		return post_title;
	}


	public void setPost_title(String post_title) {
		this.post_title = post_title;
	}


	public String getPost_category() {
		return post_category;
	}


	public void setPost_category(String post_category) {
		this.post_category = post_category;
	}


	public String getCreated_at_text() {
		return created_at_text;
	}


	public void setCreated_at_text(String created_at_text) {
		this.created_at_text = created_at_text;
	}


	public String getUpdated_at_text() {
		return updated_at_text;
	}


	public void setUpdated_at_text(String updated_at_text) {
		this.updated_at_text = updated_at_text;
	}


	public Long getPending_report_count() {
		return pending_report_count;
	}


	public void setPending_report_count(Long pending_report_count) {
		this.pending_report_count = pending_report_count;
	}


	public String getIs_visible() {
		return is_visible;
	}


	public void setIs_visible(String is_visible) {
		this.is_visible = is_visible;
	}


	

}
