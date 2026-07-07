package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class ReinitialiserMotDePasseRequest {
    private String token;
    private String nouveauMotDePasse;
}
