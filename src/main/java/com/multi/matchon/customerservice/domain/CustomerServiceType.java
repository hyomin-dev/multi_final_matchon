package com.multi.matchon.customerservice.domain;


public enum CustomerServiceType {
    // 이용방법
    HOWTOUSE("이용방법"),
    // 계정
    ACCOUNT("계정"),
    // TEAM / GUEST
    TEAM_GUEST("Team / Guest"),
    // 쇼핑몰
    SHOP("쇼핑몰"),
    // 신고
    REPORT("신고"),
    // 매너온도
    MANNER_TEMPERATURE("매너온도");

    private final String label;

    CustomerServiceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
