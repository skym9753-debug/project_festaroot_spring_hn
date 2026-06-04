package com.study.app.domains.festival.dto;

public class FestImageDTO {

    private String contentid;
    private String originimgurl;
    private String imgname;
    private String smallimageurl;
    private String cpyrhtDivCd;
    private String serialnum;

    public FestImageDTO() {}

    public String getContentid() {
        return contentid;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public String getOriginimgurl() {
        return originimgurl;
    }

    public void setOriginimgurl(String originimgurl) {
        this.originimgurl = originimgurl;
    }

    public String getImgname() {
        return imgname;
    }

    public void setImgname(String imgname) {
        this.imgname = imgname;
    }

    public String getSmallimageurl() {
        return smallimageurl;
    }

    public void setSmallimageurl(String smallimageurl) {
        this.smallimageurl = smallimageurl;
    }

    public String getCpyrhtDivCd() {
        return cpyrhtDivCd;
    }

    public void setCpyrhtDivCd(String cpyrhtDivCd) {
        this.cpyrhtDivCd = cpyrhtDivCd;
    }

    public String getSerialnum() {
        return serialnum;
    }

    public void setSerialnum(String serialnum) {
        this.serialnum = serialnum;
    }
}