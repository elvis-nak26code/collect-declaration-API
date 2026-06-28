package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.models.DemandeAcces;

@Repository
public interface DemandeAccesRepository extends JpaRepository<DemandeAcces, Long> {

    // Ancienne méthode conservée pour compatibilité
    List<DemandeAcces> findByStatutDemandeAcces(StatutDemandeAcces statut);

    // Nouvelle : charge utilisateur + administrateur en UNE seule requête SQL
    @Query("SELECT d FROM DemandeAcces d " +
           "LEFT JOIN FETCH d.utilisateur " +
           "LEFT JOIN FETCH d.administrateur " +
           "WHERE d.statutDemandeAcces = :statut")
    List<DemandeAcces> findByStatutWithFetch(@Param("statut") StatutDemandeAcces statut);

    // Nouvelle : idem pour toutes les demandes
    @Query("SELECT d FROM DemandeAcces d " +
           "LEFT JOIN FETCH d.utilisateur " +
           "LEFT JOIN FETCH d.administrateur")
    List<DemandeAcces> findAllWithFetch();
}