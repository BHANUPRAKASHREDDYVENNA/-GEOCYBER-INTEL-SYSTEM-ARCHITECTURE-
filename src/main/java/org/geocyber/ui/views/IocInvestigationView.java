package org.geocyber.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.ingestion.IocNormalizationService;
import org.geocyber.repository.IocRepository;
import org.geocyber.ui.MainLayout;

import java.util.List;

@Route(value = "iocs", layout = MainLayout.class)
@PageTitle("IOC Investigation Lab | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class IocInvestigationView extends VerticalLayout {

    private final IocNormalizationService iocService;
    private final IocRepository iocRepository;
    private final Grid<IndicatorOfCompromise> grid = new Grid<>(IndicatorOfCompromise.class, false);

    public IocInvestigationView(IocNormalizationService iocService, IocRepository iocRepository) {
        this.iocService = iocService;
        this.iocRepository = iocRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("IOC EXTRACTION, NORMALIZATION & DEFANGING LAB");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Defang malicious indicators, extract observables from raw incident logs, and inspect threat actor infrastructure.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        HorizontalLayout toolsLayout = new HorizontalLayout();
        toolsLayout.setWidthFull();

        // 1. Defanger Tool Card
        Div defangCard = new Div();
        defangCard.setWidth("40%");
        defangCard.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "16px");

        H3 defangTitle = new H3("Quick Indicator Defanger");
        defangTitle.getStyle().set("color", "#00ffcc").set("font-size", "0.95rem").set("margin", "0 0 8px 0");

        TextField rawInput = new TextField("Raw IP / Domain / URL");
        rawInput.setWidthFull();
        rawInput.setPlaceholder("e.g. 192.168.1.1 or http://evil.com/drop");

        TextField defangedOutput = new TextField("Defanged & Normalized Output");
        defangedOutput.setWidthFull();
        defangedOutput.setReadOnly(true);

        Button defangBtn = new Button("DEFANG & NORMALIZE", e -> {
            if (rawInput.getValue() != null && !rawInput.getValue().isBlank()) {
                String def = iocService.defang(rawInput.getValue());
                defangedOutput.setValue(def);
            }
        });
        defangBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        defangBtn.getStyle().set("background", "#00ffcc").set("color", "#000").set("margin-top", "10px");

        defangCard.add(defangTitle, rawInput, defangBtn, defangedOutput);

        // 2. Bulk Text Extraction Card
        Div bulkCard = new Div();
        bulkCard.setWidth("60%");
        bulkCard.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "16px");

        H3 bulkTitle = new H3("Automated Unstructured Text IOC Parser");
        bulkTitle.getStyle().set("color", "#00ffcc").set("font-size", "0.95rem").set("margin", "0 0 8px 0");

        TextArea rawTextArea = new TextArea("Paste Raw Threat Advisory or Syslog Block");
        rawTextArea.setWidthFull();
        rawTextArea.setHeight("100px");
        rawTextArea.setPlaceholder("Paste text containing IPs, URLs, SHA256 hashes, or CVE numbers...");

        Button parseBtn = new Button("EXTRACT & INGEST ALL IOCs", e -> {
            String text = rawTextArea.getValue();
            if (text != null && !text.isBlank()) {
                List<IndicatorOfCompromise> extracted = iocService.extractFromText(text, "MANUAL_INVESTIGATION", "UNC");
                for (IndicatorOfCompromise ioc : extracted) {
                    iocService.saveOrUpdate(ioc);
                }
                grid.setItems(iocRepository.findTop50ByOrderByCreatedAtDesc());
                Notification n = Notification.show("Successfully parsed and ingested " + extracted.size() + " IOCs!", 3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });
        parseBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        parseBtn.getStyle().set("background", "#0099ff").set("color", "#fff").set("margin-top", "10px");

        bulkCard.add(bulkTitle, rawTextArea, parseBtn);

        toolsLayout.add(defangCard, bulkCard);

        configureGrid();

        add(title, sub, toolsLayout, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setItems(iocRepository.findTop50ByOrderByCreatedAtDesc());

        grid.addColumn(IndicatorOfCompromise::getIocType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(IndicatorOfCompromise::getDefangedValue).setHeader("Defanged Value").setFlexGrow(2);
        grid.addColumn(IndicatorOfCompromise::getReputationScore).setHeader("Reputation Score").setAutoWidth(true);
        grid.addColumn(IndicatorOfCompromise::getConfidenceLevel).setHeader("Confidence").setAutoWidth(true);
        grid.addColumn(IndicatorOfCompromise::getAssociatedActors).setHeader("Associated Actors").setAutoWidth(true);
        grid.addColumn(IndicatorOfCompromise::getThreatFeed).setHeader("Source Feed").setAutoWidth(true);
    }
}
