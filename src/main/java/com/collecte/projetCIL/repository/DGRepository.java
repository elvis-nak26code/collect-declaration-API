package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DGRepository extends JpaRepository<DG, Long> {
}
