package com.study.app.domains.festival.dto;

public class RegionTourPortalStagingDTO {
	
    private String sourceId;
    private String regionCode;
    private String sigunguCode;
    private String tourismPortalUrl;

    public RegionTourPortalStagingDTO() {}

	public RegionTourPortalStagingDTO(String sourceId, String regionCode, String sigunguCode, String tourismPortalUrl) {
		super();
		this.sourceId = sourceId;
		this.regionCode = regionCode;
		this.sigunguCode = sigunguCode;
		this.tourismPortalUrl = tourismPortalUrl;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getSigunguCode() {
		return sigunguCode;
	}

	public void setSigunguCode(String sigunguCode) {
		this.sigunguCode = sigunguCode;
	}

	public String getTourismPortalUrl() {
		return tourismPortalUrl;
	}

	public void setTourismPortalUrl(String tourismPortalUrl) {
		this.tourismPortalUrl = tourismPortalUrl;
	}
    
    

}
