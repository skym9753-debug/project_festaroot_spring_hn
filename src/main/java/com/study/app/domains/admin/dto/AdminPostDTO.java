package com.study.app.domains.admin.dto;

import java.util.List;
import java.util.Map;

public class AdminPostDTO {
	
    private Long postId;
    private String postCode;

    private String title;
    private String category;
    private String author;
    private String content;
    private String createdAt;

    private int views;
    private int comments;

    private int imageCount;
    private int fileCount;

    private int reportCount;
    private int pendingReportCount;
    
    private String is_visible;

    // 상세조회에서만 채움
    private List<Map<String, Object>> reportItems;
    private List<Map<String, Object>> attachments;

    public AdminPostDTO() {}

	public AdminPostDTO(Long postId, String postCode, String title, String category, String author, String content,
			String createdAt, int views, int comments, int imageCount, int fileCount, int reportCount,
			int pendingReportCount, String is_visible, List<Map<String, Object>> reportItems,
			List<Map<String, Object>> attachments) {
		super();
		this.postId = postId;
		this.postCode = postCode;
		this.title = title;
		this.category = category;
		this.author = author;
		this.content = content;
		this.createdAt = createdAt;
		this.views = views;
		this.comments = comments;
		this.imageCount = imageCount;
		this.fileCount = fileCount;
		this.reportCount = reportCount;
		this.pendingReportCount = pendingReportCount;
		this.is_visible = is_visible;
		this.reportItems = reportItems;
		this.attachments = attachments;
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public int getComments() {
		return comments;
	}

	public void setComments(int comments) {
		this.comments = comments;
	}

	public int getImageCount() {
		return imageCount;
	}

	public void setImageCount(int imageCount) {
		this.imageCount = imageCount;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getReportCount() {
		return reportCount;
	}

	public void setReportCount(int reportCount) {
		this.reportCount = reportCount;
	}

	public int getPendingReportCount() {
		return pendingReportCount;
	}

	public void setPendingReportCount(int pendingReportCount) {
		this.pendingReportCount = pendingReportCount;
	}

	public String getIs_visible() {
		return is_visible;
	}

	public void setIs_visible(String is_visible) {
		this.is_visible = is_visible;
	}

	public List<Map<String, Object>> getReportItems() {
		return reportItems;
	}

	public void setReportItems(List<Map<String, Object>> reportItems) {
		this.reportItems = reportItems;
	}

	public List<Map<String, Object>> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Map<String, Object>> attachments) {
		this.attachments = attachments;
	}


	

}
