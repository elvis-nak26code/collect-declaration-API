
package com.collecte.projetCIL.dto.response;
 
import java.time.LocalDateTime;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;

import lombok.Data;
 
@Data
public class UtilisateurResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private StatutUtilisateur statutUtilisateur;
    private String typeUtilisateur;
    private LocalDateTime dateCreation;
    private LocalDateTime dernierAcces;       // ← pour lastLogin dans le frontend
    private StatutDemandeAcces statutDemandeAcces; // ← pour filtrer côté frontend
}