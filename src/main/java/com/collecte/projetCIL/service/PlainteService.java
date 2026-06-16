package com.collecte.projetCIL.service;

import com.collecte.projetCIL.dto.request.PlainteRequest;
import com.collecte.projetCIL.dto.response.PlainteResponse;
import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.StatutPlainte;
import com.collecte.projetCIL.enums.TypeAction;
import com.collecte.projetCIL.enums.TypeNotification;
import com.collecte.projetCIL.models.CIL;
import com.collecte.projetCIL.models.DPO;
import com.collecte.projetCIL.models.Plainte;
import com.collecte.projetCIL.repository.CILRepository;
import com.collecte.projetCIL.repository.DPORepository;
import com.collecte.projetCIL.repository.PlainteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlainteService {

    private final PlainteRepository plainteRepository;
    private final CILRepository cilRepository;
    private final DPORepository dpoRepository;
    private final NotificationService notificationService;
    private final JournalAuditService journalAuditService;

    // ------------------------------------------------------------------ //
    //  CIL envoie une plainte au DPO                                      //
    // ------------------------------------------------------------------ //
    @Transactional
    public PlainteResponse envoyerPlainteCilVersDpo(PlainteRequest req, String emailCil) {

        CIL cil = cilRepository.findByEmail(emailCil)
                .orElseThrow(() -> new RuntimeException("CIL introuvable : " + emailCil));

        // On prend le premier DPO disponible (un seul DPO par hypothèse)
        DPO dpo = dpoRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Aucun DPO trouvé dans le système."));

        Plainte plainte = new Plainte();
        plainte.setDatePlainte(LocalDate.now());
        plainte.setLieu(req.getLieu());
        plainte.setObjetPlainte(req.getObjetPlainte());
        plainte.setDescriptionPlainte(req.getDescriptionPlainte());
        plainte.setStatutPlainte(StatutPlainte.RECUE);
        plainte.setCil(cil);
        plainte.setDpo(dpo);

        Plainte saved = plainteRepository.save(plainte);

        // Notifier le DPO
        String msg = "La CIL " + cil.getPrenom() + " " + cil.getNom()
                + " vous a transmis une plainte : « " + req.getObjetPlainte()
                + " » (Plainte #" + saved.getIdPlainte() + ")";
        notificationService.envoyer(dpo, TypeNotification.PLAINTE, msg);

        // Audit CIL
        journalAuditService.enregistrer(cil, TypeAction.CREATION, ModuleConserne.PLAINTE, ResultatAction.SUCCES);

        return toResponse(saved);
    }

    // ------------------------------------------------------------------ //
    //  Lister les plaintes reçues par le DPO                              //
    // ------------------------------------------------------------------ //
    public List<PlainteResponse> listerPlaintegRecuesDpo(Long dpoId) {
        return plainteRepository.findAll().stream()
                .filter(p -> p.getDpo() != null && p.getDpo().getId().equals(dpoId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Plaintes émises par une CIL. */
    public List<PlainteResponse> listerParCil(Long cilId) {
        return plainteRepository.findByCilId(cilId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Toutes les plaintes non clôturées (admin). */
    public List<PlainteResponse> listerNonCloturees() {
        return plainteRepository.findNonCloturees()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ //
    //  Mapper                                                             //
    // ------------------------------------------------------------------ //
    private PlainteResponse toResponse(Plainte p) {
        Long cilId = p.getCil() != null ? p.getCil().getId() : null;
        String cilNom = p.getCil() != null ? p.getCil().getPrenom() + " " + p.getCil().getNom() : null;
        Long dpoId = p.getDpo() != null ? p.getDpo().getId() : null;
        String dpoNom = p.getDpo() != null ? p.getDpo().getPrenom() + " " + p.getDpo().getNom() : null;

        return new PlainteResponse(
                p.getIdPlainte(),
                p.getDatePlainte(),
                p.getLieu(),
                p.getObjetPlainte(),
                p.getDescriptionPlainte(),
                p.getStatutPlainte(),
                p.getDecisionCil(),
                p.getDateDecision(),
                cilId,
                cilNom,
                dpoId,
                dpoNom
        );
    }
}
