package com.study.app.domains.ai.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AIPlannerDTO {

    private Long planner_id;
    private String member_id;

    @JsonProperty("content_id")
    @JsonAlias({"contentId", "CONTENT_ID"})
    private Long content_id;

    private String title;

    @JsonProperty("visit_date")
    @JsonAlias({"visitDate"})
    private LocalDate visit_date;

    // 기존 컬럼은 유지하되, 최종 기능에서는 사용하지 않음
    @JsonProperty("start_location")
    @JsonAlias({"startLocation"})
    private String start_location;

    @JsonProperty("people_count")
    @JsonAlias({"peopleCount"})
    private Integer people_count;

    @JsonProperty("companion_type")
    @JsonAlias({"companionType"})
    private String companion_type;

    // 기존 이동수단 컬럼. 최종 기능에서는 필수 아님
    @JsonProperty("transport_type")
    @JsonAlias({"transportType"})
    private String transport_type;

    @JsonProperty("start_time")
    @JsonAlias({"startTime"})
    private String start_time;

    @JsonProperty("end_time")
    @JsonAlias({"endTime"})
    private String end_time;

    @JsonProperty("user_input")
    @JsonAlias({"userInput"})
    private String user_input;

    @JsonProperty("weather_summary")
    @JsonAlias({"weatherSummary"})
    private String weather_summary;

    @JsonProperty("rag_query")
    @JsonAlias({"ragQuery"})
    private String rag_query;

    @JsonProperty("recommendation_reason")
    @JsonAlias({"recommendationReason"})
    private String recommendation_reason;

    // 새로 추가한 컬럼
    @JsonProperty("planner_type")
    @JsonAlias({"plannerType"})
    private String planner_type;

    @JsonProperty("course_style")
    @JsonAlias({"courseStyle"})
    private String course_style;

    @JsonProperty("route_notice")
    @JsonAlias({"routeNotice"})
    private String route_notice;

    // 조회/응답용 추가 필드
    private String festival_title;
    private String first_image;
    private String addr1;
    private String map_x;
    private String map_y;

    public AIPlannerDTO() {}

    public Long getPlanner_id() {
        return planner_id;
    }

    public void setPlanner_id(Long planner_id) {
        this.planner_id = planner_id;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public Long getContent_id() {
        return content_id;
    }

    public void setContent_id(Long content_id) {
        this.content_id = content_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getVisit_date() {
        return visit_date;
    }

    public void setVisit_date(LocalDate visit_date) {
        this.visit_date = visit_date;
    }

    public String getStart_location() {
        return start_location;
    }

    public void setStart_location(String start_location) {
        this.start_location = start_location;
    }

    public Integer getPeople_count() {
        return people_count;
    }

    public void setPeople_count(Integer people_count) {
        this.people_count = people_count;
    }

    public String getCompanion_type() {
        return companion_type;
    }

    public void setCompanion_type(String companion_type) {
        this.companion_type = companion_type;
    }

    public String getTransport_type() {
        return transport_type;
    }

    public void setTransport_type(String transport_type) {
        this.transport_type = transport_type;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getUser_input() {
        return user_input;
    }

    public void setUser_input(String user_input) {
        this.user_input = user_input;
    }

    public String getWeather_summary() {
        return weather_summary;
    }

    public void setWeather_summary(String weather_summary) {
        this.weather_summary = weather_summary;
    }

    public String getRag_query() {
        return rag_query;
    }

    public void setRag_query(String rag_query) {
        this.rag_query = rag_query;
    }

    public String getRecommendation_reason() {
        return recommendation_reason;
    }

    public void setRecommendation_reason(String recommendation_reason) {
        this.recommendation_reason = recommendation_reason;
    }

    public String getPlanner_type() {
        return planner_type;
    }

    public void setPlanner_type(String planner_type) {
        this.planner_type = planner_type;
    }

    public String getCourse_style() {
        return course_style;
    }

    public void setCourse_style(String course_style) {
        this.course_style = course_style;
    }

    public String getRoute_notice() {
        return route_notice;
    }

    public void setRoute_notice(String route_notice) {
        this.route_notice = route_notice;
    }

    public String getFestival_title() {
        return festival_title;
    }

    public void setFestival_title(String festival_title) {
        this.festival_title = festival_title;
    }

    public String getFirst_image() {
        return first_image;
    }

    public void setFirst_image(String first_image) {
        this.first_image = first_image;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(String addr1) {
        this.addr1 = addr1;
    }

    public String getMap_x() {
        return map_x;
    }

    public void setMap_x(String map_x) {
        this.map_x = map_x;
    }

    public String getMap_y() {
        return map_y;
    }

    public void setMap_y(String map_y) {
        this.map_y = map_y;
    }

    // 개발 완료 후 삭제 예정
    @Override
    public String toString() {
        return "AIPlannerDTO{" +
                "planner_id=" + planner_id +
                ", member_id='" + member_id + '\'' +
                ", content_id=" + content_id +
                ", title='" + title + '\'' +
                ", visit_date=" + visit_date +
                ", people_count=" + people_count +
                ", companion_type='" + companion_type + '\'' +
                ", course_style='" + course_style + '\'' +
                ", planner_type='" + planner_type + '\'' +
                '}';
    }
}