package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.enums.StatutDeclaration;
import com.collecte.projetCIL.models.Declaration;

@Repository
public interface DeclarationRepository extends JpaRepository<Declaration, Long> {

    /** Toutes les déclarations d'un DPO donné. */
    @Query("SELECT d FROM Declaration d WHERE d.dpo.id = :dpoId")
    List<Declaration> findByDpoId(@Param("dpoId") Long dpoId);

    /** Déclarations par statut. */
    @Query("SELECT d FROM Declaration d WHERE d.statut = :statut")
    List<Declaration> findByStatut(@Param("statut") StatutDeclaration statut);

    /**
     * Déclarations EN_ATTENTE créées manuellement par un DPO (origine MANUELLE).
     * Exclut à la fois les brouillons auto-générés par les traitements (dpo null)
     * ET les déclarations auto-générées avec un traitement même si elles ont été
     * soumises par la suite (origine AUTOMATIQUE) : seules celles créées explicitement
     * par le DPO via "Déclarer" doivent apparaître dans la file d'attente du DG.
     */
    @Query("SELECT d FROM Declaration d WHERE d.statut = 'EN_ATTENTE' AND d.dpo IS NOT NULL "
            + "AND d.origineDeclaration = 'MANUELLE' "
            + "ORDER BY d.dateSoumission ASC")
    List<Declaration> findEnAttenteAvecDpo();

    /** Déclarations en attente, triées par date de soumission. */
    @Query("SELECT d FROM Declaration d WHERE d.statut = 'EN_ATTENTE' ORDER BY d.dateSoumission ASC")
    List<Declaration> findEnAttenteOrderByDate();

    /** Déclaration liée à un traitement donné (s'il en existe une). */
    @Query("SELECT d FROM Declaration d WHERE d.traitement.idTraitement = :traitementId")
    java.util.Optional<Declaration> findByTraitementId(@Param("traitementId") Long traitementId);

    // Dans DeclarationRepository
   List<Declaration> findAllByTraitement_IdTraitementOrderByDateSoumissionDesc(Long traitementId);
}