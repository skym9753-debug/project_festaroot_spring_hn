package com.study.app.domains.chat;

public enum ChatType {
    TALK,   // 일반 대화 메시지
    ENTER,  // 채팅방 입장 메시지
    LEAVE,  // 채팅방 퇴장 메시지
    KICK,    // 강퇴 시스템 메시지
    DM // 1:1 대화용 입장 메세지
}
