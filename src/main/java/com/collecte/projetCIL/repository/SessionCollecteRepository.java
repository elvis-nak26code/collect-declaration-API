package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.SessionCollecte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionCollecteRepository extends JpaRepository<SessionCollecte, Long> {
}
