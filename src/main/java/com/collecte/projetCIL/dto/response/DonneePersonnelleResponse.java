package com.collecte.projetCIL.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonneePersonnelleResponse {

    private Long idDonnee;
    private String valeur;
    private LocalDateTime dateCollecte;

    private Long usagerId;
    private String usagerNomComplet;

    private Long personneId;
    private String personneNomComplet;

    private Long typeDonneeId;
    private String typeDonneeNom;
    private Boolean typeDonneeSensible;

    private Long traitementId;
    private String traitementNom;
}
