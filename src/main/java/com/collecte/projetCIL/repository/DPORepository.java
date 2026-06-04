package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DPORepository extends JpaRepository<DPO, Long> {

    @Query("SELECT d FROM DPO d WHERE d.email = :email")
    Optional<DPO> findByEmail(@Param("email") String email);
}