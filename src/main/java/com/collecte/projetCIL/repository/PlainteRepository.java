package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.Plainte;

@Repository
public interface PlainteRepository extends JpaRepository<Plainte, Long> {

    /** Plaintes envoyées par un usager. */
    @Query("SELECT p FROM Plainte p WHERE p.usager.id = :usagerId ORDER BY p.datePlainte DESC")
    List<Plainte> findByUsagerId(@Param("usagerId") Long usagerId);

    /** Plaintes traitées par la CIL (login interne). */
    @Query("SELECT p FROM Plainte p WHERE p.cil.id = :cilId ORDER BY p.datePlainte DESC")
    List<Plainte> findByCilId(@Param("cilId") Long cilId);

    /** Plaintes émises via le système externe de la CIL (clé API, pas de fiche CIL). */
    @Query("SELECT p FROM Plainte p WHERE p.cleApiCil.id = :cleApiCilId ORDER BY p.datePlainte DESC")
    List<Plainte> findByCleApiCilId(@Param("cleApiCilId") Long cleApiCilId);

    /** Toutes les plaintes non clôturées. */
    @Query("SELECT p FROM Plainte p WHERE p.statutPlainte != 'CLOTUREE' ORDER BY p.datePlainte ASC")
    List<Plainte> findNonCloturees();
}