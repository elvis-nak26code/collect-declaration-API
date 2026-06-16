package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.DemandeRequest;
import com.collecte.projetCIL.dto.response.DemandeResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.StatutDemande;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.Demande;
import com.collecte.projetCIL.models.DonneePersonnelle;
import com.collecte.projetCIL.models.Usager;
import com.collecte.projetCIL.models.UtilisateurMetier;
import com.collecte.projetCIL.repository.DemandeRepository;
import com.collecte.projetCIL.repository.DonneePersonnelleRepository;
import com.collecte.projetCIL.repository.UsagerRepository;
import com.collecte.projetCIL.repository.UtilisateurMetierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final DonneePersonnelleRepository donneeRepository;
    private final UsagerRepository usagerRepository;
    private final UtilisateurMetierRepository utilisateurMetierRepository;
    private final NotificationService notificationService;
    private final JournalAuditService journalAuditService;

    // --------------------------------------------------------- //
    //  Usager soumet une demande de modification ou suppression  //
    // --------------------------------------------------------- //
    @Transactional
    public DemandeResponse soumettreDemandeUsager(DemandeRequest req) {

        Usager usager = usagerRepository.findById(req.getUsagerId())
                .orElseThrow(() -> new RuntimeException("Usager introuvable : " + req.getUsagerId()));

        DonneePersonnelle donnee = donneeRepository.findById(req.getDonneeId())
                .orElseThrow(() -> new RuntimeException("Donnée introuvable : " + req.getDonneeId()));

        // L'UtilisateurMetier responsable est celui lié au traitement de la donnée
        UtilisateurMetier um = (donnee.getTraitement() != null)
                ? donnee.getTraitement().getUtilisateurMetier()
                : null;

        Demande demande = new Demande();
        demande.setDateDemande(LocalDate.now());
        demande.setTypeDemande(req.getTypeDemande());
        demande.setDescriptionDemande(req.getDescriptionDemande());
        demande.setNouvelleValeur(req.getNouvelleValeur());
        demande.setStatutDemande(StatutDemande.EN_COURS);
        demande.setUsager(usager);
        demande.setUtilisateurMetier(um);
        demande.setDonneePersonnelle(donnee);

        Demande saved = demandeRepository.save(demande);

        // Notifier l'UtilisateurMetier
        if (um != null) {
            TypeNotification typeNotif = "SUPPRESSION".equalsIgnoreCase(req.getTypeDemande())
                    ? TypeNotification.DEMANDE_SUPPRESSION
                    : TypeNotification.DEMANDE_MODIFICATION;
            String msg = "L'usager " + usager.getPrenom() + " " + usager.getNom()
                    + " a soumis une demande de " + req.getTypeDemande().toLowerCase()
                    + " sur ses données personnelles. (Demande #" + saved.getIdDemande() + ")";
            notificationService.envoyer(um, typeNotif, msg);
        }

        // Audit usager
        journalAuditService.enregistrer(usager, TypeAction.CREATION, ModuleConserne.DEMANDE, ResultatAction.SUCCES);

        return toResponse(saved);
    }

    // --------------------------------------------------------- //
    //  UtilisateurMetier accepte la demande                      //
    // --------------------------------------------------------- //
    @Transactional
    public DemandeResponse accepterDemande(Long demandeId, String emailUm) {

        Demande demande = getDemandeOrThrow(demandeId);
        UtilisateurMetier um = utilisateurMetierRepository.findByEmail(emailUm)
                .orElseThrow(() -> new RuntimeException("UtilisateurMetier introuvable : " + emailUm));

        DonneePersonnelle donnee = demande.getDonneePersonnelle();

        if ("SUPPRESSION".equalsIgnoreCase(demande.getTypeDemande())) {
            // Supprimer la donnée
            if (donnee != null) donneeRepository.delete(donnee);
            demande.setReponse("Donnée supprimée conformément à votre demande.");
        } else {
            // Modifier la valeur
            if (donnee != null && demande.getNouvelleValeur() != null) {
                donnee.setValeur(demande.getNouvelleValeur());
                donneeRepository.save(donnee);
            }
            demande.setReponse("Valeur mise à jour conformément à votre demande.");
        }

        demande.setStatutDemande(StatutDemande.ACCEPTEE);
        demande.setDateTraitement(LocalDate.now());
        Demande saved = demandeRepository.save(demande);

        // Notifier l'usager
        if (demande.getUsager() != null) {
            String msg = "Votre demande de " + demande.getTypeDemande().toLowerCase()
                    + " (#" + demandeId + ") a été acceptée et traitée.";
            notificationService.envoyer(demande.getUsager(), TypeNotification.CONFIRMATION, msg);
        }

        journalAuditService.enregistrer(um, TypeAction.MODIFICATION, ModuleConserne.DEMANDE, ResultatAction.SUCCES);
        return toResponse(saved);
    }

    // --------------------------------------------------------- //
    //  UtilisateurMetier rejette la demande                      //
    // --------------------------------------------------------- //
    @Transactional
    public DemandeResponse rejeterDemande(Long demandeId, String emailUm, String motifRejet) {

        Demande demande = getDemandeOrThrow(demandeId);
        UtilisateurMetier um = utilisateurMetierRepository.findByEmail(emailUm)
                .orElseThrow(() -> new RuntimeException("UtilisateurMetier introuvable : " + emailUm));

        demande.setStatutDemande(StatutDemande.REJETEE);
        demande.setMotifRejet(motifRejet);
        demande.setDateTraitement(LocalDate.now());
        Demande saved = demandeRepository.save(demande);

        // Notifier l'usager
        if (demande.getUsager() != null) {
            String msg = "Votre demande de " + demande.getTypeDemande().toLowerCase()
                    + " (#" + demandeId + ") a été rejetée. Motif : " + motifRejet;
            notificationService.envoyer(demande.getUsager(), TypeNotification.ALERTE, msg);
        }

        journalAuditService.enregistrer(um, TypeAction.MODIFICATION, ModuleConserne.DEMANDE, ResultatAction.SUCCES);
        return toResponse(saved);
    }

    // --------------------------------------------------------- //
    //  Consultation                                              //
    // --------------------------------------------------------- //
    public List<DemandeResponse> listerParUsager(Long usagerId) {
        return demandeRepository.findByUsagerId(usagerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DemandeResponse> listerParUtilisateurMetier(Long umId) {
        return demandeRepository.findByUtilisateurMetierId(umId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<DemandeResponse> listerEnAttentePourUm(Long umId) {
        return demandeRepository.findByUtilisateurMetierIdAndStatut(umId, StatutDemande.EN_COURS)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // --------------------------------------------------------- //
    //  Helpers                                                   //
    // --------------------------------------------------------- //
    private Demande getDemandeOrThrow(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable : " + id));
    }

    private DemandeResponse toResponse(Demande d) {
        String usagerNom = d.getUsager() != null
                ? d.getUsager().getPrenom() + " " + d.getUsager().getNom() : null;
        String umNom = d.getUtilisateurMetier() != null
                ? d.getUtilisateurMetier().getPrenom() + " " + d.getUtilisateurMetier().getNom() : null;
        Long donneeId = d.getDonneePersonnelle() != null ? d.getDonneePersonnelle().getIdDonnee() : null;
        String donneeValeur = d.getDonneePersonnelle() != null ? d.getDonneePersonnelle().getValeur() : null;

        return new DemandeResponse(
                d.getIdDemande(),
                d.getDateDemande(),
                d.getTypeDemande(),
                d.getDescriptionDemande(),
                d.getNouvelleValeur(),
                d.getStatutDemande(),
                d.getReponse(),
                d.getMotifRejet(),
                d.getDateTraitement(),
                d.getUsager() != null ? d.getUsager().getId() : null,
                usagerNom,
                d.getUtilisateurMetier() != null ? d.getUtilisateurMetier().getId() : null,
                umNom,
                donneeId,
                donneeValeur
        );
    }
}
