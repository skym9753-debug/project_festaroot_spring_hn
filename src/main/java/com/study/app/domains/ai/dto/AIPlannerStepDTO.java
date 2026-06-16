package com.study.app.domains.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AIPlannerStepDTO {

    private Long step_id;
    private Long planner_id;
    private Integer step_order;

    @JsonProperty("time_label")
    @JsonAlias({"timeLabel", "time"})
    private String time_label;

    private String title;
    private String description;
    private String type;

    @JsonProperty("place_name")
    @JsonAlias({"placeName"})
    private String place_name;

    private String address;
    private String x;
    private String y;
    private String reason;

    @JsonProperty("kakao_place_url")
    @JsonAlias({"kakaoPlaceUrl"})
    private String kakao_place_url;

    // 새로 추가한 컬럼
    @JsonProperty("source_content_id")
    @JsonAlias({"sourceContentId", "contentid"})
    private String source_content_id;

    @JsonProperty("content_type_id")
    @JsonAlias({"contentTypeId", "contenttypeid"})
    private String content_type_id;

    private Double distance;

    @JsonProperty("first_image")
    @JsonAlias({"firstImage", "firstimage"})
    private String first_image;

    @JsonProperty("source_api")
    @JsonAlias({"sourceApi"})
    private String source_api;

    public AIPlannerStepDTO() {}

    public Long getStep_id() {
        return step_id;
    }

    public void setStep_id(Long step_id) {
        this.step_id = step_id;
    }

    public Long getPlanner_id() {
        return planner_id;
    }

    public void setPlanner_id(Long planner_id) {
        this.planner_id = planner_id;
    }

    public Integer getStep_order() {
        return step_order;
    }

    public void setStep_order(Integer step_order) {
        this.step_order = step_order;
    }

    public String getTime_label() {
        return time_label;
    }

    public void setTime_label(String time_label) {
        this.time_label = time_label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getKakao_place_url() {
        return kakao_place_url;
    }

    public void setKakao_place_url(String kakao_place_url) {
        this.kakao_place_url = kakao_place_url;
    }

    public String getSource_content_id() {
        return source_content_id;
    }

    public void setSource_content_id(String source_content_id) {
        this.source_content_id = source_content_id;
    }

    public String getContent_type_id() {
        return content_type_id;
    }

    public void setContent_type_id(String content_type_id) {
        this.content_type_id = content_type_id;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public String getFirst_image() {
        return first_image;
    }

    public void setFirst_image(String first_image) {
        this.first_image = first_image;
    }

    public String getSource_api() {
        return source_api;
    }

    public void setSource_api(String source_api) {
        this.source_api = source_api;
    }

    // 개발 완료 후 삭제 예정	
    @Override
    public String toString() {
        return "AIPlannerStepDTO{" +
                "step_id=" + step_id +
                ", planner_id=" + planner_id +
                ", step_order=" + step_order +
                ", time_label='" + time_label + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", place_name='" + place_name + '\'' +
                ", source_api='" + source_api + '\'' +
                '}';
    }
}