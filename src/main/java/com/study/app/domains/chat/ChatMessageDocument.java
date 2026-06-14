package com.study.app.domains.chat;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection="chat_messages") 
public class ChatMessageDocument {
    
    @Id
    private String id;          
    
    @Field("room_id")          
    private Long roomId;        
    
    @Field("sender_id")        
    private String senderId;    
    
    @Field("sender_name")      
    private String senderName;  
    
    private String message;     
    
    private ChatType type;      
    
    @Field("created_at")       
    private LocalDateTime createdAt = LocalDateTime.now(); 

    // 기본 생성자
    public ChatMessageDocument() {}

    // 모든 필드를 포함하는 생성자
    public ChatMessageDocument(String id, Long roomId, String senderId, String senderName, String message, ChatType type, LocalDateTime createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // --- Getter / Setter 직접 구현 ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ChatType getType() { return type; }
    public void setType(ChatType type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}