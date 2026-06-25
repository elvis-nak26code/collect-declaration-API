package com.collecte.projetCIL.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.dto.response.UtilisateurResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Retourne TOUS les utilisateurs sans exception.
     * - Les usagers sont inclus même sans demande d'accès approuvée (ils se connectent directement).
     * - Les autres types d'utilisateurs sont inclus quel que soit le statut de leur demande.
     * Le frontend fait le tri selon les besoins de chaque section.
     */
    public List<UtilisateurResponse> listerTous() {
        return utilisateurRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MessageResponse suspendre(Long id) {
        Utilisateur u = trouverUtilisateur(id);
        verifierNonSupprime(u);
        u.setStatutUtilisateur(StatutUtilisateur.SUSPENDU);
        utilisateurRepository.save(u);
        return new MessageResponse("Utilisateur suspendu avec succès.");
    }

    public MessageResponse reactiver(Long id) {
        Utilisateur u = trouverUtilisateur(id);
        verifierNonSupprime(u);
        u.setStatutUtilisateur(StatutUtilisateur.ACTIF);
        utilisateurRepository.save(u);
        return new MessageResponse("Utilisateur réactivé avec succès.");
    }

    public MessageResponse desactiver(Long id) {
        Utilisateur u = trouverUtilisateur(id);
        verifierNonSupprime(u);
        u.setStatutUtilisateur(StatutUtilisateur.INACTIF);
        utilisateurRepository.save(u);
        return new MessageResponse("Utilisateur désactivé avec succès.");
    }

    public MessageResponse supprimer(Long id) {
        Utilisateur u = trouverUtilisateur(id);
        u.setStatutUtilisateur(StatutUtilisateur.SUPPRIME);
        utilisateurRepository.save(u);
        return new MessageResponse("Utilisateur supprimé avec succès.");
    }

    public MessageResponse changerStatut(Long id, StatutUtilisateur nouveauStatut) {
        Utilisateur u = trouverUtilisateur(id);
        u.setStatutUtilisateur(nouveauStatut);
        utilisateurRepository.save(u);
        return new MessageResponse("Statut mis à jour : " + nouveauStatut);
    }

    private Utilisateur trouverUtilisateur(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable avec l'id : " + id));
    }

    private void verifierNonSupprime(Utilisateur u) {
        if (StatutUtilisateur.SUPPRIME.equals(u.getStatutUtilisateur())) {
            throw new RuntimeException("Action impossible : l'utilisateur est déjà supprimé.");
        }
    }

    private UtilisateurResponse toResponse(Utilisateur u) {
        UtilisateurResponse r = new UtilisateurResponse();
        r.setId(u.getId());
        r.setNom(u.getNom());
        r.setPrenom(u.getPrenom());
        r.setEmail(u.getEmail());
        r.setStatutUtilisateur(u.getStatutUtilisateur());
        r.setTypeUtilisateur(u.getClass().getSimpleName());
        r.setDateCreation(u.getDateCreation());
        r.setDernierAcces(u.getDernierAcces());
        // Expose le statut de la demande pour que le frontend puisse filtrer côté client si besoin
        if (u.getDemandeAcces() != null) {
            r.setStatutDemandeAcces(u.getDemandeAcces().getStatutDemandeAcces());
        }
        return r;
    }
}