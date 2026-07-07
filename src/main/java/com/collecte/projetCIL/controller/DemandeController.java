package com.collecte.projetCIL.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.collecte.projetCIL.dto.request.DemandeRequest;
import com.collecte.projetCIL.dto.response.DemandeResponse;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.repository.UsagerRepository;
import com.collecte.projetCIL.service.DemandeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;
    private final UsagerRepository usagerRepository;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> soumettreDemande(
            @RequestBody DemandeRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        Usager usager = usagerRepository.findUsagerByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usager introuvable pour : " + email));

        request.setUsagerId(usager.getId());
        return ResponseEntity.ok(demandeService.soumettreDemandeUsager(request));
    }

    @GetMapping("/par-usager")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> parUsager(@RequestParam Long usagerId) {
        return ResponseEntity.ok(demandeService.listerParUsager(usagerId));
    }

    @GetMapping("/par-personne")
    @PreAuthorize("hasAnyAuthority('ROLE_USAGER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> parPersonne(@RequestParam Long personneId) {
        return ResponseEntity.ok(demandeService.listerParPersonne(personneId));
    }

    @GetMapping("/par-um")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> parUm(@RequestParam Long umId) {
        return ResponseEntity.ok(demandeService.listerParUtilisateurMetier(umId));
    }

    @GetMapping("/en-attente")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<List<DemandeResponse>> enAttente(@RequestParam Long umId) {
        return ResponseEntity.ok(demandeService.listerEnAttentePourUm(umId));
    }

    @PutMapping("/{id}/accepter")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> accepter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(demandeService.accepterDemande(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}/rejeter")
    @PreAuthorize("hasAnyAuthority('ROLE_UTILISATEUR_METIER','ROLE_ADMINISTRATEUR')")
    public ResponseEntity<DemandeResponse> rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String motif = body.getOrDefault("motifRejet", "Aucun motif fourni.");
        return ResponseEntity.ok(demandeService.rejeterDemande(id, userDetails.getUsername(), motif));
    }
}