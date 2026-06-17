package com.collecte.projetCIL.controller;

import com.collecte.projetCIL.dto.response.DemandeResponse;
import com.collecte.projetCIL.dto.response.DonneePersonnelleResponse;
import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import com.collecte.projetCIL.service.DemandeService;
import com.collecte.projetCIL.service.DonneePersonnelleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mon-compte")
@RequiredArgsConstructor
public class MonCompteController {

    private final UtilisateurRepository utilisateurRepository;
    private final DonneePersonnelleService donneePersonnelleService;
    private final DemandeService demandeService;

    private Utilisateur getUtilisateur(Authentication auth) {
        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @GetMapping("/personne")
    public ResponseEntity<?> getMaPersonne(Authentication authentication) {
        Utilisateur utilisateur = getUtilisateur(authentication);
        Personne personne = utilisateur.getPersonne();
        if (personne == null) {
            return ResponseEntity.ok(Map.of("message", "Aucune personne associée à ce compte."));
        }
        return ResponseEntity.ok(personne);
    }

    @GetMapping("/donnees")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_UTILISATEUR_METIER','ROLE_DPO','ROLE_CIL','ROLE_DG','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DonneePersonnelleResponse>> mesDonnees(Authentication authentication) {
        Utilisateur utilisateur = getUtilisateur(authentication);
        Personne personne = utilisateur.getPersonne();
        if (personne == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(donneePersonnelleService.listerParPersonne(personne.getId()));
    }

    @GetMapping("/demandes")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> mesDemandes(Authentication authentication) {
        Utilisateur utilisateur = getUtilisateur(authentication);
        Personne personne = utilisateur.getPersonne();
        if (personne == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(demandeService.listerParPersonne(personne.getId()));
    }
}
