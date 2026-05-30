package com.collecte.projetCIL.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.Administrateur;
import com.collecte.projetCIL.models.CIL;
import com.collecte.projetCIL.models.DG;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.AdministrateurRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final AdministrateurRepository administrateurRepository;
    private final UtilisateurRepository utilisateurRepository;

    public Map<String, String> getFonctionByEmail(String email) {

        // ── 1. Administrateur — pas de vérification de statut ──────────
        var adminOpt = administrateurRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrateur a = adminOpt.get();
            return Map.of(
                "type",     "ADMINISTRATEUR",
                "email",    a.getEmail(),
                "nom",      a.getNom() + " " + a.getPrenom(),
                "fonction", a.getFonction() != null ? a.getFonction() : "Non renseignée",
                "actif",    "true"
            );
        }

        // ── 2. Toutes les sous-classes de Utilisateur ──────────────────
        var userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Aucun utilisateur trouvé avec l'email : " + email);
        }

        Utilisateur u = userOpt.get();

        // "actif" = true uniquement si ACTIF, false pour tout autre statut
        boolean estActif = StatutUtilisateur.ACTIF.equals(u.getStatutUtilisateur());

        Map<String, String> result = new HashMap<>();
        result.put("email", u.getEmail());
        result.put("nom",   u.getNom() + " " + u.getPrenom());
        result.put("actif", String.valueOf(estActif));   // ← "true" ou "false"

        // Sous-classe UtilisateurMetier
        if (u instanceof UtilisateurMetier metier) {
            result.put("type",      "UTILISATEUR_METIER");
            result.put("fonction",  metier.getFonction() != null ? metier.getFonction() : "Non renseignée");
            result.put("telephone", metier.getTelephone() != null ? metier.getTelephone() : "");
            return result;
        }

        // Sous-classe CIL
        if (u instanceof CIL cil) {
            result.put("type",                "CIL");
            result.put("fonction",             "Correspondant Informatique & Libertés");
            result.put("service",              cil.getService() != null ? cil.getService() : "");
            result.put("niveauResponsabilite", cil.getNiveauResponsabilite() != null ? cil.getNiveauResponsabilite() : "");
            return result;
        }

        // Sous-classe DPO
        if (u instanceof DPO dpo) {
            result.put("type",                  "DPO");
            result.put("fonction",               "Délégué à la Protection des Données");
            result.put("organisme",              dpo.getOrganisme() != null ? dpo.getOrganisme() : "");
            result.put("adresseProfessionnelle", dpo.getAdresseProfessionnelle() != null ? dpo.getAdresseProfessionnelle() : "");
            result.put("dateNomination",         dpo.getDateNomination() != null ? dpo.getDateNomination().toString() : "");
            return result;
        }

        // Sous-classe DG
        if (u instanceof DG dg) {
            result.put("type",     "DG");
            result.put("fonction", "Direction Générale");
            result.put("idDg",     dg.getIdDg() != null ? dg.getIdDg() : "");
            return result;
        }

        // Utilisateur de base
        result.put("type",     "UTILISATEUR");
        result.put("fonction", "Non définie");
        return result;
    }
}