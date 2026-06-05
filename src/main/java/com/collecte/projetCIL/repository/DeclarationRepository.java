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

    /** Déclarations en attente, triées par date de soumission. */
    @Query("SELECT d FROM Declaration d WHERE d.statut = 'EN_ATTENTE' ORDER BY d.dateSoumission ASC")
    List<Declaration> findEnAttenteOrderByDate();
}