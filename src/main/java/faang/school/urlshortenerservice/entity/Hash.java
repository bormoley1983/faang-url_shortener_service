package faang.school.urlshortenerservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "hash")
@Data
public class Hash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hash", unique = true, nullable = false, length = 7)
    private String hash;
}
