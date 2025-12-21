package faang.school.urlshortenerservice.service;

/**
 * Сервис для очистки устаревших URL-ассоциаций
 * и возврата освобождённых хэшей в пул доступных.
 */
public interface CleanerService {
    /**
     * Выполняет очистку устаревших URL-ассоциаций.
     * <p>
     * Удаляет записи из таблицы {@code url}, созданные ранее пороговой даты,
     * и возвращает их хэши обратно в таблицу {@code hash}.
     * </p>
     *
     * @return количество удалённых URL-ассоциаций
     */
    public int clean();
}
