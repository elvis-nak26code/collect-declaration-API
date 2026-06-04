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

    // Repositories spécifiques pour garantir l'insert dans les tables enfants
    private final DPORepository dpoRepository;
    private final CILRepository cilRepository;
    private final DGRepository dgRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;
    private final UsagerRepository usagerRepository;

    @Transactional
    public MessageResponse inscrire(InscriptionRequest request) {

        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé : " + request.getEmail());
        }

        Utilisateur utilisateur = creerUtilisateur(request);
        utilisateur.setStatutUtilisateur(StatutUtilisateur.INACTIF);
        utilisateur.setDateCreation(LocalDateTime.now());

        // Sauvegarder via le bon repository selon le type réel
        Utilisateur saved = sauvegarderUtilisateur(utilisateur);

        DemandeAcces demande = new DemandeAcces();
        demande.setUtilisateur(saved);
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatutDemandeAcces(StatutDemandeAcces.EN_ATTENTE);
        demande.setMotifDemande(request.getMotifDemande());
        demandeAccesRepository.save(demande);

        return new MessageResponse("Inscription envoyée. Votre compte sera activé après validation de l'administrateur.");
    }

    // Sauvegarde via le repository spécifique → insert dans la bonne table enfant
    private Utilisateur sauvegarderUtilisateur(Utilisateur utilisateur) {
        if (utilisateur instanceof DPO d)                   return dpoRepository.save(d);
        if (utilisateur instanceof CIL c)                   return cilRepository.save(c);
        if (utilisateur instanceof DG dg)                   return dgRepository.save(dg);
        if (utilisateur instanceof UtilisateurMetier um)    return utilisateurMetierRepository.save(um);
        if (utilisateur instanceof Usager u)                return usagerRepository.save(u);
        return utilisateurRepository.save(utilisateur);
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
                um.setTelephone(req.getTelephone());
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
                c.setTelephone(req.getTelephone());
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
                return d;
            }

            case "DG" -> {
                DG dg = new DG();
                dg.setNom(req.getNom());
                dg.setPrenom(req.getPrenom());
                dg.setEmail(req.getEmail());
                dg.setMotDePasse(mdpHache);
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