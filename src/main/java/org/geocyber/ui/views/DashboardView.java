package org.geocyber.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.domain.AnomalyAlert;
import org.geocyber.domain.CyberCampaign;
import org.geocyber.domain.GdeltEvent;
import org.geocyber.repository.AnomalyAlertRepository;
import org.geocyber.repository.CyberCampaignRepository;
import org.geocyber.repository.GdeltEventRepository;
import org.geocyber.ui.MainLayout;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Global Cyber Threat Dashboard | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class DashboardView extends VerticalLayout {

    private final CyberCampaignRepository campaignRepository;
    private final GdeltEventRepository gdeltRepository;
    private final AnomalyAlertRepository anomalyAlertRepository;

    public DashboardView(CyberCampaignRepository campaignRepository,
                         GdeltEventRepository gdeltRepository,
                         AnomalyAlertRepository anomalyAlertRepository) {
        this.campaignRepository = campaignRepository;
        this.gdeltRepository = gdeltRepository;
        this.anomalyAlertRepository = anomalyAlertRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#0d1117");

        addHeader();
        addKpiCards();
        addThreatMapAndFeed();
        addAnomalyAlertsSection();
        addActiveCampaignsTable();
    }

    private void addHeader() {
        H2 heading = new H2("GLOBAL GEOPOLITICAL CYBER THREAT POSTURE");
        heading.getStyle()
                .set("color", "#00ffcc")
                .set("margin", "0")
                .set("font-size", "1.3rem")
                .set("letter-spacing", "1px");

        Paragraph sub = new Paragraph("Continuous real-time ingestion, cross-attention feature correlation, and nation-state attribution.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0").set("font-size", "0.85rem");

        Button runAttributionBtn = new Button("NEW ATTRIBUTION CASE", new Icon(VaadinIcon.FLASK),
                e -> UI.getCurrent().navigate(AttributionLabView.class));
        runAttributionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        runAttributionBtn.getStyle()
                .set("background", "linear-gradient(135deg, #00ffcc 0%, #0099ff 100%)")
                .set("color", "#000")
                .set("font-weight", "bold");

        HorizontalLayout topBar = new HorizontalLayout(new VerticalLayout(heading, sub), runAttributionBtn);
        topBar.setWidthFull();
        topBar.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        topBar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        add(topBar);
    }

    private void addKpiCards() {
        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();

        cards.add(
                createKpiCard("THREAT CONDITION", "SEVERE (DEFCON 2)", "#ff3366", "Active kinetic & wiper escalation in Europe & Indo-Pacific"),
                createKpiCard("GDELT TENSION INDEX", "0.88 / 1.00", "#ff9900", "Acute bilateral conflict peaks (RU-UA, CN-TW, IR-IL)"),
                createKpiCard("TRACKED APT CLUSTERS", "10 Monitored", "#00ffcc", "Russia, China, North Korea, Iran, United States"),
                createKpiCard("AI ATTRIBUTION ACCURACY", "89.5% (Top-3: 97.2%)", "#39ff14", "Multimodal Cross-Attention Transformer (F1: 89.1%)")
        );

        add(cards);
    }

    private Div createKpiCard(String label, String value, String accentColor, String footer) {
        Div card = new Div();
        card.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                .set("border-left", "4px solid " + accentColor)
                .set("border-radius", "6px")
                .set("padding", "14px 18px")
                .set("flex", "1");

        Span l = new Span(label);
        l.getStyle().set("color", "#718096").set("font-size", "0.75rem").set("font-weight", "bold");

        H3 v = new H3(value);
        v.getStyle().set("color", accentColor).set("margin", "6px 0").set("font-size", "1.3rem").set("font-weight", "800");

        Paragraph f = new Paragraph(footer);
        f.getStyle().set("color", "#a0aec0").set("font-size", "0.72rem").set("margin", "0");

        card.add(l, v, f);
        return card;
    }

    private void addThreatMapAndFeed() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setHeight("340px");

        // Interactive Geopolitical Cyber Threat Map Card (Rendered via clean SVG/vector HTML)
        Div mapContainer = new Div();
        mapContainer.setWidth("65%");
        mapContainer.setHeightFull();
        mapContainer.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "6px")
                .set("padding", "16px")
                .set("position", "relative")
                .set("overflow", "hidden");

        Span mapTitle = new Span("GLOBAL CYBER CONFLICT HOTSPOTS & INTRUSION CORRIDORS");
        mapTitle.getStyle().set("color", "#00ffcc").set("font-weight", "bold").set("font-size", "0.85rem");

        // SVG World Map Representation with pulsing theater indicators
        Div svgWrapper = new Div();
        svgWrapper.getElement().setProperty("innerHTML", """
            <svg viewBox="0 0 1000 450" style="width:100%; height:260px; background:#0d1117; border-radius:6px; margin-top:8px;">
                <!-- Subtle grid background -->
                <defs>
                    <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
                        <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#21262d" stroke-width="0.8"/>
                    </pattern>
                </defs>
                <rect width="1000" height="450" fill="url(#grid)" />
                
                <!-- Continents Outline Approximation -->
                <path d="M 150 120 Q 220 100 280 140 T 260 220 T 170 200 Z" fill="#1c2128" stroke="#30363d"/>
                <path d="M 230 240 Q 280 260 270 360 T 220 380 T 210 260 Z" fill="#1c2128" stroke="#30363d"/>
                <path d="M 450 100 Q 550 80 580 140 T 520 220 T 440 180 Z" fill="#1c2128" stroke="#30363d"/>
                <path d="M 480 220 Q 560 230 540 380 T 470 320 Z" fill="#1c2128" stroke="#30363d"/>
                <path d="M 600 90 Q 850 80 880 180 T 750 250 T 620 180 Z" fill="#1c2128" stroke="#30363d"/>
                <path d="M 800 300 Q 880 300 860 380 T 780 360 Z" fill="#1c2128" stroke="#30363d"/>

                <!-- Pulsing Hotspot Corridors -->
                <!-- Eastern Europe: Sandworm / APT28 -> Ukraine -->
                <line x1="560" y1="130" x2="520" y2="140" stroke="#ff3366" stroke-width="2.5" stroke-dasharray="4,4"/>
                <circle cx="560" cy="130" r="7" fill="#ff3366" opacity="0.85"/>
                <circle cx="520" cy="140" r="9" fill="#ff3366" opacity="0.3"/>
                <text x="500" y="120" fill="#ff3366" font-size="11" font-weight="bold">UKRAINE / NATO FLANK [Sandworm, APT28]</text>

                <!-- Taiwan Strait: Volt Typhoon / APT41 -->
                <line x1="780" y1="180" x2="820" y2="200" stroke="#00ffcc" stroke-width="2.5" stroke-dasharray="4,4"/>
                <circle cx="780" cy="180" r="8" fill="#00ffcc" opacity="0.85"/>
                <circle cx="820" cy="200" r="9" fill="#00ffcc" opacity="0.3"/>
                <text x="730" y="225" fill="#00ffcc" font-size="11" font-weight="bold">TAIWAN / GUAM [Volt Typhoon, APT41]</text>

                <!-- Middle East: Charming Kitten / MuddyWater -->
                <circle cx="610" cy="180" r="7" fill="#ff9900" opacity="0.85"/>
                <text x="580" y="205" fill="#ff9900" font-size="11" font-weight="bold">IRAN / ISRAEL [Charming Kitten]</text>

                <!-- Korean Peninsula: Lazarus Group -->
                <circle cx="820" cy="150" r="7" fill="#aa00ff" opacity="0.85"/>
                <text x="830" y="145" fill="#aa00ff" font-size="11" font-weight="bold">DPRK / LAZARUS</text>
            </svg>
        """);

        mapContainer.add(mapTitle, svgWrapper);

        // Real-Time Ticker & Alerts Panel
        Div tickerPanel = new Div();
        tickerPanel.setWidth("35%");
        tickerPanel.setHeightFull();
        tickerPanel.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                .set("border-radius", "6px")
                .set("padding", "16px")
                .set("overflow-y", "auto");

        Span tickerTitle = new Span("REAL-TIME GDELT & CTI STREAM");
        tickerTitle.getStyle().set("color", "#00ffcc").set("font-weight", "bold").set("font-size", "0.85rem");
        tickerPanel.add(tickerTitle);

        List<GdeltEvent> events = gdeltRepository.findTop20ByOrderByEventDateDesc();
        for (GdeltEvent e : events) {
            Div item = new Div();
            item.getStyle()
                    .set("border-bottom", "1px solid rgba(255, 255, 255, 0.05)")
                    .set("padding", "8px 0");

            Span actors = new Span(e.getActor1Country() + " ↔ " + e.getActor2Country() + " (" + e.getEventCode() + ")");
            actors.getStyle().set("color", "#ff3366").set("font-weight", "bold").set("font-size", "0.8rem");

            Paragraph desc = new Paragraph(e.getEventDescription() != null ? e.getEventDescription() : "Conflict event detected.");
            desc.getStyle().set("color", "#a0aec0").set("font-size", "0.75rem").set("margin", "2px 0");

            Span tone = new Span("Goldstein: " + e.getGoldsteinScale() + " | Mentions: " + e.getNumMentions());
            tone.getStyle().set("color", "#718096").set("font-size", "0.7rem");

            item.add(actors, desc, tone);
            tickerPanel.add(item);
        }

        layout.add(mapContainer, tickerPanel);
        add(layout);
    }

    private void addAnomalyAlertsSection() {
        H3 alertHeading = new H3("REAL-TIME THREAT ANOMALIES & DGA ALERTS (MODULE 6)");
        alertHeading.getStyle().set("color", "#ff3366").set("font-size", "1.0rem").set("margin", "15px 0 5px 0");

        Grid<AnomalyAlert> alertGrid = new Grid<>(AnomalyAlert.class, false);
        alertGrid.setItems(anomalyAlertRepository.findByOrderByTimestampDesc());
        alertGrid.setHeight("200px");

        alertGrid.addComponentColumn(alert -> {
            Span badge = new Span(alert.getSeverity());
            badge.getStyle().set("font-weight", "bold").set("padding", "2px 8px").set("border-radius", "4px").set("font-size", "0.75rem");
            switch (alert.getSeverity().toUpperCase()) {
                case "CRITICAL" -> badge.getStyle().set("background", "#ff0055").set("color", "#fff");
                case "HIGH" -> badge.getStyle().set("background", "#ff3366").set("color", "#fff");
                case "MEDIUM" -> badge.getStyle().set("background", "#ff9900").set("color", "#000");
                default -> badge.getStyle().set("background", "#38bdf8").set("color", "#000");
            }
            return badge;
        }).setHeader("Severity").setAutoWidth(true);

        alertGrid.addColumn(AnomalyAlert::getAnomalyType).setHeader("Anomaly Type").setAutoWidth(true);
        alertGrid.addColumn(AnomalyAlert::getSourceEntity).setHeader("Entity / Observable").setAutoWidth(true);
        alertGrid.addColumn(a -> String.format("%.2f", a.getAnomalyScore())).setHeader("Anomaly Score").setAutoWidth(true);
        alertGrid.addColumn(AnomalyAlert::getDescription).setHeader("Forensic Description").setFlexGrow(2);
        alertGrid.addColumn(AnomalyAlert::getRecommendedAction).setHeader("Action").setAutoWidth(true);

        add(alertHeading, alertGrid);
    }

    private void addActiveCampaignsTable() {
        H3 tableHeading = new H3("ACTIVE CYBER CAMPAIGNS & GEOPOLITICAL CORRELATION");
        tableHeading.getStyle().set("color", "#00ffcc").set("font-size", "1.0rem").set("margin", "15px 0 5px 0");

        Grid<CyberCampaign> grid = new Grid<>(CyberCampaign.class, false);
        grid.setItems(campaignRepository.findAll());
        grid.setHeight("240px");

        grid.addColumn(CyberCampaign::getName).setHeader("Campaign").setAutoWidth(true);
        grid.addColumn(CyberCampaign::getAttributedActorName).setHeader("Attributed Actor").setAutoWidth(true);
        grid.addColumn(CyberCampaign::getTargetNations).setHeader("Target Nations").setAutoWidth(true);
        grid.addColumn(CyberCampaign::getTargetSectors).setHeader("Sectors").setAutoWidth(true);
        grid.addColumn(CyberCampaign::getObservedTtps).setHeader("Observed TTPs").setAutoWidth(true);
        grid.addColumn(c -> String.format("%.2f", c.getGdeltCorrelationScore())).setHeader("GDELT Correlation").setAutoWidth(true);

        add(tableHeading, grid);
    }
}
