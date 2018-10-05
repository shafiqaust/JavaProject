package io.silverstring.domain.hibernate;

import lombok.Data;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

@Data
@Entity
public class IcoRecommend implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String title;
    private String url;
    private String content;
    private String email;
    private String imgUrl;
}
