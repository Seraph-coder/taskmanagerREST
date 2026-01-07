package ru.naujava.taskmanager.repository.ydb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.ydb.core.Result;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.query.Params;
import tech.ydb.table.result.ResultSetReader;
import tech.ydb.table.transaction.TxControl;
import tech.ydb.table.values.PrimitiveValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Базовый класс для репозиториев YDB.
 * Содержит методы для записи и чтения с подробным логированием.
 */
public abstract class BaseYdbRepository {

    private static final Logger log = LoggerFactory.getLogger(BaseYdbRepository.class);

    protected final SessionRetryContext retryCtx;

    protected BaseYdbRepository(SessionRetryContext retryCtx) {
        this.retryCtx = retryCtx;
    }

    // ========================
    // Запись и генерация ID (SerializableRW + commit)
    // ========================

    protected void executeUpdate(String query, Params params) {
        log.info("=== YDB UPDATE START ===");
        log.info("Query:\n{}", query.trim());
        log.info("Params: {}", params);

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("YDB update FAILED: {}", result.getStatus());
            log.error("Issues: {}", result.getStatus().getIssues());
            throw new RuntimeException("YDB update failed: " + result.getStatus());
        } else {
            log.info("YDB update SUCCESS");
        }
        log.info("=== YDB UPDATE END ===\n");
    }

    protected Long generateId(String tableName) {
        log.info("Generating ID for table: {}", tableName);

        String query = """
                DECLARE $table_name AS Utf8;
                UPDATE id_sequences SET next_id = next_id + 1 WHERE table_name = $table_name;
                SELECT next_id FROM id_sequences WHERE table_name = $table_name;
                """;

        Params params = Params.of("$table_name", PrimitiveValue.newText(tableName));

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.serializableRw().setCommitTx(true), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("Failed to generate ID for {}: {}", tableName, result.getStatus());
            throw new RuntimeException("Failed to generate ID: " + result.getStatus());
        }

        ResultSetReader rs = result.getValue().getResultSet(0);
        if (rs.next()) {
            long newId = rs.getColumn("next_id").getUint64();
            log.info("Generated new ID: {} for table {}", newId, tableName);
            return newId;
        }
        throw new RuntimeException("Failed to generate ID for table: " + tableName);
    }

    // ========================
    // Чтение по асинхронным индексам — StaleRO
    // ========================

    protected <T> List<T> executeStaleReadList(
            String query,
            Params params,
            Function<ResultSetReader, T> mapper) {

        log.info("=== YDB STALE READ (INDEX) START ===");
        log.info("Query:\n{}", query.trim());
        log.info("Params: {}", params);

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.staleRo(), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("YDB stale read FAILED: {}", result.getStatus());
            log.error("Issues: {}", result.getStatus().getIssues());
            throw new RuntimeException("YDB stale read failed: " + result.getStatus());
        }

        ResultSetReader rs = result.getValue().getResultSet(0);
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.apply(rs));
        }

        log.info("YDB stale read SUCCESS — returned {} rows", results.size());
        log.info("=== YDB STALE READ END ===\n");
        return results;
    }

    protected <T> Optional<T> executeStaleReadSingle(
            String query,
            Params params,
            Function<ResultSetReader, T> mapper) {

        log.info("=== YDB STALE READ SINGLE (INDEX) START ===");
        log.info("Query:\n{}", query.trim());
        log.info("Params: {}", params);

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.staleRo(), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("YDB stale read single FAILED: {}", result.getStatus());
            throw new RuntimeException("YDB stale read failed: " + result.getStatus());
        }

        ResultSetReader rs = result.getValue().getResultSet(0);
        Optional<T> resultOpt = rs.next() ? Optional.of(mapper.apply(rs)) : Optional.empty();

        log.info("YDB stale read single SUCCESS — found: {}", resultOpt.isPresent());
        log.info("=== YDB STALE READ SINGLE END ===\n");
        return resultOpt;
    }

    // ========================
    // Обычное чтение (по первичному ключу) — SerializableRW без коммита
    // ========================

    protected <T> List<T> executeSerializableReadList(
            String query,
            Params params,
            Function<ResultSetReader, T> mapper) {

        log.info("=== YDB SERIALIZABLE READ LIST START ===");
        log.info("Query:\n{}", query.trim());
        log.info("Params: {}", params);

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.serializableRw(), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("YDB serializable read FAILED: {}", result.getStatus());
            throw new RuntimeException("YDB serializable read failed: " + result.getStatus());
        }

        ResultSetReader rs = result.getValue().getResultSet(0);
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.apply(rs));
        }

        log.info("YDB serializable read SUCCESS — returned {} rows", results.size());
        log.info("=== YDB SERIALIZABLE READ LIST END ===\n");
        return results;
    }

    protected <T> Optional<T> executeSerializableReadSingle(
            String query,
            Params params,
            Function<ResultSetReader, T> mapper) {

        log.info("=== YDB SERIALIZABLE READ SINGLE START ===");
        log.info("Query:\n{}", query.trim());
        log.info("Params: {}", params);

        Result<DataQueryResult> result = retryCtx.supplyResult(session ->
                session.executeDataQuery(query, TxControl.serializableRw(), params)
        ).join();

        if (!result.isSuccess()) {
            log.error("YDB serializable read single FAILED: {}", result.getStatus());
            throw new RuntimeException("YDB serializable read failed: " + result.getStatus());
        }

        ResultSetReader rs = result.getValue().getResultSet(0);
        Optional<T> resultOpt = rs.next() ? Optional.of(mapper.apply(rs)) : Optional.empty();

        log.info("YDB serializable read single SUCCESS — found: {}", resultOpt.isPresent());
        log.info("=== YDB SERIALIZABLE READ SINGLE END ===\n");
        return resultOpt;
    }
}