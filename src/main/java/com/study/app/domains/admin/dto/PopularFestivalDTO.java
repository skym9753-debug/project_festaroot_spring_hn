package com.study.app.domains.admin.dto;

public class PopularFestivalDTO {
	
    private int rank;
    private String name;
    private String region;
    private int views;
    private int likes;
    private String status;
    
    public PopularFestivalDTO() {}

	public PopularFestivalDTO(int rank, String name, String region, int views, int likes, String status) {
		super();
		this.rank = rank;
		this.name = name;
		this.region = region;
		this.views = views;
		this.likes = likes;
		this.status = status;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    

}
