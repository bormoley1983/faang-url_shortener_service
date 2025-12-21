package faang.school.urlshortenerservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "hashes", indexes = {
        @Index(name = "idx_hash_value", columnList = "hash_value")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hash {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hashes_seq_gen")
    @SequenceGenerator(
            name = "hashes_seq_gen",
            sequenceName = "hashes_id_seq",   // см. миграцию ниже
            allocationSize = 1000             // меньше round-trips за nextval
    )
    private Long id;

    @Column(name = "hash_value", nullable = false, unique = true, length = 10)
    private String hashValue;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
