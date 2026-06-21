package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class PersonneRequest {

    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String numeroIdentite;
}
