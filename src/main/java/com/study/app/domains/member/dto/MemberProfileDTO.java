package com.study.app.domains.member.dto;

import java.util.List;
import com.study.app.domains.activity.dto.UserActivityLogDTO;

import java.util.Map;

public class MemberProfileDTO {
    private MemberDTO member;
    private List<InterestRegionDTO> interestRegions;
    private List<InterestThemeDTO> interestThemes;
    private List<UserActivityLogDTO> recentLogs;
    private List<Map<String, Object>> likedFestivals;

    public MemberProfileDTO() {}

    public MemberProfileDTO(MemberDTO member, List<InterestRegionDTO> interestRegions, List<InterestThemeDTO> interestThemes, 
                            List<UserActivityLogDTO> recentLogs, List<Map<String, Object>> likedFestivals) {
        this.member = member;
        this.interestRegions = interestRegions;
        this.interestThemes = interestThemes;
        this.recentLogs = recentLogs;
        this.likedFestivals = likedFestivals;
    }

    public List<Map<String, Object>> getLikedFestivals() {
        return likedFestivals;
    }

    public void setLikedFestivals(List<Map<String, Object>> likedFestivals) {
        this.likedFestivals = likedFestivals;
    }

    public MemberDTO getMember() {
        return member;
    }

    public void setMember(MemberDTO member) {
        this.member = member;
    }

    public List<InterestRegionDTO> getInterestRegions() {
        return interestRegions;
    }

    public void setInterestRegions(List<InterestRegionDTO> interestRegions) {
        this.interestRegions = interestRegions;
    }

    public List<InterestThemeDTO> getInterestThemes() {
        return interestThemes;
    }

    public void setInterestThemes(List<InterestThemeDTO> interestThemes) {
        this.interestThemes = interestThemes;
    }

    public List<UserActivityLogDTO> getRecentLogs() {
        return recentLogs;
    }

    public void setRecentLogs(List<UserActivityLogDTO> recentLogs) {
        this.recentLogs = recentLogs;
    }
}
