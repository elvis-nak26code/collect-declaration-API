package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DPORepository extends JpaRepository<DPO, Long> {
}
