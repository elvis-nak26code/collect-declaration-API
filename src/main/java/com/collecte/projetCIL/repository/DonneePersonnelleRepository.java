package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DonneePersonnelle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonneePersonnelleRepository extends JpaRepository<DonneePersonnelle, Long> {

    /** Toutes les données d'un usager donné. */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.usager.id = :usagerId ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByUsagerId(@Param("usagerId") Long usagerId);

    /** Toutes les données d'un traitement donné. */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.traitement.idTraitement = :traitementId ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByTraitementId(@Param("traitementId") Long traitementId);

    /** Données d'un usager pour un traitement précis. */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.usager.id = :usagerId AND d.traitement.idTraitement = :traitementId")
    List<DonneePersonnelle> findByUsagerIdAndTraitementId(@Param("usagerId") Long usagerId, @Param("traitementId") Long traitementId);
}
