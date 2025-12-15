package faang.school.urlshortenerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "url")
@Getter
@Setter
@NoArgsConstructor
public class Url {
    @Column(name = "hash", length = 6, unique = true, nullable = false)
    private String hash;

    @Column(name = "long_url")
    private String longUrl;

    public Url(String hash, String longUrl) {
        this.hash = hash;
        this.longUrl = longUrl;
    }
}
