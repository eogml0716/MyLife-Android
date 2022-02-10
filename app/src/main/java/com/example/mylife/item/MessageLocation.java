package com.example.mylife.item;

// 메시지가 왼쪽에 놓일 지 중간에 놓일 지 오른쪽에 놓일 지 결정하는 거임, 테이블에 작성된 텍스트인 지, 이미지인 지 결정짓는 타입 아님
public enum MessageLocation {
    NONE(-1), LEFT_CONTENTS(0), CENTER_CONTENTS(1), RIGHT_CONTENTS(2), LEFT_IMAGE(3), RIGHT_IMAGE(4);

    private int code;

    MessageLocation(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public static MessageLocation of(int code) {
        MessageLocation[] types = MessageLocation.values();
        for (MessageLocation type : types) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return NONE;
    }

}
