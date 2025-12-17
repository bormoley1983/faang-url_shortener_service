package faang.school.urlshortenerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hash")
@Getter
@Setter
@NoArgsConstructor
public class Hash {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hash_seq")
    @SequenceGenerator(name = "hash_seq", sequenceName = "unique_hash_number_seq", allocationSize = 1)
    private long id;

    @Column(name = "hash", length = 6, unique = true, nullable = false)
    private String hash;

    public Hash(String hash) {
        this.hash = hash;
    }
}
