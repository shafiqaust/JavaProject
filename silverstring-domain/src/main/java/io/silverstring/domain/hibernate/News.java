package io.silverstring.domain.hibernate;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Entity
public class News implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String url;

    private LocalDateTime regDtm;
    private LocalDateTime delDtm;
}
