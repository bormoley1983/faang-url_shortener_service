package faang.school.urlshortenerservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "url")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Url {
    @Id
    @Column(name = "hash", nullable = false, length = 6)
    private String hash;

    @Column(name = "url", nullable = false, length = 2048)
    private String url;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}