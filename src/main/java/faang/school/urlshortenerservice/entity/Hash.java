package faang.school.urlshortenerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Класс-сущность представляющий хэш
 *
 * @author Linempy
 * @since 10.09.2025
 */
@Entity
@Table(name = "hash")
@Getter
@Setter
@AllArgsConstructor
public class Hash {

    @Id
    @Column(name = "hash", length = 6)
    private final String hash;
}