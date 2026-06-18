package com.study.app.domains.admin.dto;

public class FestivalStatusStatDTO {
	
    private String label;
    private int count;
    private int percent;
    
    public FestivalStatusStatDTO () {}

	public FestivalStatusStatDTO(String label, int count, int percent) {
		super();
		this.label = label;
		this.count = count;
		this.percent = percent;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
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
