package io.silverstring.domain.hibernate;

import io.silverstring.domain.enums.TagEnum;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
public class ActionLog implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private TagEnum tag;

    private String ip;
    private String userAgent;

    private LocalDateTime regDtm;
    private String data;
}
