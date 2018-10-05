package io.silverstring.domain.enums;

public enum LevelEnum {
    LEVEL1("level1", "1 level member"),
    LEVEL2("level2", "2 level member"),
    LEVEL3("level3", "3 level member");

    private String code;
    private String detail;

    LevelEnum(String code, String detail) {
        this.code = code;
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }
    public String getDetail() {
        return detail;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public void setDetail(String detail) {
        this.detail = detail;
    }

}
