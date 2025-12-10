package com.SkillCatalogService.skillservice.openSearch;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchConfig {

    @Bean
    public ObjectMapper openSearchObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public OpenSearchClient openSearchClient(
            @Value("${opensearch.host}") String host,
            @Value("${opensearch.port}") int port,
            @Value("${opensearch.scheme}") String scheme,
            ObjectMapper openSearchObjectMapper) {

        RestClient restClient = RestClient.builder(
                new HttpHost(host, port, scheme)
        ).build();

        return new OpenSearchClient(
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper(openSearchObjectMapper)
                )
        );
    }
}
