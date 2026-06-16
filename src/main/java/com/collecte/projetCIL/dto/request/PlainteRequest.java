package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class PlainteRequest {

    /** ID du CIL qui émet la plainte vers le DPO. */
    private Long cilId;

    /** Objet de la plainte. */
    private String objetPlainte;

    /** Description détaillée. */
    private String descriptionPlainte;

    /** Lieu concerné (optionnel). */
    private String lieu;
}
