package ru.naujava.taskmanager.repository.ydb;

import org.springframework.stereotype.Repository;
import ru.naujava.taskmanager.model.ydb.YdbUser;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.query.Params;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.values.PrimitiveValue;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями в YDB.
 */
@Repository
public class YdbUserRepository extends BaseYdbRepository {

    public YdbUserRepository(SessionRetryContext retryCtx) {
        super(retryCtx);
    }

    /**
     * Сохраняет пользователя.
     */
    public YdbUser save(YdbUser user) {
        if (user.getId() == null) {
            user.setId(generateId("users"));
        }

        String query = """
                DECLARE $id AS Uint64;
                DECLARE $username AS Utf8;
                DECLARE $password AS Utf8;
                DECLARE $role AS Utf8;
                UPSERT INTO users (id, username, password, role)
                VALUES ($id, $username, $password, $role);
                """;

        Params params = Params.of(
                "$id", PrimitiveValue.newUint64(user.getId()),
                "$username", PrimitiveValue.newText(user.getUsername()),
                "$password", PrimitiveValue.newText(user.getPassword()),
                "$role", PrimitiveValue.newText(user.getRole())
        );

        executeUpdate(query, params);
        return user;
    }

    /**
     * Находит пользователя по ID (по первичному ключу).
     */
    public Optional<YdbUser> findById(Long id) {
        String query = """
                DECLARE $id AS Uint64;
                SELECT id, username, password, role FROM users WHERE id = $id;
                """;

        Params params = Params.of("$id", PrimitiveValue.newUint64(id));

        return executeSerializableReadSingle(query, params, this::mapToUser);
    }

    /**
     * Находит пользователя по имени (предполагаем уникальность username — по первичному ключу или без индекса).
     */
    public Optional<YdbUser> findByUsername(String username) {
        String query = """
                DECLARE $username AS Utf8;
                SELECT id, username, password, role FROM users WHERE username = $username;
                """;

        Params params = Params.of("$username", PrimitiveValue.newText(username));

        return executeSerializableReadSingle(query, params, this::mapToUser);
    }

    /**
     * Находит всех пользователей.
     */
    public List<YdbUser> findAll() {
        String query = "SELECT id, username, password, role FROM users ORDER BY id ASC;";

        return executeSerializableReadList(query, Params.empty(), this::mapToUser);
    }

    /**
     * Проверяет существование по имени.
     */
    public boolean existsByUsername(String username) {
        String query = """
                DECLARE $username AS Utf8;
                SELECT COUNT(*) AS cnt FROM users WHERE username = $username;
                """;

        Params params = Params.of("$username", PrimitiveValue.newText(username));

        return executeSerializableReadSingle(query, params, rs -> rs.getColumn("cnt").getUint64())
                .map(count -> count > 0)
                .orElse(false);
    }

    /**
     * Удаляет пользователя по ID.
     */
    public void deleteById(Long id) {
        String query = """
                DECLARE $id AS Uint64;
                DELETE FROM users WHERE id = $id;
                """;

        Params params = Params.of("$id", PrimitiveValue.newUint64(id));

        executeUpdate(query, params);
    }

    /**
     * Маппинг в YdbUser.
     */
    private YdbUser mapToUser(ResultSetReader rs) {
        return new YdbUser(
                rs.getColumn("id").getUint64(),
                rs.getColumn("username").getText(),
                rs.getColumn("password").getText(),
                rs.getColumn("role").getText()
        );
    }
}