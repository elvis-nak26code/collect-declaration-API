package com.collecte.projetCIL.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TypeDonneeResponse {

    private Long idTypeDonnee;
    private String nom;
    private Boolean sensible;
}
