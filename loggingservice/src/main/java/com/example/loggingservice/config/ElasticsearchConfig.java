package com.example.loggingservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.loggingservice.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5s}")
    private String connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:60s}")
    private String socketTimeout;

    @Value("${elasticsearch.index.name:logs}")
    private String indexName;

    @Bean(name = "elasticsearchIndexName")
    public String elasticsearchIndexName() {
        return indexName;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        String[] uriParts = elasticsearchUris.replace("http://", "").replace("https://", "").split(":");
        String host = uriParts[0];
        int port = uriParts.length > 1 ? Integer.parseInt(uriParts[1]) : 9200;

        return ClientConfiguration.builder()
                .connectedTo(host + ":" + port)
                .withBasicAuth(username, password)
                .withConnectTimeout(Duration.parse("PT" + connectionTimeout))
                .withSocketTimeout(Duration.parse("PT" + socketTimeout))
                .build();
    }
}

