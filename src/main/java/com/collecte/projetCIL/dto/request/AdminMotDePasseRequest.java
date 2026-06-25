// src/main/java/com/collecte/projetCIL/dto/request/AdminMotDePasseRequest.java
package com.collecte.projetCIL.dto.request;

import lombok.Data;

@Data
public class AdminMotDePasseRequest {
    private String ancienMotDePasse;
    private String nouveauMotDePasse;
}