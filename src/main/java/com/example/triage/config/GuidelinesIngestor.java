package com.example.triage.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GuidelinesIngestor implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GuidelinesIngestor.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    public GuidelinesIngestor(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) {
        String operationalGuidelines = """
                RUNBOOK OPERATIVO - GESTIONE ANOMALIE DI SINCRONIZZAZIONE DATI (CRM <-> ERP)
                
                1. STATO: SYNCED
                   - Gravità (Severity): LOW
                   - Causa: Il record è correttamente allineato tra CRM ed ERP.
                   - Azione Raccomandata: Nessuna azione richiesta. Monitoraggio standard.
                   - Intervento Manuale: false (NON necessario).

                2. STATO: FAILED_SCHEMA (Codice Errore ERR-402)
                   - Gravità (Severity): HIGH
                   - Causa: Scarto di validazione formale sul payload JSON/XML (es. Partita IVA o Codice Fiscale mancante/invalido).
                   - Azione Raccomandata: Aprire ticket al team Data Stewardship CRM per la bonifica anagrafica del campo mancante e rilanciare il job di sincronizzazione manuale.
                   - Intervento Manuale: true (NECESSARIO intervento operatore).

                3. STATO: STALE_LOCK (Codice Errore ERR-504)
                   - Gravità (Severity): CRITICAL
                   - Causa: Lock pessimistico orfano causato da timeout della transazione distribuita sul consumer Kafka 'erp-sync-cg' da oltre 24 ore.
                   - Azione Raccomandata: Eseguire la stored procedure `sp_release_stale_locks(codice_cliente)` sul DB di staging e riavviare l'offset del consumer Kafka.
                   - Intervento Manuale: true (NECESSARIO intervento SRE/DBA).

                4. STATO: CONFLICT_VERSION (Codice Errore ERR-409)
                   - Gravità (Severity): MEDIUM
                   - Causa: Disallineamento di versione (OptimisticLockException) tra il record locale e il sistema master SAP.
                   - Azione Raccomandata: Innescare la procedura automatica di 'Force Pull from Master SAP' tramite endpoint di riconciliazione `/internal/reconcile/pull-master`.
                   - Intervento Manuale: false (risolvibile tramite riconciliazione automatica Master-Wins).
                """;

        try {
            Document document = Document.from(
                    operationalGuidelines,
                    Metadata.from("source", "runbook-sincronizzazione-v2.4")
            );

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(500, 50))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);
            log.info("Linee guida operative indicizzate con successo nell'EmbeddingStore.");
        } catch (Exception e) {
            log.warn("Impossibile indicizzare le linee guida all'avvio (verifica che Ollama sia attivo con il modello embedding configurato): {}", e.getMessage());
        }
    }
}
