package com.collecte.projetCIL.service;

import java.util.Objects;

import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.DemandeAcces;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.DemandeAccesRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeAccesService {

    private final DemandeAccesRepository demandeAccesRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Lister toutes les demandes en attente
    public List<DemandeAccesResponse> listerEnAttente() {
        return demandeAccesRepository.findByStatutDemandeAcces(StatutDemandeAcces.EN_ATTENTE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Lister toutes les demandes (toutes statuts)
    public List<DemandeAccesResponse> listerToutes() {
        return demandeAccesRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Valider une demande -> compte devient ACTIF
    @Transactional
    public MessageResponse valider(Long demandeId) {
        DemandeAcces demande = demandeAccesRepository.findById(Objects.requireNonNull(demandeId))
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + demandeId));

        if (demande.getStatutDemandeAcces() != StatutDemandeAcces.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        // Activer le compte utilisateur
        Utilisateur utilisateur = demande.getUtilisateur();
        utilisateur.setStatutUtilisateur(StatutUtilisateur.ACTIF);
        utilisateurRepository.save(utilisateur);

        // Mettre à jour la demande
        demande.setStatutDemandeAcces(StatutDemandeAcces.APPROUVEE);
        demande.setDateValidation(LocalDateTime.now());
        demandeAccesRepository.save(demande);

        return new MessageResponse("Compte de " + utilisateur.getEmail() + " activé avec succès.");
    }

    // Rejeter une demande -> compte reste INACTIF
    @Transactional
    public MessageResponse rejeter(Long demandeId, String motif) {
        DemandeAcces demande = demandeAccesRepository.findById(Objects.requireNonNull(demandeId))
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + demandeId));

        if (demande.getStatutDemandeAcces() != StatutDemandeAcces.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        // Marquer le compte comme suspendu
        Utilisateur utilisateur = demande.getUtilisateur();
        utilisateur.setStatutUtilisateur(StatutUtilisateur.SUSPENDU);
        utilisateurRepository.save(utilisateur);

        // Mettre à jour la demande
        demande.setStatutDemandeAcces(StatutDemandeAcces.REJETEE);
        demande.setMotif(motif);
        demande.setDateValidation(LocalDateTime.now());
        demandeAccesRepository.save(demande);

        return new MessageResponse("Demande de " + utilisateur.getEmail() + " rejetée.");
    }

    private DemandeAccesResponse toResponse(DemandeAcces d) {
        DemandeAccesResponse r = new DemandeAccesResponse();
        r.setId(d.getIdDemande());
        r.setStatut(d.getStatutDemandeAcces());
        r.setDateDemande(d.getDateDemande());
        r.setDateValidation(d.getDateValidation());
        r.setMotif(d.getMotif());
        if (d.getUtilisateur() != null) {
            Utilisateur u = d.getUtilisateur();
            r.setNomUtilisateur(u.getNom());
            r.setPrenomUtilisateur(u.getPrenom());
            r.setEmailUtilisateur(u.getEmail());
            r.setTypeUtilisateur(u.getClass().getSimpleName());
        }
        return r;
    }
}
