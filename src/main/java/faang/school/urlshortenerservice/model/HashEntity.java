package faang.school.urlshortenerservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HashEntity — Сущность для хранения всех сгенерированных хэшей
 *
 * @author agent
 * @since 10.09.2025
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "hash")
public class HashEntity {

    @Id
    @Column(name = "hash", length = 6)
    private String hash;
}