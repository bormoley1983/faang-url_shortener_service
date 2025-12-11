package faang.school.urlshortenerservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;

@Entity
@Table(name = "url")
public class Url {

    @Id
    public String hash;

    public String url;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    public LocalDateTime createdAt;
}
