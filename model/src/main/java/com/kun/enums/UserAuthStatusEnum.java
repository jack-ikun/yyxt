package com.kun.enums;

/**
 * @author jiakun
 * @create 2023-03-18-22:43
 */
public enum UserAuthStatusEnum {
    UNAUTH(0,"未认证"),
    AUTHING(1,"认证中" ),
    AUTHED(2,"已认证" ),
    NOAUTH(-1,"认证失败"),
    ;

    private Integer status;

    private String comment;

    public static String getStatusNameByStatus(Integer status) {
        UserAuthStatusEnum arrObj[] = UserAuthStatusEnum.values();
        for (UserAuthStatusEnum obj : arrObj) {
            if (status.intValue() == obj.getStatus().intValue()) {
                return obj.getComment();
            }
        }
        return "";
    }

    UserAuthStatusEnum(Integer status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
