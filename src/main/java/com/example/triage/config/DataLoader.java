package com.example.triage.config;

import com.example.triage.domain.CustomerRecord;
import com.example.triage.repository.CustomerRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final CustomerRecordRepository repository;

    public DataLoader(CustomerRecordRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("Database già popolato ({} record presenti). Skip DataLoader.", repository.count());
            return;
        }

        List<CustomerRecord> initialRecords = List.of(
                new CustomerRecord(
                        "CLI-1001",
                        "SYNCED",
                        LocalDateTime.now().minusMinutes(15),
                        null
                ),
                new CustomerRecord(
                        "CLI-1002",
                        "FAILED_SCHEMA",
                        LocalDateTime.now().minusHours(3),
                        "ERR-402: Campo 'partitaIva' mancante o formato non valido nel payload CRM verso ERP."
                ),
                new CustomerRecord(
                        "CLI-1003",
                        "STALE_LOCK",
                        LocalDateTime.now().minusHours(28),
                        "ERR-504: Timeout transazione distribuita su Kafka consumer group 'erp-sync-cg'; lock pessimistico non rilasciato."
                ),
                new CustomerRecord(
                        "CLI-1004",
                        "SYNCED",
                        LocalDateTime.now().minusHours(1),
                        null
                ),
                new CustomerRecord(
                        "CLI-1005",
                        "CONFLICT_VERSION",
                        LocalDateTime.now().minusMinutes(45),
                        "ERR-409: OptimisticLockException - versione record locale (v12) inferiore alla versione remota SAP (v15)."
                )
        );

        repository.saveAll(initialRecords);
        log.info("Caricati {} CustomerRecord di esempio nel database.", initialRecords.size());
    }
}
