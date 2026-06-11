package com.collecte.projetCIL.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraitementResponse {

    private Long idTraitement;
    private String department;
    private String description;
    private String texte;
    private String certificationSecurite;
    private Integer dureeConservation;
    private LocalDateTime dateCreation;
    private LocalDateTime dateFin;
    private Long nombreDonnee;
    private Long sessionCollecteId;
    private Long utilisateurMetierId;
    private String utilisateurMetierNom;

    // ID de la déclaration auto-créée (utile pour le frontend)
    private Long declarationId;
}