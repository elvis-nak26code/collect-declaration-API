package com.collecte.projetCIL.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DonneePersonnelleRequest {

    private String valeur;
    private LocalDateTime dateCollecte;
    private Long usagerId;
    private Long personneId;
    private Long typeDonneeId;
    private Long traitementId;
}
