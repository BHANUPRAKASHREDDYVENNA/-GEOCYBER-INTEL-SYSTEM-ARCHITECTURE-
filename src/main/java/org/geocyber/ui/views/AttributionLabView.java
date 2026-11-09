package org.geocyber.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.ai.attribution.AttributionReport;
import org.geocyber.ai.attribution.ProbabilisticAttributionEngine;
import org.geocyber.ai.features.FeatureExtractor;
import org.geocyber.ai.features.MultiModalFeatureVector;
import org.geocyber.investigation.InvestigationCaseService;
import org.geocyber.ui.MainLayout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Route(value = "ai-lab", layout = MainLayout.class)
@PageTitle("AI Attribution Laboratory | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class AttributionLabView extends VerticalLayout {

    private final FeatureExtractor featureExtractor;
    private final ProbabilisticAttributionEngine attributionEngine;
    private final InvestigationCaseService caseService;

    // Inputs
    private final TextArea geopoliticalContextField = new TextArea("Geopolitical Conflict Context & Recent News");
    private final ComboBox<String> sourceCountryCombo = new ComboBox<>("Suspected State Sphere");
    private final TextField targetCountryField = new TextField("Target Victim Country");
    private final TextField targetSectorField = new TextField("Target Industry Sector");
    private final TextArea observedTtpsField = new TextArea("Observed MITRE ATT&CK Techniques");
    private final TextArea rawIocsField = new TextArea("Observed Network IOCs, Hashes & Ports");
    private final DatePicker incidentDatePicker = new DatePicker("Incident Date");

    // Results container
    private final Div resultsContainer = new Div();

    public AttributionLabView(FeatureExtractor featureExtractor,
                              ProbabilisticAttributionEngine attributionEngine,
                              InvestigationCaseService caseService) {
        this.featureExtractor = featureExtractor;
        this.attributionEngine = attributionEngine;
        this.caseService = caseService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("MULTIMODAL CROSS-ATTENTION ATTRIBUTION LABORATORY");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Deep neural cross-attention fusing news NLP, GDELT tension, MITRE TTPs, network IOCs, and temporal signatures.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        add(title, sub);
        addPresetBar();
        addInputForm();
        add(resultsContainer);

        // Preload Sandworm preset
        loadPreset1();
    }

    private void addPresetBar() {
        HorizontalLayout presets = new HorizontalLayout();
        presets.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        Span label = new Span("Load Real-World Incident Archetype: ");
        label.getStyle().set("color", "#718096").set("font-size", "0.8rem").set("font-weight", "bold");

        Button p1 = new Button("1. Ukraine SCADA Wiper (Sandworm)", e -> loadPreset1());
        Button p2 = new Button("2. Pacific SOHO LOTL (Volt Typhoon)", e -> loadPreset2());
        Button p3 = new Button("3. M365 Diplomatic Phish (APT29)", e -> loadPreset3());
        Button p4 = new Button("4. Novel Unknown Cluster", e -> loadPresetNovel());

        p1.addThemeVariants(ButtonVariant.LUMO_SMALL);
        p2.addThemeVariants(ButtonVariant.LUMO_SMALL);
        p3.addThemeVariants(ButtonVariant.LUMO_SMALL);
        p4.addThemeVariants(ButtonVariant.LUMO_SMALL);

        presets.add(label, p1, p2, p3, p4);
        add(presets);
    }

    private void loadPreset1() {
        geopoliticalContextField.setValue("Severe escalation in missile strikes targeting Ukrainian thermal energy stations. GDELT conflict code 190. Diplomatic talks suspended.");
        sourceCountryCombo.setValue("RU");
        targetCountryField.setValue("UA");
        targetSectorField.setValue("Energy / Power Grid");
        observedTtpsField.setValue("T1486, T1562.001, T1055, T1059.003, T1071.001");
        rawIocsField.setValue("194.26.29.112, caddywiper.exe, CVE-2024-21413, port 443");
        incidentDatePicker.setValue(LocalDate.now().minusDays(3));
    }

    private void loadPreset2() {
        geopoliticalContextField.setValue("Chinese naval carrier groups conduct simulated blockade of Taiwan Strait and Western Pacific choke points.");
        sourceCountryCombo.setValue("CN");
        targetCountryField.setValue("GU");
        targetSectorField.setValue("Ports & Water Infrastructure");
        observedTtpsField.setValue("T1078, T1190, T1059.003, T1082, T1071.001");
        rawIocsField.setValue("45.154.255.89, cisco-telemetry[.]net, port 8443, living-off-the-land");
        incidentDatePicker.setValue(LocalDate.now().minusDays(2));
    }

    private void loadPreset3() {
        geopoliticalContextField.setValue("NATO foreign ministers assemble for emergency sanctions coordination summit in Brussels.");
        sourceCountryCombo.setValue("RU");
        targetCountryField.setValue("DE");
        targetSectorField.setValue("Government / Foreign Policy");
        observedTtpsField.setValue("T1190, T1078, T1059.001, T1071.001, T1082");
        rawIocsField.setValue("13.59.205.66, ms-graph-sync[.]org, port 443");
        incidentDatePicker.setValue(LocalDate.now().minusDays(5));
    }

    private void loadPresetNovel() {
        geopoliticalContextField.setValue("Peaceful trade delegations and cultural exchange in South America.");
        sourceCountryCombo.setValue("UNKNOWN");
        targetCountryField.setValue("BR");
        targetSectorField.setValue("Agriculture");
        observedTtpsField.setValue("T1059.005");
        rawIocsField.setValue("127.0.0.1");
        incidentDatePicker.setValue(LocalDate.now());
    }

    private void addInputForm() {
        Div formCard = new Div();
        formCard.setWidthFull();
        formCard.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "18px")
                .set("margin-bottom", "15px");

        sourceCountryCombo.setItems("RU", "CN", "KP", "IR", "US", "UNKNOWN");

        HorizontalLayout row1 = new HorizontalLayout(sourceCountryCombo, targetCountryField, targetSectorField, incidentDatePicker);
        row1.setWidthFull();

        geopoliticalContextField.setWidthFull();
        geopoliticalContextField.setHeight("80px");

        HorizontalLayout row2 = new HorizontalLayout(observedTtpsField, rawIocsField);
        row2.setWidthFull();
        observedTtpsField.setWidth("50%");
        rawIocsField.setWidth("50%");

        Button runAttributionBtn = new Button("EXECUTE MULTIMODAL CROSS-ATTENTION ATTRIBUTION", e -> runAttribution());
        runAttributionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        runAttributionBtn.getStyle()
                .set("background", "linear-gradient(135deg, #00ffcc 0%, #0099ff 100%)")
                .set("color", "#000")
                .set("font-weight", "900")
                .set("font-size", "0.95rem")
                .set("letter-spacing", "1px")
                .set("margin-top", "14px")
                .set("height", "45px");

        formCard.add(row1, geopoliticalContextField, row2, runAttributionBtn);
        add(formCard);
    }

    private void runAttribution() {
        MultiModalFeatureVector fv = featureExtractor.extract(
                geopoliticalContextField.getValue(),
                sourceCountryCombo.getValue(),
                targetCountryField.getValue(),
                incidentDatePicker.getValue(),
                incidentDatePicker.getValue() != null ? incidentDatePicker.getValue().atTime(10, 0) : LocalDateTime.now(),
                observedTtpsField.getValue(),
                rawIocsField.getValue(),
                null,
                "MANUAL_LAB"
        );

        AttributionReport report = attributionEngine.performAttribution(
                fv, observedTtpsField.getValue(), rawIocsField.getValue(),
                targetCountryField.getValue(), targetSectorField.getValue()
        );

        renderResults(report);

        Notification n = Notification.show("Attribution analysis completed with calibrated confidence " +
                String.format("%.1f%%", report.getCalibratedConfidence() * 100.0), 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void renderResults(AttributionReport report) {
        resultsContainer.removeAll();
        resultsContainer.setWidthFull();

        // 1. Top Verdict Banner
        Div verdictBanner = new Div();
        verdictBanner.getStyle()
                .set("background", report.isUnknownActorDetected() ? "rgba(255, 85, 85, 0.15)" : "rgba(0, 255, 204, 0.1)")
                .set("border", "1px solid " + (report.isUnknownActorDetected() ? "#ff5555" : "#00ffcc"))
                .set("border-radius", "6px")
                .set("padding", "16px 20px")
                .set("margin-bottom", "15px");

        H3 verdictTitle = new H3("ATTRIBUTION DECISION: " + report.getTopAttributedActor().toUpperCase());
        verdictTitle.getStyle().set("color", report.isUnknownActorDetected() ? "#ff5555" : "#00ffcc").set("margin", "0 0 6px 0");

        Span confSpan = new Span("Calibrated Confidence: " + String.format("%.2f%%", report.getCalibratedConfidence() * 100.0) +
                "  |  Sponsoring Nation: " + report.getTopAttributedNation() +
                "  |  Unknown Novel Cluster: " + (report.isUnknownActorDetected() ? "TRIGGERED (High Entropy)" : "NEGATIVE"));
        confSpan.getStyle().set("color", "#fff").set("font-weight", "bold").set("font-size", "0.85rem");

        verdictBanner.add(verdictTitle, confSpan);

        // 2. Ranked Hypotheses Table
        H4 hypHeader = new H4("RANKED THREAT ACTOR HYPOTHESES (POSTERIOR PROBABILITIES)");
        hypHeader.getStyle().set("color", "#00ffcc").set("font-size", "0.9rem").set("margin", "15px 0 6px 0");

        Grid<AttributionReport.Hypothesis> hypGrid = new Grid<>();
        hypGrid.setHeight("180px");
        hypGrid.setItems(report.getRankedHypotheses().stream().limit(5).toList());

        hypGrid.addColumn(AttributionReport.Hypothesis::getActorName).setHeader("Actor Hypothesis").setAutoWidth(true);
        hypGrid.addColumn(AttributionReport.Hypothesis::getNationState).setHeader("Nation State").setAutoWidth(true);
        hypGrid.addColumn(h -> String.format("%.2f%%", h.getPosteriorProbability() * 100.0)).setHeader("Posterior Probability").setAutoWidth(true);
        hypGrid.addColumn(AttributionReport.Hypothesis::getMotivation).setHeader("Doctrine / Motivation").setAutoWidth(true);
        hypGrid.addColumn(h -> String.join(", ", h.getMatchingTtps())).setHeader("Matching TTPs").setAutoWidth(true);

        // 3. Modality Importance & Cross-Attention Heatmap
        HorizontalLayout aiExplainability = new HorizontalLayout();
        aiExplainability.setWidthFull();

        // Modality weights bar
        Div modWeightsDiv = new Div();
        modWeightsDiv.setWidth("45%");
        modWeightsDiv.getStyle().set("background", "#161b22").set("padding", "16px").set("border-radius", "6px");
        H4 modTitle = new H4("Modality Contribution Weights");
        modTitle.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 10px 0");
        modWeightsDiv.add(modTitle);

        for (Map.Entry<String, Double> entry : report.getModalityImportance().entrySet()) {
            Span name = new Span(entry.getKey() + " (" + String.format("%.1f%%", entry.getValue() * 100.0) + ")");
            name.getStyle().set("color", "#cbd5e0").set("font-size", "0.75rem");
            ProgressBar pb = new ProgressBar(0.0, 1.0, entry.getValue());
            pb.getStyle().set("margin-bottom", "8px");
            modWeightsDiv.add(name, pb);
        }

        // Cross-Attention Heatmap (5x5 HTML table)
        Div heatmapDiv = new Div();
        heatmapDiv.setWidth("55%");
        heatmapDiv.getStyle().set("background", "#161b22").set("padding", "16px").set("border-radius", "6px");
        H4 heatTitle = new H4("5x5 Multimodal Cross-Attention Matrix S(Q, K)");
        heatTitle.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 10px 0");
        heatmapDiv.add(heatTitle);

        String[] modNames = {"News", "GDELT", "TTPs", "IOCs", "Time"};
        StringBuilder tableHtml = new StringBuilder("<table style='width:100%; border-collapse:collapse; font-size:11px; text-align:center;'>");
        tableHtml.append("<tr style='color:#718096;'><th></th><th>News</th><th>GDELT</th><th>TTPs</th><th>IOCs</th><th>Time</th></tr>");

        double[][] attn = report.getCrossAttentionMatrix();
        for (int r = 0; r < 5; r++) {
            tableHtml.append("<tr><td style='color:#718096; font-weight:bold; text-align:left;'>").append(modNames[r]).append("</td>");
            for (int c = 0; c < 5; c++) {
                double val = attn != null ? attn[r][c] : 0.2;
                int alpha = (int) Math.min(255, Math.max(20, val * 500));
                tableHtml.append(String.format("<td style='background:rgba(0,255,204,%s); padding:6px; border:1px solid #21262d;'>%.2f</td>",
                        String.format("%.2f", Math.min(0.85, val * 2.0)), val));
            }
            tableHtml.append("</tr>");
        }
        tableHtml.append("</table>");
        heatmapDiv.getElement().setProperty("innerHTML", tableHtml.toString());

        aiExplainability.add(modWeightsDiv, heatmapDiv);

        // 4. Evidence Breakdown: Supporting vs Contradicting
        HorizontalLayout evidenceLayout = new HorizontalLayout();
        evidenceLayout.setWidthFull();

        Div supportDiv = new Div();
        supportDiv.setWidth("50%");
        supportDiv.getStyle().set("background", "#161b22").set("padding", "16px").set("border-radius", "6px").set("border-left", "3px solid #39ff14");
        H4 supTitle = new H4("SUPPORTING EVIDENCE");
        supTitle.getStyle().set("color", "#39ff14").set("font-size", "0.85rem").set("margin", "0 0 8px 0");
        supportDiv.add(supTitle);
        for (String s : report.getSupportingEvidence()) {
            Paragraph p = new Paragraph("✔ " + s);
            p.getStyle().set("color", "#cbd5e0").set("font-size", "0.8rem").set("margin", "4px 0");
            supportDiv.add(p);
        }

        Div contraDiv = new Div();
        contraDiv.setWidth("50%");
        contraDiv.getStyle().set("background", "#161b22").set("padding", "16px").set("border-radius", "6px").set("border-left", "3px solid #ff5555");
        H4 conTitle = new H4("CONTRADICTING / ANOMALOUS EVIDENCE");
        conTitle.getStyle().set("color", "#ff5555").set("font-size", "0.85rem").set("margin", "0 0 8px 0");
        contraDiv.add(conTitle);
        for (String c : report.getContradictingEvidence()) {
            Paragraph p = new Paragraph("✖ " + c);
            p.getStyle().set("color", "#cbd5e0").set("font-size", "0.8rem").set("margin", "4px 0");
            contraDiv.add(p);
        }

        evidenceLayout.add(supportDiv, contraDiv);

        // 5. Correlation vs Verified Attribution & Provenance
        Div notesDiv = new Div();
        notesDiv.getStyle().set("background", "#161b22").set("padding", "14px").set("border-radius", "6px").set("margin-top", "12px");
        H4 notesTitle = new H4("ANALYTIC ASSESSMENT & SOURCE PROVENANCE");
        notesTitle.getStyle().set("color", "#00ffcc").set("font-size", "0.85rem").set("margin", "0 0 6px 0");

        Paragraph assess = new Paragraph(report.getCorrelationVsAttributionNotes());
        assess.getStyle().set("color", "#fff").set("font-size", "0.85rem").set("font-weight", "bold");

        Span prov = new Span("Traceable Ground Truth Sources: " + String.join(" | ", report.getProvenanceTrail()));
        prov.getStyle().set("color", "#718096").set("font-size", "0.75rem");

        notesDiv.add(notesTitle, assess, prov);

        resultsContainer.add(verdictBanner, hypHeader, hypGrid, aiExplainability, evidenceLayout, notesDiv);
    }
}
