package faang.school.urlshortenerservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hashes")
@AllArgsConstructor
@RequiredArgsConstructor
@Setter
@Getter
public class Hash {
    @Id
    private String hash;
}
