package com.multi.matchon.community.domain;


public enum Category {
    ANNOUNCEMENT("공지사항"),
    FREEBOARD("자유게시판"),
    INFORMATION("정보게시판"),
    REVIEWBOARD("리뷰게시판");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


