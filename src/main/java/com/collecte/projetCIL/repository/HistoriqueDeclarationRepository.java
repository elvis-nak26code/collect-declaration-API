package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.HistoriqueDeclaration;

@Repository
public interface HistoriqueDeclarationRepository extends JpaRepository<HistoriqueDeclaration, Long> {

    /** Historique complet d'un DPO donné (toutes les déclarations qu'il a soumises), du plus récent au plus ancien. */
    @Query("SELECT h FROM HistoriqueDeclaration h " +
           "WHERE h.declaration.dpo.id = :dpoId " +
           "ORDER BY h.dateDeclaration DESC, h.idHistorique DESC")
    List<HistoriqueDeclaration> findByDpoId(@Param("dpoId") Long dpoId);

    /** Historique lié à une déclaration précise (utile pour tracer le cycle de vie d'une déclaration). */
    @Query("SELECT h FROM HistoriqueDeclaration h " +
           "WHERE h.declaration.idDeclaration = :declarationId " +
           "ORDER BY h.dateDeclaration DESC, h.idHistorique DESC")
    List<HistoriqueDeclaration> findByDeclarationId(@Param("declarationId") Long declarationId);

    /** Déclarations déjà traitées (validées ou rejetées) par un partenaire externe donné (clé API, pas de fiche CIL). */
    @Query("SELECT h FROM HistoriqueDeclaration h " +
           "WHERE h.declaration.cleApiCil.id = :cleApiCilId " +
           "ORDER BY h.dateDeclaration DESC, h.idHistorique DESC")
    List<HistoriqueDeclaration> findByCleApiCilId(@Param("cleApiCilId") Long cleApiCilId);
}