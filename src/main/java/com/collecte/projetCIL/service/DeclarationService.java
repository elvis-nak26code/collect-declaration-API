package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.*;
import com.collecte.projetCIL.dto.response.DeclarationResponse;
import com.collecte.projetCIL.enums.*;
import com.collecte.projetCIL.models.*;
import com.collecte.projetCIL.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeclarationService {

    private final DeclarationNormaleRepository               normaleRepo;
    private final DeclarationCollecteSiteInternetRepository  collecteSiteRepo;
    private final DeclarationSystemeVideoSurveillanceRepository videoRepo;
    private final DeclarationAutorisationRepository          autorisationRepo;
    private final DeclarationRepository                      declarationRepo;
    private final TraitementRepository                       traitementRepo;
    private final DPORepository                              dpoRepo;
    private final DGRepository                               dgRepo;
    private final UtilisateurMetierRepository                utilisateurMetierRepo;
    private final JournalAuditService                        journalAuditService;
    private final NotificationService                        notificationService;

    // ================================================================== //
    //  DÉCLARATION NORMALE
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationNormale(DeclarationNormaleRequest req, String emailDpo) {

        DPO dpo         = getDpo(emailDpo);
        Traitement trt  = getTraitement(req.getTraitementId());

        DeclarationNormale d = new DeclarationNormale();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDescriptionProcedureManuelle(req.getDescriptionProcedureManuelle());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setCaracteristiquesSysteme(req.getCaracteristiquesSysteme());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setProtocoleRecherche(req.getProtocoleRecherche());
        d.setDescriptionConnexionFichiers(req.getDescriptionConnexionFichiers());
        d.setMotifsInterconnexion(req.getMotifsInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());

        DeclarationNormale saved = normaleRepo.save(d);

        postCreation(dpo, trt, saved, "NORMALE");
        return toResponse(saved, "NORMALE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION COLLECTE SITE INTERNET
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationCollecteSite(DeclarationCollecteSiteInternetRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        DeclarationCollecteSiteInternet d = new DeclarationCollecteSiteInternet();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setCaracteristiquesMainStructure(req.getCaracteristiquesMainStructure());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setDonneesConnexion(req.getDonneesConnexion());
        d.setDescriptionDonneesConnexion(req.getDescriptionDonneesConnexion());
        d.setCookies(req.getCookies());
        d.setDescriptionCookies(req.getDescriptionCookies());
        d.setDureeConservationCookies(req.getDureeConservationCookies());
        d.setTelechargementTraitement(req.getTelechargementTraitement());

        DeclarationCollecteSiteInternet saved = collecteSiteRepo.save(d);

        postCreation(dpo, trt, saved, "COLLECTE_SITE");
        return toResponse(saved, "COLLECTE_SITE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION VIDÉO SURVEILLANCE
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationVideoSurveillance(DeclarationVideoSurveillanceRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        DeclarationSystemeVideoSurveillance d = new DeclarationSystemeVideoSurveillance();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setFinalites(req.getFinalites());
        d.setAdresseInstallation(req.getAdresseInstallation());
        d.setNatureEnvironnement(req.getNatureEnvironnement());
        d.setEmplacementCameras(req.getEmplacementCameras());
        d.setNombreTotalCameras(req.getNombreTotalCameras());
        d.setModeleDispositif(req.getModeleDispositif());
        d.setVisualisationTempsReel(req.getVisualisationTempsReel());
        d.setModeTransfert(req.getModeTransfert());
        d.setSonDeSon(req.getSonDeSon());
        d.setTypeEnregistrement(req.getTypeEnregistrement());
        d.setNatureEnregistrement(req.getNatureEnregistrement());
        d.setLiaisonReseau(req.getLiaisonReseau());
        d.setUtilisationSystemesExperts(req.getUtilisationSystemesExperts());
        d.setDescriptionSystemesExperts(req.getDescriptionSystemesExperts());
        d.setFonctionnalitesTraitement(req.getFonctionnalitesTraitement());
        d.setAccesImagesDistance(req.getAccesImagesDistance());
        d.setAccesPhysique(req.getAccesPhysique());
        d.setAccesLogique(req.getAccesLogique());
        d.setMesuresSuppression(req.getMesuresSuppression());
        d.setAttribute(req.getAttribute());
        d.setLocalisationPictogrammes(req.getLocalisationPictogrammes());

        DeclarationSystemeVideoSurveillance saved = videoRepo.save(d);

        postCreation(dpo, trt, saved, "VIDEO_SURVEILLANCE");
        return toResponse(saved, "VIDEO_SURVEILLANCE", trt);
    }

    // ================================================================== //
    //  DÉCLARATION AUTORISATION
    // ================================================================== //
    @Transactional
    public DeclarationResponse creerDeclarationAutorisation(DeclarationAutorisationRequest req, String emailDpo) {

        DPO dpo        = getDpo(emailDpo);
        Traitement trt = getTraitement(req.getTraitementId());

        DeclarationAutorisation d = new DeclarationAutorisation();
        remplirChampBase(d, req.getDateSoumission(), req.getSecteur(), req.getNatureDemande(),
                req.getResponsableDeclaration(), req.getContactConfidentialite(),
                req.getDateMiseEnOeuvre(), req.getCategoriesDonnees(), req.getOrigineDonnees(),
                req.getDureeConservation(), req.getLieuStockage(), req.getCommunicationAutresOrganismes(),
                req.getDestinataireNom(), req.getDestinataireAdresse(), req.getTexteJuridiqueCommunication(),
                req.getFinaliteCommunication(), req.getDestinataireConformeCil(), req.getTransfertPaysEtranger(),
                req.getRecoursSousTraitant(), req.getContratConfidentialiteSousTraitant(),
                req.getRolesSousTraitants(), req.getCategoriesPersonnesAcces(), req.getPolitiqueAccesBatiments(),
                req.getMesuresSecurite(), req.getMesuresSensibilisation(), req.getMoyensInformationDroits(),
                req.getMoyensExerciceDroits(), req.getCoordonneesExerciceDroits(),
                req.getDelaiCommunicationDroits(), req.getNomPrenomResponsable(), req.getFonctionResponsable(),
                dpo);

        d.setDenominationTraitement(req.getDenominationTraitement());
        d.setFinaliteTraitement(req.getFinaliteTraitement());
        d.setTexteJuridique(req.getTexteJuridique());
        d.setCategoriesPersonnesConcernees(req.getCategoriesPersonnesConcernees());
        d.setNombrePersonnesConcernees(req.getNombrePersonnesConcernees());
        d.setTypeTraitement(req.getTypeTraitement());
        d.setCaracteristiquesTechniques(req.getCaracteristiquesTechniques());
        d.setFonctionnalitesSysteme(req.getFonctionnalitesSysteme());
        d.setCertificationSecurite(req.getCertificationSecurite());
        d.setPolitiqueAccesSystemes(req.getPolitiqueAccesSystemes());
        d.setDescriptionFichier(req.getDescriptionFichier());
        d.setModeTransfert(req.getModeTransfert());
        d.setTraitementDonneesSante(req.getTraitementDonneesSante());
        d.setProfessionalSante(req.getProfessionalSante());
        d.setModalitesDiffusionResultats(req.getModalitesDiffusionResultats());
        d.setDestinataireAdresse(req.getDestinataireAdresse());
        d.setTexteJuridiqueCommunication(req.getTexteJuridiqueCommunication());
        d.setDestinataireAdresse(req.getDestinataireAdresse());
        d.setDestinataireNomPrenom(req.getDestinataireNomPrenom());
        d.setDestinataireAdresse(req.getDestinataireAdresse());
        d.setConnexionFichiers(req.getConnexionFichiers());
        d.setCategoriesDonneesInterconnexion(req.getCategoriesDonneesInterconnexion());
        d.setDureeInterconnexion(req.getDureeInterconnexion());
        d.setIdentiteFichiersInterconnexion(req.getIdentiteFichiersInterconnexion());
        d.setTransfertPaysEtranger(req.getTransfertPaysEtranger());
        d.setRecoursSousTraitant(req.getRecoursSousTraitant());
        d.setRolesSousTraitants(req.getRolesSousTraitants());
        d.setCategoriesPersonnesAcces(req.getCategoriesPersonnesAcces());
        d.setPolitiqueAccesBatiments(req.getPolitiqueAccesBatiments());
        d.setMesuresSecurite(req.getMesuresSecurite());
        d.setDescriptionSensibilisation(req.getDescriptionSensibilisation() != null ? req.getDescriptionSensibilisation() : null);
        d.setPaysDestinationProtectionDonnees(req.getPaysDestinationProtectionDonnees());
        d.setDescriptionFichierTransfert(req.getDescriptionFichierTransfert());
        d.setNombrePersonnesTransfert(req.getNombrePersonnesTransfert());
        d.setCategoriesDonneesTransfert(req.getCategoriesDonneesTransfert());
        d.setFondementJuridique(req.getFondementJuridique());
        d.setConsentementPersonnesConcernees(req.getConsentementPersonnesConcernees());
        d.setMethodeRecueilConsentement(req.getMethodeRecueilConsentement());
        d.setMesuresSecuriteTransfert(req.getMesuresSecuriteTransfert());
        d.setLieuStockage(req.getLieuStockage());
        d.setCommunicationAutresOrganismes(req.getCommunicationAutresOrganismes());
        d.setDureeConservationSante(req.getDureeConservationSante());
        d.setOrigineDonnees(req.getOrigineDonnees());

        DeclarationAutorisation saved = autorisationRepo.save(d);

        postCreation(dpo, trt, saved, "AUTORISATION");
        return toResponse(saved, "AUTORISATION", trt);
    }

    // ================================================================== //
    //  CONSULTATION — Lister les déclarations
    // ================================================================== //

    /** Toutes les déclarations d'un DPO */
    public List<DeclarationResponse> listerParDpo(Long dpoId) {
        return declarationRepo.findByDpoId(dpoId).stream()
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    /** Déclarations en attente de validation par la DG */
    public List<DeclarationResponse> listerEnAttente() {
        return declarationRepo.findEnAttenteOrderByDate().stream()
                .map(d -> toResponse(d, detecterType(d), null))
                .collect(Collectors.toList());
    }

    /** Obtenir une déclaration par ID */
    public DeclarationResponse getById(Long id) {
        Declaration d = declarationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + id));
        return toResponse(d, detecterType(d), null);
    }

    // ================================================================== //
    //  VALIDATION / REJET PAR LA DG
    // ================================================================== //
    @Transactional
    public DeclarationResponse validerDeclaration(Long declarationId, String emailDg) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        DG dg = dgRepo.findByEmail(emailDg)
                .orElseThrow(() -> new RuntimeException("DG introuvable : " + emailDg));

        d.setStatut(StatutDeclaration.APPROUVEE);
        Declaration saved = declarationRepo.save(d);

        // Audit DG
        journalAuditService.enregistrer(dg, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        // Notifier le DPO
        if (d.getDpo() != null) {
            notificationService.envoyer(d.getDpo(), TypeNotification.CONFIRMATION,
                    "Votre déclaration #" + declarationId + " a été approuvée par la DG.");
        }

        return toResponse(saved, detecterType(saved), null);
    }

    @Transactional
    public DeclarationResponse rejeterDeclaration(Long declarationId, String emailDg, String commentaire) {
        Declaration d = declarationRepo.findById(declarationId)
                .orElseThrow(() -> new RuntimeException("Déclaration introuvable : " + declarationId));

        DG dg = dgRepo.findByEmail(emailDg)
                .orElseThrow(() -> new RuntimeException("DG introuvable : " + emailDg));

        d.setStatut(StatutDeclaration.REJETEE);
        Declaration saved = declarationRepo.save(d);

        // Audit DG
        journalAuditService.enregistrer(dg, TypeAction.MODIFICATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        // Notifier le DPO avec le commentaire de rejet
        if (d.getDpo() != null) {
            String message = "Votre déclaration #" + declarationId + " a été rejetée. Motif : " + commentaire;
            notificationService.envoyer(d.getDpo(), TypeNotification.ALERTE, message);
        }

        return toResponse(saved, detecterType(saved), null);
    }

    // ================================================================== //
    //  HELPERS PRIVÉS
    // ================================================================== //

    /**
     * Actions post-création communes : audit DPO + notification DG.
     */
    private void postCreation(DPO dpo, Traitement trt, Declaration saved, String type) {
        // Journal d'audit pour le DPO
        journalAuditService.enregistrer(dpo, TypeAction.CREATION, ModuleConserne.DECLARATION, ResultatAction.SUCCES);

        // Notification au DG (on prend le premier DG — hypothèse : un seul compte DG)
        dgRepo.findAll().stream().findFirst().ifPresent(dg ->
                notificationService.envoyer(dg, TypeNotification.ALERTE,
                        "Nouvelle déclaration de type " + type + " soumise par " +
                        dpo.getPrenom() + " " + dpo.getNom() +
                        " — en attente de validation."));
    }

    private void remplirChampBase(Declaration d,
                                  LocalDate dateSoumission, String secteur, NatureDemande nature,
                                  String responsable, String contact, LocalDate dateMiseEnOeuvre,
                                  String categoriesDonnees, String origineDonnees, String dureeConservation,
                                  String lieuStockage, Boolean communicationAutresOrganismes,
                                  String destinataireNom, String destinataireAdresse,
                                  String texteJuridiqueCommunication, String finaliteCommunication,
                                  Boolean destinataireConformeCil, Boolean transfertPaysEtranger,
                                  Boolean recoursSousTraitant, Boolean contratConfidentialiteSousTraitant,
                                  String rolesSousTraitants, String categoriesPersonnesAcces,
                                  Boolean politiqueAccesBatiments, String mesuresSecurite,
                                  Boolean mesuresSensibilisation, String moyensInformationDroits,
                                  String moyensExerciceDroits, String coordonneesExerciceDroits,
                                  String delaiCommunicationDroits, String nomPrenomResponsable,
                                  String fonctionResponsable, DPO dpo) {

        d.setDateSoumission(dateSoumission != null ? dateSoumission : LocalDate.now());
        d.setSecteur(secteur);
        d.setNatureDemande(nature);
        d.setResponsableDeclaration(responsable);
        d.setContactConfidentialite(contact);
        d.setDateMiseEnOeuvre(dateMiseEnOeuvre);
        d.setCategoriesDonnees(categoriesDonnees);
        d.setOrigineDonnees(origineDonnees);
        d.setDureeConservation(dureeConservation);
        d.setLieuStockage(lieuStockage);
        d.setCommunicationAutresOrganismes(communicationAutresOrganismes);
        d.setDestinataireNom(destinataireNom);
        d.setDestinataireAdresse(destinataireAdresse);
        d.setTexteJuridiqueCommunication(texteJuridiqueCommunication);
        d.setFinaliteCommunication(finaliteCommunication);
        d.setDestinataireConformeCil(destinataireConformeCil);
        d.setTransfertPaysEtranger(transfertPaysEtranger);
        d.setRecoursSousTraitant(recoursSousTraitant);
        d.setContratConfidentialiteSousTraitant(contratConfidentialiteSousTraitant);
        d.setRolesSousTraitants(rolesSousTraitants);
        d.setCategoriesPersonnesAcces(categoriesPersonnesAcces);
        d.setPolitiqueAccesBatiments(politiqueAccesBatiments);
        d.setMesuresSecurite(mesuresSecurite);
        d.setMesuresSensibilisation(mesuresSensibilisation);
        d.setMoyensInformationDroits(moyensInformationDroits);
        d.setMoyensExerciceDroits(moyensExerciceDroits);
        d.setCoordonneesExerciceDroits(coordonneesExerciceDroits);
        d.setDelaiCommunicationDroits(delaiCommunicationDroits);
        d.setNomPrenomResponsable(nomPrenomResponsable);
        d.setFonctionResponsable(fonctionResponsable);
        d.setStatut(StatutDeclaration.EN_ATTENTE);
        d.setDpo(dpo);
    }

    private DPO getDpo(String email) {
        return dpoRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("DPO introuvable : " + email));
    }

    private Traitement getTraitement(Long id) {
        return traitementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Traitement introuvable : " + id));
    }

    private String detecterType(Declaration d) {
        if (d instanceof DeclarationNormale)               return "NORMALE";
        if (d instanceof DeclarationCollecteSiteInternet)  return "COLLECTE_SITE";
        if (d instanceof DeclarationSystemeVideoSurveillance) return "VIDEO_SURVEILLANCE";
        if (d instanceof DeclarationAutorisation)          return "AUTORISATION";
        return "INCONNUE";
    }

    private DeclarationResponse toResponse(Declaration d, String type, Traitement trt) {
        String dpoNom = null;
        Long   dpoId  = null;
        if (d.getDpo() != null) {
            dpoId  = d.getDpo().getId();
            dpoNom = d.getDpo().getPrenom() + " " + d.getDpo().getNom();
        }
        Long   trtId  = trt != null ? trt.getIdTraitement() : null;
        String trtDesc = trt != null ? trt.getDescription() : null;

        return new DeclarationResponse(
                d.getIdDeclaration(),
                type,
                d.getDateSoumission(),
                d.getSecteur(),
                d.getNatureDemande(),
                d.getStatut(),
                d.getResponsableDeclaration(),
                d.getContactConfidentialite(),
                d.getDateMiseEnOeuvre(),
                dpoId,
                dpoNom,
                trtId,
                trtDesc
        );
    }
}
