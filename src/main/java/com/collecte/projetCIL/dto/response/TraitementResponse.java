package com.collecte.projetCIL.dto.response;

import java.time.LocalDateTime;

import com.collecte.projetCIL.enums.StatutTraitement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TraitementResponse {

    private Long idTraitement;
    private String nom;                  // ← ajouté
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
    private Long declarationId;
    private StatutTraitement statut;
    private Boolean envoyeAuDpo;
    private LocalDateTime dateEnvoiDpo;
}