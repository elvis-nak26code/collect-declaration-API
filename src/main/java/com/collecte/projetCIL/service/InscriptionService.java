package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.InscriptionRequest;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.*;
import com.collecte.projetCIL.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final UtilisateurRepository utilisateurRepository;
    private final DemandeAccesRepository demandeAccesRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MessageResponse inscrire(InscriptionRequest request) {

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé : " + request.getEmail());
        }

        Utilisateur utilisateur = creerUtilisateur(request);
        utilisateur.setStatutUtilisateur(StatutUtilisateur.INACTIF);
        utilisateur.setDateCreation(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);

        DemandeAcces demande = new DemandeAcces();
        demande.setUtilisateur(utilisateur);
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatutDemandeAcces(StatutDemandeAcces.EN_ATTENTE);
        demandeAccesRepository.save(demande);

        return new MessageResponse("Inscription envoyée. Votre compte sera activé après validation de l'administrateur.");
    }

    private Utilisateur creerUtilisateur(InscriptionRequest req) {
        String mdpHache = passwordEncoder.encode(req.getMotDePasse());

        switch (req.getTypeUtilisateur().toUpperCase()) {

            case "UTILISATEUR_METIER" -> {
                UtilisateurMetier um = new UtilisateurMetier();
                um.setNom(req.getNom());
                um.setPrenom(req.getPrenom());
                um.setEmail(req.getEmail());
                um.setMotDePasse(mdpHache);
                um.setFonction(req.getFonction());
                um.setDepartement(req.getDepartement());
                // ✅ téléphone professionnel (telMetier ou telephone en fallback)
                um.setTelephone(req.getTelephone());
                // ✅ dateNomination supprimée du form — définie à l'activation
                // um.setDateNomination(...)  → sera définie par l'admin
                return um;
            }

            case "CIL" -> {
                CIL c = new CIL();
                c.setNom(req.getNom());
                c.setPrenom(req.getPrenom());
                c.setEmail(req.getEmail());
                c.setMotDePasse(mdpHache);
                c.setService(req.getService());
                c.setNiveauResponsabilite(req.getNiveauResponsabilite());
                // ✅ téléphone professionnel ajouté
                c.setTelephone(req.getTelephone());
                // ✅ fonction supprimée du form CIL
                return c;
            }

            case "DPO" -> {
                DPO d = new DPO();
                d.setNom(req.getNom());
                d.setPrenom(req.getPrenom());
                d.setEmail(req.getEmail());
                d.setMotDePasse(mdpHache);
                d.setOrganisme(req.getOrganisme());
                d.setAdresseProfessionnelle(req.getAdresseProfessionnelle());
                // ✅ dateNomination supprimée du form — définie à l'activation
                // d.setDateNomination(...)  → sera définie par l'admin
                return d;
            }

            case "DG" -> {
                DG dg = new DG();
                dg.setNom(req.getNom());
                dg.setPrenom(req.getPrenom());
                dg.setEmail(req.getEmail());
                dg.setMotDePasse(mdpHache);
                // ✅ idDg supprimé — remplacé par téléphone professionnel
                dg.setTelephone(req.getTelephone());
                return dg;
            }

            case "USAGER" -> {
                Usager u = new Usager();
                u.setNom(req.getNom());
                u.setPrenom(req.getPrenom());
                u.setEmail(req.getEmail());
                u.setMotDePasse(mdpHache);
                u.setTelephone(req.getTelephone());
                u.setAdresse(req.getAdresse());
                u.setMatricule(req.getMatricule());
                return u;
            }

            default -> throw new RuntimeException("Type utilisateur invalide : " + req.getTypeUtilisateur());
        }
    }
}