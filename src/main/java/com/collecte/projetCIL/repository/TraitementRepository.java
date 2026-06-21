package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.Traitement;

@Repository
public interface TraitementRepository extends JpaRepository<Traitement, Long> {

    // Compte les traitements de TOUTES les sessions en une seule requête
    // (évite d'appeler s.getTraitements().size() pour chaque session, qui
    // déclenche une requête par session = N+1). Chaque Object[] = [idSession, count].
    @Query("SELECT t.sessionCollecte.idSession, COUNT(t) FROM Traitement t " +
           "WHERE t.sessionCollecte IS NOT NULL GROUP BY t.sessionCollecte.idSession")
    List<Object[]> countTraitementsParSession();
}