package com.example.triage.repository;

import com.example.triage.domain.CustomerRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRecordRepository extends JpaRepository<CustomerRecord, Long> {

    Optional<CustomerRecord> findByCodiceClienteIgnoreCase(String codiceCliente);
}
