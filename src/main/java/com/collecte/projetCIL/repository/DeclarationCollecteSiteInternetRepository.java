package com.collecte.projetCIL.repository;

import com.collecte.projetCIL.models.DeclarationCollecteSiteInternet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeclarationCollecteSiteInternetRepository extends JpaRepository<DeclarationCollecteSiteInternet, Long> {
}
