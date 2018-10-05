package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.ActiveEnum;
import io.silverstring.domain.enums.LevelEnum;
import io.silverstring.domain.enums.RoleEnum;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String pwd;

    @Enumerated(EnumType.STRING)
    private LevelEnum level;

    @Enumerated(EnumType.STRING)
    private ActiveEnum active;

    @Enumerated(EnumType.STRING)
    private RoleEnum role;

    private String otpHash;
    private String otpStatus;

    @Transient
    private String otpCode;

    private LocalDateTime regDtm;
    private LocalDateTime delDtm;
}
