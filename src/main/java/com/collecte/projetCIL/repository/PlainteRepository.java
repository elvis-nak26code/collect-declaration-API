package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.Plainte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlainteRepository extends JpaRepository<Plainte, Long> {
}
