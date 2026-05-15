package com.collecte.projetCIL.models;

import com.collecte.projetCIL.enums.ModuleConserne;
import com.collecte.projetCIL.enums.ResultatAction;
import com.collecte.projetCIL.enums.TypeAction;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "journal_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_journal")
    private Long idJournal;

    @Column(name = "date_action")
    private LocalDate dateAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_action")
    private TypeAction typeAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "module_conserne")
    private ModuleConserne moduleConserne;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultat_action")
    private ResultatAction resultatAction;

    // ManyToOne Utilisateur (a)
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;
}
