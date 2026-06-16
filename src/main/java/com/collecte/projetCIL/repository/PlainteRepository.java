package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.Plainte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlainteRepository extends JpaRepository<Plainte, Long> {

    /** Plaintes envoyées par un usager. */
    @Query("SELECT p FROM Plainte p WHERE p.usager.id = :usagerId ORDER BY p.datePlainte DESC")
    List<Plainte> findByUsagerId(@Param("usagerId") Long usagerId);

    /** Plaintes traitées par la CIL. */
    @Query("SELECT p FROM Plainte p WHERE p.cil.id = :cilId ORDER BY p.datePlainte DESC")
    List<Plainte> findByCilId(@Param("cilId") Long cilId);

    /** Toutes les plaintes non clôturées. */
    @Query("SELECT p FROM Plainte p WHERE p.statutPlainte != 'CLOTUREE' ORDER BY p.datePlainte ASC")
    List<Plainte> findNonCloturees();
}
