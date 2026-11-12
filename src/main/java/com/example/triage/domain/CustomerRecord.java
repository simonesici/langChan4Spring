package com.example.triage.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_records")
public class CustomerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codice_cliente", nullable = false, unique = true, length = 32)
    private String codiceCliente;

    @Column(name = "stato_sincronizzazione", nullable = false, length = 32)
    private String statoSincronizzazione;

    @Column(name = "ultimo_aggiornamento", nullable = false)
    private LocalDateTime ultimoAggiornamento;

    @Column(name = "note_errore", length = 1000)
    private String noteErrore;

    public CustomerRecord() {
    }

    public CustomerRecord(String codiceCliente, String statoSincronizzazione, LocalDateTime ultimoAggiornamento, String noteErrore) {
        this.codiceCliente = codiceCliente;
        this.statoSincronizzazione = statoSincronizzazione;
        this.ultimoAggiornamento = ultimoAggiornamento;
        this.noteErrore = noteErrore;
    }

    public Long getId() {
        return id;
    }

    public String getCodiceCliente() {
        return codiceCliente;
    }

    public void setCodiceCliente(String codiceCliente) {
        this.codiceCliente = codiceCliente;
    }

    public String getStatoSincronizzazione() {
        return statoSincronizzazione;
    }

    public void setStatoSincronizzazione(String statoSincronizzazione) {
        this.statoSincronizzazione = statoSincronizzazione;
    }

    public LocalDateTime getUltimoAggiornamento() {
        return ultimoAggiornamento;
    }

    public void setUltimoAggiornamento(LocalDateTime ultimoAggiornamento) {
        this.ultimoAggiornamento = ultimoAggiornamento;
    }

    public String getNoteErrore() {
        return noteErrore;
    }

    public void setNoteErrore(String noteErrore) {
        this.noteErrore = noteErrore;
    }

    @Override
    public String toString() {
        return "CustomerRecord{" +
                "id=" + id +
                ", codiceCliente='" + codiceCliente + '\'' +
                ", statoSincronizzazione='" + statoSincronizzazione + '\'' +
                ", ultimoAggiornamento=" + ultimoAggiornamento +
                ", noteErrore='" + noteErrore + '\'' +
                '}';
    }
}
