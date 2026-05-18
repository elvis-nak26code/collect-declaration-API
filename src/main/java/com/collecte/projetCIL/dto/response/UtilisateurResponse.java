package com.collecte.projetCIL.dto.response;

import com.collecte.projetCIL.enums.StatutUtilisateur;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private StatutUtilisateur statutUtilisateur;
    private String typeUtilisateur;
    private LocalDateTime dateCreation;
}
