package com.example.triage.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(
            @Value("${app.pgvector.enabled:true}") boolean pgVectorEnabled,
            @Value("${app.pgvector.host:localhost}") String host,
            @Value("${app.pgvector.port:5432}") int port,
            @Value("${app.pgvector.database:triage_db}") String database,
            @Value("${app.pgvector.user:triage_user}") String user,
            @Value("${app.pgvector.password:triage_password}") String password,
            @Value("${app.pgvector.table:operational_guidelines_embeddings}") String table,
            @Value("${app.pgvector.dimension:768}") int dimension
    ) {
        if (pgVectorEnabled) {
            log.info("Inizializzazione PgVectorEmbeddingStore su {}:{}/{} (tabella: {})", host, port, database, table);
            return PgVectorEmbeddingStore.builder()
                    .host(host)
                    .port(port)
                    .database(database)
                    .user(user)
                    .password(password)
                    .table(table)
                    .dimension(dimension)
                    .createTable(true)
                    .dropTableFirst(true) // Ricrea all'avvio per evitare duplicati nello scaffolding demo
                    .build();
        }

        // Alternativa rapida in-memory (impostare app.pgvector.enabled=false in application.yml)
        log.info("Inizializzazione InMemoryEmbeddingStore per avvio rapido");
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel
    ) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .build();
    }
}
