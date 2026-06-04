package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.response.DemandeAccesResponse;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.*;
import com.collecte.projetCIL.repository.*;
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

    // Repositories spécifiques pour préserver les tables enfants JPA
    private final DPORepository dpoRepository;
    private final CILRepository cilRepository;
    private final DGRepository dgRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;
    private final UsagerRepository usagerRepository;

    public List<DemandeAccesResponse> listerEnAttente() {
        return demandeAccesRepository.findByStatutDemandeAcces(StatutDemandeAcces.EN_ATTENTE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DemandeAccesResponse> listerToutes() {
        return demandeAccesRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse valider(Long demandeId) {
        DemandeAcces demande = demandeAccesRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + demandeId));

        if (demande.getStatutDemandeAcces() != StatutDemandeAcces.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        Utilisateur utilisateur = demande.getUtilisateur();
        utilisateur.setStatutUtilisateur(StatutUtilisateur.ACTIF);
        sauvegarderUtilisateur(utilisateur);

        demande.setStatutDemandeAcces(StatutDemandeAcces.APPROUVEE);
        demande.setDateValidation(LocalDateTime.now());
        demandeAccesRepository.save(demande);

        return new MessageResponse("Compte de " + utilisateur.getEmail() + " activé avec succès.");
    }

    @Transactional
    public MessageResponse rejeter(Long demandeId, String motif) {
        DemandeAcces demande = demandeAccesRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + demandeId));

        if (demande.getStatutDemandeAcces() != StatutDemandeAcces.EN_ATTENTE) {
            throw new RuntimeException("Cette demande a déjà été traitée.");
        }

        Utilisateur utilisateur = demande.getUtilisateur();
        utilisateur.setStatutUtilisateur(StatutUtilisateur.SUSPENDU);
        sauvegarderUtilisateur(utilisateur);

        demande.setStatutDemandeAcces(StatutDemandeAcces.REJETEE);
        demande.setMotifRejet(motif);
        demande.setDateValidation(LocalDateTime.now());
        demandeAccesRepository.save(demande);

        return new MessageResponse("Demande de " + utilisateur.getEmail() + " rejetée.");
    }

    /**
     * Sauvegarde via le repository du sous-type réel.
     * Évite que utilisateurRepository.save() écrase/ignore la table enfant (dpo, cil, etc.)
     */
    private void sauvegarderUtilisateur(Utilisateur utilisateur) {
        if (utilisateur instanceof DPO d)                { dpoRepository.save(d); return; }
        if (utilisateur instanceof CIL c)                { cilRepository.save(c); return; }
        if (utilisateur instanceof DG dg)                { dgRepository.save(dg); return; }
        if (utilisateur instanceof UtilisateurMetier um) { utilisateurMetierRepository.save(um); return; }
        if (utilisateur instanceof Usager u)             { usagerRepository.save(u); return; }
        utilisateurRepository.save(utilisateur);
    }

    private DemandeAccesResponse toResponse(DemandeAcces d) {
        DemandeAccesResponse r = new DemandeAccesResponse();

        r.setIdDemande(d.getIdDemande());
        r.setStatutDemandeAcces(d.getStatutDemandeAcces());
        r.setDateDemande(d.getDateDemande());
        r.setDateValidation(d.getDateValidation());
        r.setMotif(d.getMotifDemande());

        Utilisateur u = d.getUtilisateur();
        if (u != null) {
            r.setUtilisateurId(u.getId());
            r.setNom(u.getNom());
            r.setPrenom(u.getPrenom());
            r.setEmail(u.getEmail());
            r.setStatutUtilisateur(u.getStatutUtilisateur());
            r.setDateCreationCompte(u.getDateCreation());
            r.setTypeUtilisateur(u.getClass().getSimpleName());

            if (u instanceof Usager usager) {
                r.setTelephone(usager.getTelephone());
                r.setVille(extraireVille(usager.getAdresse()));
            } else if (u instanceof DPO dpo) {
                r.setOrganisme(dpo.getOrganisme());
                r.setVille(extraireVille(dpo.getAdresseProfessionnelle()));
            } else if (u instanceof UtilisateurMetier metier) {
                r.setTelephone(metier.getTelephone());
                r.setFonction(metier.getFonction());
            }
        }

        if (d.getAdministrateur() != null) {
            Administrateur admin = d.getAdministrateur();
            r.setAdminTraitantNom(admin.getPrenom() + " " + admin.getNom());
        }

        return r;
    }

    private String extraireVille(String adresse) {
        if (adresse == null || adresse.isBlank()) return null;
        String[] parties = adresse.split(",");
        return parties[parties.length - 1].trim();
    }
}