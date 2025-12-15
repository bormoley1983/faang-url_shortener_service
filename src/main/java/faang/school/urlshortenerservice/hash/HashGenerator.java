package faang.school.urlshortenerservice.hash;

import faang.school.urlshortenerservice.entity.Hash;
import faang.school.urlshortenerservice.repo.HashRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class HashGenerator {

    private final HashRepository hashRepository;
    private final Base64Encoder base64Encoder;

    @Value("${hash.range:1000}") //TODO кинуть в конфиг
    private int maxRange;

    public void generateBatch() { //TODO Async имеет свой кастомный трэд пул. Трэд пул создаётся в конфигурации отдельным бином с соответсвующим именем. Его размер, и размер его очереди задач, задаются через конфиг.
        List<Long> range = hashRepository.getUniqueNumbers(maxRange);
        List<String> hashes = base64Encoder.encode(range);
        hashRepository.save(hashes);//TODO сохранять батчами

    }



    @Transactional //TODO подумать над джобой,чтобы автоматом иногда запускался, кроме ручного вызова
    public void generateHash() {
        List<Long> ganaratedHashlist = hashRepository.getUniqueNumbers(maxRange);
        //TODO подумать,как распараллелить вытаскивание диапазона,если он большой
        List<String> hash = base64Encoder.encode(ganaratedHashlist);

        for (String elem : hash) {

        }
        //TODO допилить батчевое сохранение в бд, спринг или хибернейт?
    }

    @Async
//TODO как сделать это асинхронным, чтобы когда локальный хеш ходил в базу, его не ждал пользователь, этот оставить
    //без асинк, а сделать асинхронный отдельно. не синхорнный будет участвовать в разогреве кеша, а синхр в работе
    @Transactional
    public List<Hash> getHashes(long amount) {
        List<Hash> hashes = hashRepository.getAndDelete(amount);
        if (hashes.size() < amount) {
            generateHash();
            hashes.addAll(getHashes(amount - hashes.size()));
        }
        return hashes;
    }
}
