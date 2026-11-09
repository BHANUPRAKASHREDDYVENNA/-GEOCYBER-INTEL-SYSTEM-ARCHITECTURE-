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
import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.geocyber.ui.MainLayout;

@Route(value = "actors", layout = MainLayout.class)
@PageTitle("APT Actor Profiles | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class AptProfilesView extends VerticalLayout {

    private final AptActorRepository actorRepository;
    private final Grid<AptActor> grid = new Grid<>(AptActor.class, false);
    private final Div detailPanel = new Div();

    public AptProfilesView(AptActorRepository actorRepository) {
        this.actorRepository = actorRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("NATION-STATE APT ACTOR PROFILES & ALIAS RESOLUTION");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Detailed threat actor dossiers with doctrine, malware tooling, targets, and vendor alias mappings.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search actor by name or alias (e.g. STRONTIUM, Midnight Blizzard, Barium)...");
        searchField.setWidth("500px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> updateList(e.getValue()));

        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();

        configureGrid();
        configureDetailPanel();

        grid.setWidth("45%");
        detailPanel.setWidth("55%");

        content.add(grid, detailPanel);
        add(title, sub, searchField, content);

        if (!actorRepository.findAll().isEmpty()) {
            showActorDetails(actorRepository.findAll().getFirst());
        }
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setItems(actorRepository.findAll());

        grid.addColumn(AptActor::getName).setHeader("Actor").setAutoWidth(true);
        grid.addColumn(AptActor::getCountry).setHeader("State Sponsor").setAutoWidth(true);
        grid.addColumn(AptActor::getMotivation).setHeader("Motivation").setAutoWidth(true);

        grid.asSingleSelect().addValueChangeListener(e -> {
            if (e.getValue() != null) {
                showActorDetails(e.getValue());
            }
        });
    }

    private void configureDetailPanel() {
        detailPanel.setSizeFull();
        detailPanel.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "20px")
                .set("overflow-y", "auto");
    }

    private void showActorDetails(AptActor actor) {
        detailPanel.removeAll();

        H3 name = new H3(actor.getName() + " (" + actor.getCountryName() + " / " + actor.getCountry() + ")");
        name.getStyle().set("color", "#00ffcc").set("margin", "0 0 10px 0");

        Span aliases = new Span("Known Vendor Aliases: " + actor.getAliases());
        aliases.getStyle().set("color", "#a0aec0").set("font-size", "0.85rem").set("display", "block").set("margin-bottom", "15px");

        Paragraph desc = new Paragraph(actor.getDescription());
        desc.getStyle().set("color", "#cbd5e0").set("font-size", "0.9rem");

        Div metaGrid = new Div();
        metaGrid.getStyle().set("display", "grid").set("grid-template-columns", "1fr 1fr").set("gap", "12px").set("margin-top", "15px");

        metaGrid.add(
                createMetaItem("PRIMARY MOTIVATION", actor.getMotivation()),
                createMetaItem("CONFIDENCE SCORE", String.format("%.0f%%", actor.getConfidenceScore() * 100.0)),
                createMetaItem("ACTIVE YEARS", actor.getFirstSeen() + " - " + actor.getLastSeen()),
                createMetaItem("PRIMARY PROVENANCE", actor.getProvenance())
        );

        Div ttpSection = new Div();
        ttpSection.getStyle().set("margin-top", "20px");
        H4 ttpHeader = new H4("OBSERVED MITRE ATT&CK TECHNIQUES");
        ttpHeader.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 6px 0");
        Paragraph ttpText = new Paragraph(actor.getObservedTtps());
        ttpText.getStyle().set("color", "#cbd5e0").set("font-size", "0.85rem").set("background", "#0d1117").set("padding", "8px").set("border-radius", "4px");
        ttpSection.add(ttpHeader, ttpText);

        Div malSection = new Div();
        malSection.getStyle().set("margin-top", "15px");
        H4 malHeader = new H4("CUSTOM & COMMODITY MALWARE ARSENAL");
        malHeader.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 6px 0");
        Paragraph malText = new Paragraph(actor.getObservedMalware());
        malText.getStyle().set("color", "#cbd5e0").set("font-size", "0.85rem").set("background", "#0d1117").set("padding", "8px").set("border-radius", "4px");
        malSection.add(malHeader, malText);

        Div targetSection = new Div();
        targetSection.getStyle().set("margin-top", "15px");
        H4 targetHeader = new H4("TARGET SECTORS & GEOPOLITICAL SPHERES");
        targetHeader.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 6px 0");
        Paragraph targetText = new Paragraph("Sectors: " + actor.getTargetSectors() + " | Nations: " + actor.getTargetCountries());
        targetText.getStyle().set("color", "#cbd5e0").set("font-size", "0.85rem").set("background", "#0d1117").set("padding", "8px").set("border-radius", "4px");
        targetSection.add(targetHeader, targetText);

        detailPanel.add(name, aliases, desc, metaGrid, ttpSection, malSection, targetSection);
    }

    private Div createMetaItem(String label, String value) {
        Div item = new Div();
        item.getStyle().set("background", "#0d1117").set("padding", "8px 12px").set("border-radius", "4px");
        Span l = new Span(label);
        l.getStyle().set("color", "#718096").set("font-size", "0.7rem").set("display", "block").set("font-weight", "bold");
        Span v = new Span(value);
        v.getStyle().set("color", "#fff").set("font-size", "0.85rem").set("font-weight", "600");
        item.add(l, v);
        return item;
    }

    private void updateList(String filter) {
        if (filter == null || filter.isBlank()) {
            grid.setItems(actorRepository.findAll());
        } else {
            grid.setItems(actorRepository.searchByNameOrAlias(filter.trim()));
        }
    }
}
