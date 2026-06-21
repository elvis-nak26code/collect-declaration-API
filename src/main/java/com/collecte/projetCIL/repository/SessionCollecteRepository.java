package com.collecte.projetCIL.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.collecte.projetCIL.models.SessionCollecte;

@Repository
public interface SessionCollecteRepository extends JpaRepository<SessionCollecte, Long> {

    // Charge le DPO en une seule requête (LEFT JOIN FETCH) pour toutes les
    // sessions, au lieu de déclencher un lazy-load de s.getDpo() par session
    // (N+1) quand on mappe la liste vers SessionCollecteResponse.
    @Query("SELECT s FROM SessionCollecte s LEFT JOIN FETCH s.dpo")
    List<SessionCollecte> findAllWithDpo();
}