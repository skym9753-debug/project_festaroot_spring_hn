package com.study.app.domains.admin.dto;

public class RegionStatDTO {
	
    private String region;
    private int count;
    private int percent;
    
    public RegionStatDTO() {}

	public RegionStatDTO(String region, int count, int percent) {
		super();
		this.region = region;
		this.count = count;
		this.percent = percent;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}
    
    

}
