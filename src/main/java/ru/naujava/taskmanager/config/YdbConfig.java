package ru.naujava.taskmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.ydb.auth.iam.CloudAuthHelper;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.TableClient;

/**
 * Конфигурация подключения к YDB.
 */
@Configuration
public class YdbConfig {

    @Value("${ydb.endpoint}")
    private String endpoint;

    @Value("${ydb.database}")
    private String database;

    @Value("${ydb.auth.sa-key-file}")
    private String saKeyFile;

    /**
     * Создает GrpcTransport для подключения к YDB.
     */
    @Bean(destroyMethod = "close")
    public GrpcTransport grpcTransport() {
        var authProvider = CloudAuthHelper.getServiceAccountFileAuthProvider(saKeyFile);

        return GrpcTransport.forEndpoint(endpoint, database)
                .withAuthProvider(authProvider)
                .build();
    }

    /**
     * Создает TableClient для работы с таблицами YDB.
     */
    @Bean(destroyMethod = "close")
    public TableClient tableClient(GrpcTransport transport) {
        return TableClient.newClient(transport).build();
    }

    /**
     * Создает SessionRetryContext для автоматических повторов при ошибках.
     * Это основной бин для работы с YDB - передавайте его в репозитории.
     */
    @Bean
    public SessionRetryContext sessionRetryContext(TableClient tableClient) {
        return SessionRetryContext.create(tableClient).build();
    }
}