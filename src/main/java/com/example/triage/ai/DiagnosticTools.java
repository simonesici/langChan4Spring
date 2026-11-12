package com.example.triage.ai;

import com.example.triage.repository.CustomerRecordRepository;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticTools {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticTools.class);
    private final CustomerRecordRepository repository;

    public DiagnosticTools(CustomerRecordRepository repository) {
        this.repository = repository;
    }

    @Tool("Cerca un CustomerRecord nel database tramite il codiceCliente (es. CLI-1001) e restituisce lo stato reale di sincronizzazione, l'ultimo aggiornamento e le note di errore")
    public String getCustomerSyncStatus(
            @P("Il codice univoco del cliente da verificare, es. CLI-1002") String codiceCliente
    ) {
        log.info("[TOOL INVOCATION] Ricerca CustomerRecord per codiceCliente='{}'", codiceCliente);
        return repository.findByCodiceClienteIgnoreCase(codiceCliente.trim())
                .map(record -> String.format(
                        "Record trovato -> codiceCliente: %s | statoSincronizzazione: %s | ultimoAggiornamento: %s | noteErrore: %s",
                        record.getCodiceCliente(),
                        record.getStatoSincronizzazione(),
                        record.getUltimoAggiornamento(),
                        record.getNoteErrore() != null ? record.getNoteErrore() : "Nessun errore registrato"
                ))
                .orElse("Nessun CustomerRecord trovato nel database per il codice cliente: " + codiceCliente);
    }
}
