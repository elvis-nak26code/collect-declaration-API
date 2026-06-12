package com.collecte.projetCIL.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.CIL;
import com.collecte.projetCIL.models.DG;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.DemandeAcces;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final AdministrateurRepository administrateurRepository;
    private final UtilisateurRepository    utilisateurRepository;

    public Map<String, String> getFonctionByEmail(String email) {

        // ── 1. Administrateur ──────────────────────────────────────────
        var adminOpt = administrateurRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrateur a = adminOpt.get();
            Map<String, String> result = new HashMap<>();
            result.put("type",     "ADMINISTRATEUR");
            result.put("email",    a.getEmail());
            result.put("nom",      a.getNom() + " " + a.getPrenom());
            result.put("fonction", a.getFonction() != null ? a.getFonction() : "Non renseignée");
            result.put("actif",    "true");
            result.put("statutUtilisateur",   StatutUtilisateur.ACTIF.name());
            result.put("statutDemandeAcces",  "");
            result.put("compteDesactive",     "false");
            result.put("demandeEnAttente",    "false");
            result.put("demandeRejetee",      "false");
            result.put("motifRejet",          "");
            result.put("adminTraitantNom",    "");
            return result;
        }

        // ── 2. Toutes les sous-classes de Utilisateur ──────────────────
        var userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé avec l'email : " + email);
        }

        Utilisateur u = userOpt.get();
        StatutUtilisateur statutUtilisateur = u.getStatutUtilisateur();
        boolean estActif = StatutUtilisateur.ACTIF.equals(statutUtilisateur);

        // Compte explicitement désactivé par l'admin (INACTIF, SUSPENDU ou SUPPRIME)
        boolean compteDesactive = statutUtilisateur != null && !estActif;

        Map<String, String> result = new HashMap<>();
        result.put("email", u.getEmail());
        result.put("nom",   u.getNom() + " " + u.getPrenom());
        result.put("actif", String.valueOf(estActif));
        result.put("statutUtilisateur",
                statutUtilisateur != null ? statutUtilisateur.name() : "");
        result.put("compteDesactive", String.valueOf(compteDesactive));

        // ── Ajout statutDemandeAcces, motifRejet, adminTraitantNom ─────
        DemandeAcces demande = u.getDemandeAcces();
        boolean demandeEnAttente = false;
        boolean demandeRejetee   = false;

        if (demande != null) {
            StatutDemandeAcces statutDemande = demande.getStatutDemandeAcces();
            demandeEnAttente = StatutDemandeAcces.EN_ATTENTE.equals(statutDemande);
            demandeRejetee   = StatutDemandeAcces.REJETEE.equals(statutDemande);

            result.put("statutDemandeAcces",
                statutDemande != null ? statutDemande.name() : "");
            result.put("motifRejet",
                demande.getMotifRejet() != null ? demande.getMotifRejet() : "");
            result.put("adminTraitantNom",
                demande.getAdministrateur() != null
                    ? demande.getAdministrateur().getNom() + " " + demande.getAdministrateur().getPrenom()
                    : "");
        } else {
            result.put("statutDemandeAcces", "");
            result.put("motifRejet",         "");
            result.put("adminTraitantNom",   "");
        }

        result.put("demandeEnAttente", String.valueOf(demandeEnAttente));
        result.put("demandeRejetee",   String.valueOf(demandeRejetee));

        // ── Sous-classes ───────────────────────────────────────────────
        if (u instanceof UtilisateurMetier metier) {
            result.put("type",                "UTILISATEUR_METIER");
            result.put("fonction",             metier.getFonction() != null ? metier.getFonction() : "Non renseignée");
            result.put("telephone",            metier.getTelephone() != null ? metier.getTelephone() : "");
            result.put("utilisateurMetierId",  String.valueOf(metier.getId()));
            return result;
        }

        if (u instanceof CIL cil) {
            result.put("type",                "CIL");
            result.put("fonction",             "Correspondant Informatique & Libertés");
            result.put("service",              cil.getService() != null ? cil.getService() : "");
            result.put("niveauResponsabilite", cil.getNiveauResponsabilite() != null ? cil.getNiveauResponsabilite() : "");
            return result;
        }

        if (u instanceof DPO dpo) {
            result.put("type",                  "DPO");
            result.put("fonction",               "Délégué à la Protection des Données");
            result.put("organisme",              dpo.getOrganisme() != null ? dpo.getOrganisme() : "");
            result.put("adresseProfessionnelle", dpo.getAdresseProfessionnelle() != null ? dpo.getAdresseProfessionnelle() : "");
            result.put("dateNomination",         dpo.getDateNomination() != null ? dpo.getDateNomination().toString() : "");
            return result;
        }

        if (u instanceof DG dg) {
            result.put("type",     "DG");
            result.put("fonction", "Direction Générale");
            result.put("idDg",     dg.getIdDg() != null ? dg.getIdDg() : "");
            return result;
        }

        result.put("type",     "UTILISATEUR");
        result.put("fonction", "Non définie");
        return result;
    }
}