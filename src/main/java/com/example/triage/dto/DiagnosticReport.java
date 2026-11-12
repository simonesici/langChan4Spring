package com.example.triage.dto;

import dev.langchain4j.model.output.structured.Description;

public record DiagnosticReport(
        @Description("Codice identificativo del cliente analizzato, es. CLI-1002")
        String clientCode,

        @Description("Livello di gravità dell'anomalia: LOW, MEDIUM, HIGH, CRITICAL")
        String severity,

        @Description("Causa radice tecnica individuata incrociando stato DB e linee guida operative")
        String rootCause,

        @Description("Azione correttiva raccomandata secondo le procedure operative")
        String recommendedAction,

        @Description("True se è richiesto l'intervento umano di un operatore, false se risolvibile automaticamente o già allineato")
        boolean needsManualIntervention
) {
}
