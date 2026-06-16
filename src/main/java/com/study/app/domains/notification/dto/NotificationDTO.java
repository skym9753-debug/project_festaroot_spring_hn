package com.study.app.domains.notification.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private Long noti_id;
    private String member_id;
    private String noti_type;
    private Long reference_id;
    private String content;
    private String is_read;
    private LocalDateTime created_at;

    public NotificationDTO() {}

    public NotificationDTO(String member_id, String noti_type, Long reference_id, String content) {
        this.member_id = member_id;
        this.noti_type = noti_type;
        this.reference_id = reference_id;
        this.content = content;
        this.is_read = "N";
    }

    public Long getNoti_id() { return noti_id; }
    public void setNoti_id(Long noti_id) { this.noti_id = noti_id; }
    public String getMember_id() { return member_id; }
    public void setMember_id(String member_id) { this.member_id = member_id; }
    public String getNoti_type() { return noti_type; }
    public void setNoti_type(String noti_type) { this.noti_type = noti_type; }
    public Long getReference_id() { return reference_id; }
    public void setReference_id(Long reference_id) { this.reference_id = reference_id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getIs_read() { return is_read; }
    public void setIs_read(String is_read) { this.is_read = is_read; }
    public LocalDateTime getCreated_at() { return created_at; }
    public void setCreated_at(LocalDateTime created_at) { this.created_at = created_at; }
}
