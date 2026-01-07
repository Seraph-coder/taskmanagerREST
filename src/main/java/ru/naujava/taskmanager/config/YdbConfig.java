package ru.naujava.taskmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.ydb.auth.iam.CloudAuthHelper;
import tech.ydb.core.grpc.GrpcTransport;
import tech.ydb.table.SessionRetryContext;
import tech.ydb.table.TableClient;

@Configuration
public class YdbConfig {

    @Value("${ydb.endpoint}")
    private String endpoint;

    @Value("${ydb.database}")
    private String database;

    @Bean(destroyMethod = "close")
    public GrpcTransport grpcTransport() {
        // Аутентификация через метаданные контейнера (Yandex Cloud)
        var authProvider = CloudAuthHelper.getMetadataAuthProvider();

        return GrpcTransport.forEndpoint(endpoint, database)
                .withAuthProvider(authProvider)
                .build();
    }

    @Bean(destroyMethod = "close")
    public TableClient tableClient(GrpcTransport transport) {
        return TableClient.newClient(transport).build();
    }

    @Bean
    public SessionRetryContext sessionRetryContext(TableClient tableClient) {
        return SessionRetryContext.create(tableClient).build();
    }
}