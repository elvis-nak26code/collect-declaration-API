package com.collecte.projetCIL.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.collecte.projetCIL.dto.request.InscriptionRequest;
import com.collecte.projetCIL.dto.response.MessageResponse;
import com.collecte.projetCIL.enums.StatutDemandeAcces;
import com.collecte.projetCIL.enums.StatutUtilisateur;
import com.collecte.projetCIL.models.CIL;
import com.collecte.projetCIL.models.DG;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.DemandeAcces;
import com.collecte.projetCIL.models.Personne;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.models.Utilisateur;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.CILRepository;
import com.collecte.projetCIL.repository.DGRepository;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.DemandeAccesRepository;
import com.collecte.projetCIL.repository.UsagerRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;
import com.collecte.projetCIL.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InscriptionService {

    private final UtilisateurRepository utilisateurRepository;
    private final UsagerRepository usagerRepository;
    private final DemandeAccesRepository demandeAccesRepository;
    private final PasswordEncoder passwordEncoder;
    private final PersonneService personneService;

    private final DPORepository dpoRepository;
    private final CILRepository cilRepository;
    private final DGRepository dgRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;

    @Transactional
    public MessageResponse inscrire(InscriptionRequest request) {

        // Vérification email — message explicite
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                "Cette adresse e-mail est déjà associée à un compte SOFITEX. " +
                "Veuillez vous connecter ou utiliser une autre adresse."
            );
        }

        // Vérification téléphone pour les usagers — message explicite
        boolean isUsager = "USAGER".equalsIgnoreCase(request.getTypeUtilisateur());
        if (isUsager && request.getTelephone() != null && !request.getTelephone().isBlank()) {
            if (usagerRepository.existsByTelephone(request.getTelephone())) {
                throw new RuntimeException(
                    "Ce numéro de téléphone est déjà enregistré sur un compte SOFITEX. " +
                    "Veuillez utiliser un autre numéro ou contacter l'administrateur."
                );
            }
        }

        Utilisateur utilisateur = creerUtilisateur(request);
        utilisateur.setStatutUtilisateur(StatutUtilisateur.INACTIF);
        utilisateur.setDateCreation(LocalDateTime.now());

        Utilisateur saved = sauvegarderUtilisateur(utilisateur);

        Personne personne = personneService.creerOuRecuperer(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                request.getTelephone()
        );
        saved.setPersonne(personne);
        sauvegarderUtilisateur(saved);

        DemandeAcces demande = new DemandeAcces();
        demande.setUtilisateur(saved);
        demande.setDateDemande(LocalDateTime.now());
        demande.setStatutDemandeAcces(StatutDemandeAcces.EN_ATTENTE);
        demande.setMotifDemande(request.getMotifDemande());
        demandeAccesRepository.save(demande);

        return new MessageResponse(
            "Inscription envoyée. Votre compte sera activé après validation de l'administrateur."
        );
    }

    private Utilisateur sauvegarderUtilisateur(Utilisateur utilisateur) {
        if (utilisateur instanceof DPO d)                return dpoRepository.save(d);
        if (utilisateur instanceof CIL c)                return cilRepository.save(c);
        if (utilisateur instanceof DG dg)                return dgRepository.save(dg);
        if (utilisateur instanceof UtilisateurMetier um) return utilisateurMetierRepository.save(um);
        if (utilisateur instanceof Usager u)             return usagerRepository.save(u);
        return utilisateurRepository.save(utilisateur);
    }

    private Utilisateur creerUtilisateur(InscriptionRequest req) {
        String mdpHache = passwordEncoder.encode(req.getMotDePasse());

        switch (req.getTypeUtilisateur().toUpperCase()) {

            case "UTILISATEUR_METIER" -> {
                UtilisateurMetier um = new UtilisateurMetier();
                um.setNom(req.getNom()); um.setPrenom(req.getPrenom());
                um.setEmail(req.getEmail()); um.setMotDePasse(mdpHache);
                um.setFonction(req.getFonction()); um.setDepartement(req.getDepartement());
                um.setTelephone(req.getTelephone());
                return um;
            }
            case "CIL" -> {
                CIL c = new CIL();
                c.setNom(req.getNom()); c.setPrenom(req.getPrenom());
                c.setEmail(req.getEmail()); c.setMotDePasse(mdpHache);
                c.setService(req.getService()); c.setNiveauResponsabilite(req.getNiveauResponsabilite());
                c.setTelephone(req.getTelephone());
                return c;
            }
            case "DPO" -> {
                DPO d = new DPO();
                d.setNom(req.getNom()); d.setPrenom(req.getPrenom());
                d.setEmail(req.getEmail()); d.setMotDePasse(mdpHache);
                d.setOrganisme(req.getOrganisme()); d.setAdresseProfessionnelle(req.getAdresseProfessionnelle());
                return d;
            }
            case "DG" -> {
                DG dg = new DG();
                dg.setNom(req.getNom()); dg.setPrenom(req.getPrenom());
                dg.setEmail(req.getEmail()); dg.setMotDePasse(mdpHache);
                dg.setTelephone(req.getTelephone());
                return dg;
            }
            case "USAGER" -> {
                Usager u = new Usager();
                u.setNom(req.getNom()); u.setPrenom(req.getPrenom());
                u.setEmail(req.getEmail()); u.setMotDePasse(mdpHache);
                u.setTelephone(req.getTelephone()); u.setAdresse(req.getAdresse());
                u.setMatricule(req.getMatricule());
                return u;
            }
            default -> throw new RuntimeException("Type utilisateur invalide : " + req.getTypeUtilisateur());
        }
    }
}