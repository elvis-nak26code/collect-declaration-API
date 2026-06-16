package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.enums.StatutDemande;
import com.collecte.projetCIL.models.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    /** Demandes d'un usager. */
    @Query("SELECT d FROM Demande d WHERE d.usager.id = :usagerId ORDER BY d.dateDemande DESC")
    List<Demande> findByUsagerId(@Param("usagerId") Long usagerId);

    /** Demandes reçues par un UtilisateurMetier. */
    @Query("SELECT d FROM Demande d WHERE d.utilisateurMetier.id = :umId ORDER BY d.dateDemande DESC")
    List<Demande> findByUtilisateurMetierId(@Param("umId") Long umId);

    /** Demandes en attente pour un UtilisateurMetier. */
    @Query("SELECT d FROM Demande d WHERE d.utilisateurMetier.id = :umId AND d.statutDemande = :statut ORDER BY d.dateDemande ASC")
    List<Demande> findByUtilisateurMetierIdAndStatut(@Param("umId") Long umId, @Param("statut") StatutDemande statut);
}
