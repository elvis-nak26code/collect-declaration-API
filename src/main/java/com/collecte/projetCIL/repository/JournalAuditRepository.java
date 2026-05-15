package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.JournalAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalAuditRepository extends JpaRepository<JournalAudit, Long> {
}
