package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutPlainte;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlainteResponse {

    private Long idPlainte;
    private LocalDate datePlainte;
    private String lieu;
    private String objetPlainte;
    private String descriptionPlainte;
    private StatutPlainte statutPlainte;
    private String decisionCil;
    private LocalDate dateDecision;

    // Emetteur (CIL ou Usager)
    private Long cilId;
    private String cilNomComplet;

    // Destinataire DPO (pour plaintes CIL→DPO)
    private Long dpoId;
    private String dpoNomComplet;
}
