package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.DonneePersonnelle;

@Repository
public interface DonneePersonnelleRepository extends JpaRepository<DonneePersonnelle, Long> {

    @Query("SELECT d FROM DonneePersonnelle d WHERE d.usager.id = :usagerId ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByUsagerId(@Param("usagerId") Long usagerId);

    /**
     * Données visibles par l'usager : uniquement celles rattachées à un traitement.
     * Exclut les copies "entrepôt" (traitement = null) pour éviter les doublons
     * (une donnée peut exister à la fois dans son traitement et dans l'entrepôt
     * de dédoublonnage inter-traitements).
     */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.usager.id = :usagerId AND d.traitement IS NOT NULL ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByUsagerIdHorsEntrepot(@Param("usagerId") Long usagerId);

    @Query("SELECT d FROM DonneePersonnelle d WHERE d.personne.id = :personneId ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByPersonneId(@Param("personneId") Long personneId);

    /**
     * Données visibles (usager ou tableau de bord) : uniquement celles rattachées
     * à un traitement. Exclut les copies "entrepôt" (traitement = null) pour
     * éviter les doublons — une donnée existe à la fois dans son traitement et
     * dans l'entrepôt de dédoublonnage inter-traitements.
     */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.personne.id = :personneId AND d.traitement IS NOT NULL ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByPersonneIdHorsEntrepot(@Param("personneId") Long personneId);

    @Query("SELECT d FROM DonneePersonnelle d WHERE d.traitement.idTraitement = :traitementId ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findByTraitementId(@Param("traitementId") Long traitementId);

    @Query("SELECT d FROM DonneePersonnelle d WHERE d.usager.id = :usagerId AND d.traitement.idTraitement = :traitementId")
    List<DonneePersonnelle> findByUsagerIdAndTraitementId(@Param("usagerId") Long usagerId, @Param("traitementId") Long traitementId);

    @Query("SELECT d FROM DonneePersonnelle d WHERE d.personne.id = :personneId AND d.traitement.idTraitement = :traitementId")
    List<DonneePersonnelle> findByPersonneIdAndTraitementId(@Param("personneId") Long personneId, @Param("traitementId") Long traitementId);

    /**
     * Données de l'entrepôt : aucun traitement assigné.
     * Triées par date de collecte décroissante.
     */
    @Query("SELECT d FROM DonneePersonnelle d WHERE d.traitement IS NULL ORDER BY d.dateCollecte DESC")
    List<DonneePersonnelle> findEntrepot();
}