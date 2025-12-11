package faang.school.urlshortenerservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hash")
@Data
@NoArgsConstructor
public class Hash {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hash_seq")
    @SequenceGenerator(
            name = "hash_seq",
            sequenceName = "hash_id_seq",
            allocationSize = 50
    )
    private Long id;

    @Column(name = "hash", unique = true, nullable = false, length = 7)
    private String hash;

    public Hash(String hash) {
        this.hash = hash;
    }
}
