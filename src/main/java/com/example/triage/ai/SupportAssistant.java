package com.example.triage.ai;

import com.example.triage.dto.DiagnosticReport;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface SupportAssistant {

    @SystemMessage("""
            Sei un Senior AI Assistant specializzato nel Triage e nella Riconciliazione Dati tra CRM ed ERP.
            Segui rigorosamente queste regole:
            1. Quando ti viene chiesto lo stato o la diagnosi di un cliente (es. CLI-1001, CLI-1002), utilizza SEMPRE il tool `getCustomerSyncStatus` per interrogare il database reale prima di rispondere. Non inventare mai stati o errori.
            2. Consulta le linee guida operative (Runbook) recuperate dal contesto RAG per determinare la gravità (severity: LOW, MEDIUM, HIGH, CRITICAL), la causa radice (rootCause), l'azione raccomandata (recommendedAction) e se serve intervento umano (needsManualIntervention).
            3. Rispondi in modo chiaro, tecnico e conciso in lingua italiana.
            """)
    String chat(@MemoryId String sessionId, @UserMessage String message);

    @SystemMessage("""
            Sei un motore diagnostico per il Triage e la Riconciliazione Dati.
            Utilizza OBBLIGATORIAMENTE il tool `getCustomerSyncStatus` per recuperare i dati reali del cliente richiesto dal database.
            Incrocia i dati reali del record con il Runbook operativo presente nel contesto per compilare accuratamente tutti i campi del DiagnosticReport:
            - clientCode: il codice cliente analizzato
            - severity: LOW, MEDIUM, HIGH oppure CRITICAL
            - rootCause: sintesi tecnica della causa radice
            - recommendedAction: procedura operativa raccomandata dal Runbook
            - needsManualIntervention: true se il Runbook richiede intervento umano, false altrimenti
            """)
    DiagnosticReport analyzeRecord(@UserMessage String prompt);
}
