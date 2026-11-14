# AGY Session Handoff — Assistente AI per il Triage e la Riconciliazione Dati

> **Session ID**: `ecd3b9f5-19f9-4209-afa5-cad96efc0d27` (precedente: `5d7fac1e-365e-44fd-84c5-c399ea1ffdf7`)  
> **Workspace**: `/Users/simx/project/langChan4Spring`  
> **Stack**: Java 21 · Spring Boot 3.4.1 · LangChain4j 0.36.2 · Ollama · PostgreSQL 16 + `pgvector`

---

## 1. Obiettivo del Progetto

Applicazione web enterprise con **Spring Boot 3** e **LangChain4j** (approccio dichiarativo `@AiService`) che implementa un **Assistente AI per il Triage e la Riconciliazione Dati** tra CRM ed ERP.

L'assistente combina:
1. **Tool Calling (`@Tool`)** su database relazionale PostgreSQL (tramite Spring Data JPA) per ispezionare lo stato reale di sincronizzazione dei clienti (`CustomerRecord`).
2. **RAG (Retrieval-Augmented Generation)** su `pgvector` (con switch rapido a `InMemoryEmbeddingStore`) in cui viene indicizzato all'avvio il Runbook operativo per la gestione delle anomalie di sincronizzazione.
3. **Memoria Conversazionale (`ChatMemoryProvider`)** basata su `MessageWindowChatMemory` (20 messaggi) isolata per `@MemoryId`.
4. **Structured Output** tramite Java `record` (`DiagnosticReport`) annotato con `@Description`.

---

## 2. Mappa dei File Generati

| File | Ruolo e Responsabilità |
| :--- | :--- |
| `README.md` | Documentazione GitHub completa del progetto (architettura Mermaid, prerequisiti, quickstart, API reference, dataset e configurazione). Da mantenere allineata ad ogni modifica. |
| `agy.md` | Documento di handoff e stato avanzamento sessione per Antigravity. |
| `pom.xml` | Build Maven Java 21, Spring Boot 3.4.1, BOM `langchain4j-bom:0.36.2`, starter Web, Data JPA, PostgreSQL, `langchain4j-spring-boot-starter`, `langchain4j-ollama-spring-boot-starter`, `langchain4j-pgvector`. Flag `-parameters` attivo. |
| `docker-compose.yml` | Container `pgvector/pgvector:pg16` (`triage_db`, utente `triage_user`, password `triage_password`, porta `5432`). |
| `src/main/resources/application.yml` | Configurazione Datasource JPA, Ollama Chat Model (`qwen2.5-coder:7b` / `llama3.1:8b`), Ollama Embedding Model (`nomic-embed-text`, 768 dim) e parametri `app.pgvector.*`. |
| `src/main/java/com/example/triage/TriageAiApplication.java` | Entry-point `@SpringBootApplication`. |
| `src/main/java/com/example/triage/domain/CustomerRecord.java` | Entità JPA mappata sulla tabella `customer_records` (`id`, `codiceCliente`, `statoSincronizzazione`, `ultimoAggiornamento`, `noteErrore`). |
| `src/main/java/com/example/triage/repository/CustomerRecordRepository.java` | Repository Spring Data JPA con lookup case-insensitive `findByCodiceClienteIgnoreCase`. |
| `src/main/java/com/example/triage/config/DataLoader.java` | `CommandLineRunner` che popola 5 record di test (`CLI-1001` .. `CLI-1005`) se il DB è vuoto. |
| `src/main/java/com/example/triage/dto/DiagnosticReport.java` | Java `record` per lo Structured Output (`clientCode`, `severity`, `rootCause`, `recommendedAction`, `needsManualIntervention`). |
| `src/main/java/com/example/triage/ai/DiagnosticTools.java` | Componente Spring con metodo `@Tool` `getCustomerSyncStatus` per interrogare il DB reale. |
| `src/main/java/com/example/triage/config/AiConfig.java` | Bean Spring per `EmbeddingStore<TextSegment>` (`PgVectorEmbeddingStore` o `InMemoryEmbeddingStore`), `ContentRetriever` e `ChatMemoryProvider`. |
| `src/main/java/com/example/triage/config/GuidelinesIngestor.java` | `CommandLineRunner` che spezza e indicizza il Runbook operativo nell'`EmbeddingStore` all'avvio. |
| `src/main/java/com/example/triage/ai/SupportAssistant.java` | Interfaccia `@AiService` con i metodi `chat(@MemoryId, @UserMessage)` e `analyzeRecord(@UserMessage)`. |
| `src/main/java/com/example/triage/web/AssistantController.java` | REST Controller con gli endpoint `POST /api/assistant/chat` e `POST /api/assistant/analyze/{clientCode}`. |
| `src/main/resources/static/index.html` | Console web dark-mode a due pannelli per testare da browser sia la chat con memoria sia l'analisi strutturata JSON. |

---

## 3. Dati di Test Precaricati (`DataLoader`)

All'avvio vengono inseriti 5 record nella tabella `customer_records`:
- **`CLI-1001`**: `SYNCED` — Nessun errore (`LOW`, nessun intervento manuale).
- **`CLI-1002`**: `FAILED_SCHEMA` — `ERR-402`: Campo `partitaIva` mancante/invalido (`HIGH`, intervento manuale `true` verso Data Stewardship CRM).
- **`CLI-1003`**: `STALE_LOCK` — `ERR-504`: Timeout transazione distribuita su Kafka consumer group `erp-sync-cg` da >24h (`CRITICAL`, intervento manuale `true`, esecuzione `sp_release_stale_locks`).
- **`CLI-1004`**: `SYNCED` — Nessun errore (`LOW`, nessun intervento manuale).
- **`CLI-1005`**: `CONFLICT_VERSION` — `ERR-409`: `OptimisticLockException` tra locale (v12) e SAP (v15) (`MEDIUM`, intervento manuale `false`, procedura automatica `Force Pull from Master SAP`).

---

## 4. Comandi Rapidi per Riprendere il Lavoro

### Avvio Infrastruttura e Modelli
```bash
# 1. Avvia PostgreSQL + pgvector
docker compose up -d

# 2. Scarica i modelli Ollama richiesti (se non presenti)
ollama pull qwen2.5-coder:7b
ollama pull nomic-embed-text

# 3. Compila e avvia l'applicazione
mvn spring-boot:run
```

### Modalità Rapida senza pgvector (Solo In-Memory RAG)
Se si vuole testare il RAG senza creare la tabella vettoriale su Postgres, impostare in `src/main/resources/application.yml`:
```yaml
app:
  pgvector:
    enabled: false
```

### Esempi di Test via cURL
```bash
# Chat conversazionale con memoria
curl -X POST http://localhost:8080/api/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "operatore-01", "message": "Controlla il cliente CLI-1003 e dimmi cosa devo fare"}'

# Analisi strutturata (Structured Output -> DiagnosticReport)
curl -X POST http://localhost:8080/api/assistant/analyze/CLI-1002
```

---

## 5. Possibili Evoluzioni Future (Next Steps)

- **Azioni di Riconciliazione (`@Tool` di scrittura)**: aggiungere in `DiagnosticTools` metodi `@Tool` operativi (es. rilascio lock per `CLI-1003` o force-pull SAP per `CLI-1005`) che aggiornano lo stato del `CustomerRecord` a `SYNCED`.
- **Persistenza della ChatMemory**: sostituire l'attuale storage in-memory di `MessageWindowChatMemory` con un `ChatMemoryStore` persistito su PostgreSQL.
- **Test di Integrazione**: aggiungere test con Testcontainers (`pgvector/pgvector:pg16` + `OllamaContainer`).
