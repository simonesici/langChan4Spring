package com.example.triage.web;

import com.example.triage.ai.SupportAssistant;
import com.example.triage.dto.DiagnosticReport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final SupportAssistant supportAssistant;

    public AssistantController(SupportAssistant supportAssistant) {
        this.supportAssistant = supportAssistant;
    }

    public record ChatRequest(String sessionId, String message) {
    }

    public record ChatResponse(String sessionId, String reply) {
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? "default-session"
                : request.sessionId();
        String reply = supportAssistant.chat(sessionId, request.message());
        return ResponseEntity.ok(new ChatResponse(sessionId, reply));
    }

    @PostMapping("/analyze/{clientCode}")
    public ResponseEntity<DiagnosticReport> analyze(@PathVariable String clientCode) {
        String prompt = "Analizza lo stato di sincronizzazione del cliente con codice " + clientCode +
                " verificandolo nel database e restituisci il report diagnostico strutturato secondo le linee guida operative.";
        DiagnosticReport report = supportAssistant.analyzeRecord(prompt);
        return ResponseEntity.ok(report);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        return ResponseEntity.internalServerError().body(Map.of(
                "error", ex.getClass().getSimpleName(),
                "message", ex.getMessage() != null ? ex.getMessage() : "Errore durante l'elaborazione AI"
        ));
    }
}
