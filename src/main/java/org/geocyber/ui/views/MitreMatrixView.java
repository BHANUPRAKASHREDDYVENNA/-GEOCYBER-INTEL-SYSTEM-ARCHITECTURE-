package org.geocyber.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.domain.MitreAttackTechnique;
import org.geocyber.repository.MitreTechniqueRepository;
import org.geocyber.ui.MainLayout;

@Route(value = "mitre", layout = MainLayout.class)
@PageTitle("MITRE ATT&CK Matrix | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class MitreMatrixView extends VerticalLayout {

    private final MitreTechniqueRepository techniqueRepository;
    private final Grid<MitreAttackTechnique> grid = new Grid<>(MitreAttackTechnique.class, false);
    private final Div detailCard = new Div();

    public MitreMatrixView(MitreTechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("MITRE ATT&CK® ENTERPRISE MATRIX STIX 2.1 INTEGRATION");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Tactics, techniques, and procedures (TTPs) mapped to nation-state threat group playbooks.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        TextField filter = new TextField();
        filter.setPlaceholder("Filter techniques by name or ID (e.g. T1566, PowerShell)...");
        filter.setWidth("450px");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> {
            if (e.getValue() == null || e.getValue().isBlank()) {
                grid.setItems(techniqueRepository.findAll());
            } else {
                grid.setItems(techniqueRepository.findByNameContainingIgnoreCase(e.getValue().trim()));
            }
        });

        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();

        configureGrid();
        configureDetailCard();

        grid.setWidth("50%");
        detailCard.setWidth("50%");

        content.add(grid, detailCard);
        add(title, sub, filter, content);

        if (!techniqueRepository.findAll().isEmpty()) {
            showDetail(techniqueRepository.findAll().getFirst());
        }
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setItems(techniqueRepository.findAll());

        grid.addColumn(MitreAttackTechnique::getTechniqueId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(MitreAttackTechnique::getName).setHeader("Technique Name").setAutoWidth(true);
        grid.addColumn(MitreAttackTechnique::getTactic).setHeader("Tactic").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) showDetail(e.getValue());
        });
    }

    private void configureDetailCard() {
        detailCard.setSizeFull();
        detailCard.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "20px")
                .set("overflow-y", "auto");
    }

    private void showDetail(MitreAttackTechnique t) {
        detailCard.removeAll();

        H3 name = new H3(t.getTechniqueId() + ": " + t.getName());
        name.getStyle().set("color", "#00ffcc").set("margin", "0 0 8px 0");

        Span tactic = new Span("Tactical Objective: " + t.getTactic());
        tactic.getStyle().set("color", "#0099ff").set("font-weight", "bold").set("font-size", "0.85rem").set("display", "block").set("margin-bottom", "12px");

        Paragraph desc = new Paragraph(t.getDescription());
        desc.getStyle().set("color", "#cbd5e0").set("font-size", "0.9rem");

        H4 platHeader = new H4("AFFECTED PLATFORMS");
        platHeader.getStyle().set("color", "#718096").set("font-size", "0.75rem").set("margin", "15px 0 4px 0");
        Span plat = new Span(t.getPlatforms() != null ? t.getPlatforms() : "All");
        plat.getStyle().set("color", "#fff").set("font-size", "0.85rem");

        H4 detHeader = new H4("DETECTION & MITIGATION RULES");
        detHeader.getStyle().set("color", "#00ffcc").set("font-size", "0.8rem").set("margin", "15px 0 6px 0");
        Paragraph det = new Paragraph(t.getDetection() != null ? t.getDetection() : "Monitor telemetry.");
        det.getStyle().set("color", "#a0aec0").set("font-size", "0.85rem").set("background", "#0d1117").set("padding", "10px").set("border-radius", "4px");

        detailCard.add(name, tactic, desc, platHeader, plat, detHeader, det);
    }
}
