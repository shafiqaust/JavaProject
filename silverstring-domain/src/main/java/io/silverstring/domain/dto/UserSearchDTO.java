package io.silverstring.domain.dto;

import lombok.Data;

@Data
public class UserSearchDTO {
    private String code;
    private String email;
    private String locale;
}
