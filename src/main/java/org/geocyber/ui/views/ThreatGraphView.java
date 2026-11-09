package org.geocyber.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.graph.ThreatGraphService;
import org.geocyber.ui.MainLayout;

import java.util.List;
import java.util.Map;

@Route(value = "graph", layout = MainLayout.class)
@PageTitle("Threat Graph | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class ThreatGraphView extends VerticalLayout {

    private final ThreatGraphService graphService;
    private final Div graphVisualization = new Div();
    private final Div pathResultDiv = new Div();

    public ThreatGraphView(ThreatGraphService graphService) {
        this.graphService = graphService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("INTERACTIVE THREAT RELATIONSHIP GRAPH & CENTRALITY ANALYSIS");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("JGraphT-powered topological analysis mapping threat actors, toolchains, TTPs, and sponsor states.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        // Shortest attribution path query controls
        HorizontalLayout pathControls = new HorizontalLayout();
        pathControls.setWidthFull();
        pathControls.setDefaultVerticalComponentAlignment(Alignment.BASELINE);

        TextField sourceEntityField = new TextField("Source Artifact (Malware, Campaign, or Technique)");
        sourceEntityField.setValue("Industroyer");
        sourceEntityField.setWidth("350px");

        TextField targetCountryField = new TextField("Target State (e.g. RU, CN, IR, KP)");
        targetCountryField.setValue("RU");
        targetCountryField.setWidth("200px");

        Button tracePathBtn = new Button("TRACE ATTRIBUTION PATH", e -> {
            List<String> path = graphService.findAttributionPath(sourceEntityField.getValue(), targetCountryField.getValue());
            renderPath(path);
        });
        tracePathBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        tracePathBtn.getStyle().set("background", "#00ffcc").set("color", "#000");

        pathControls.add(sourceEntityField, targetCountryField, tracePathBtn);

        pathResultDiv.getStyle()
                .set("background", "#161b22")
                .set("padding", "10px 16px")
                .set("border-radius", "4px")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("margin-bottom", "10px");
        renderPath(graphService.findAttributionPath("Industroyer", "RU"));

        HorizontalLayout mainContent = new HorizontalLayout();
        mainContent.setSizeFull();

        renderVisualGraph();
        graphVisualization.setWidth("65%");
        graphVisualization.setHeightFull();

        // Centrality table
        VerticalLayout tableSide = new VerticalLayout();
        tableSide.setWidth("35%");
        tableSide.setHeightFull();
        tableSide.setPadding(false);

        H3 centralityHeading = new H3("PageRank Centrality Hubs");
        centralityHeading.getStyle().set("color", "#00ffcc").set("font-size", "0.95rem").set("margin", "0 0 8px 0");

        Grid<Map.Entry<String, Double>> centralityGrid = new Grid<>();
        centralityGrid.setSizeFull();
        centralityGrid.setItems(graphService.computeCentralityScores().entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(20)
                .toList());

        centralityGrid.addColumn(Map.Entry::getKey).setHeader("Graph Node").setAutoWidth(true);
        centralityGrid.addColumn(e -> String.format("%.4f", e.getValue())).setHeader("Centrality Score").setAutoWidth(true);

        tableSide.add(centralityHeading, centralityGrid);

        mainContent.add(graphVisualization, tableSide);

        add(title, sub, pathControls, pathResultDiv, mainContent);
    }

    private void renderPath(List<String> path) {
        pathResultDiv.removeAll();
        if (path.isEmpty()) {
            Span none = new Span("No direct topological path discovered between entities.");
            none.getStyle().set("color", "#ff5555").set("font-size", "0.85rem");
            pathResultDiv.add(none);
            return;
        }

        Span heading = new Span("Dijkstra Shortest Attribution Path: ");
        heading.getStyle().set("color", "#718096").set("font-size", "0.8rem").set("font-weight", "bold");

        Span pathStr = new Span(String.join("  →  ", path));
        pathStr.getStyle().set("color", "#39ff14").set("font-weight", "bold").set("font-size", "0.9rem");

        pathResultDiv.add(heading, pathStr);
    }

    private void renderVisualGraph() {
        graphVisualization.getStyle()
                .set("background", "#161b22")
                .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                .set("border-radius", "6px")
                .set("padding", "16px")
                .set("overflow", "hidden");

        ThreatGraphService.ThreatGraphModel model = graphService.getVisualThreatGraph();

        StringBuilder svg = new StringBuilder();
        svg.append("<svg viewBox=\"0 0 700 450\" style=\"width:100%; height:100%; background:#0d1117; border-radius:6px;\">");

        // Central Russian Cluster
        svg.append("<circle cx=\"350\" cy=\"225\" r=\"26\" fill=\"#ff3366\" opacity=\"0.9\"/>");
        svg.append("<text x=\"325\" y=\"230\" fill=\"#fff\" font-size=\"11\" font-weight=\"bold\">RUSSIA</text>");

        // Actors around RU
        svg.append("<line x1=\"350\" y1=\"225\" x2=\"230\" y2=\"160\" stroke=\"#ff3366\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"230\" cy=\"160\" r=\"16\" fill=\"#ff5555\"/>");
        svg.append("<text x=\"205\" y=\"164\" fill=\"#fff\" font-size=\"9\">APT28</text>");

        svg.append("<line x1=\"350\" y1=\"225\" x2=\"280\" y2=\"320\" stroke=\"#ff3366\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"280\" cy=\"320\" r=\"16\" fill=\"#ff5555\"/>");
        svg.append("<text x=\"255\" y=\"324\" fill=\"#fff\" font-size=\"9\">APT29</text>");

        svg.append("<line x1=\"350\" y1=\"225\" x2=\"440\" y2=\"140\" stroke=\"#ff3366\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"440\" cy=\"140\" r=\"18\" fill=\"#ff0033\"/>");
        svg.append("<text x=\"410\" y=\"144\" fill=\"#fff\" font-size=\"9\" font-weight=\"bold\">Sandworm</text>");

        // Malware connected to Sandworm
        svg.append("<line x1=\"440\" y1=\"140\" x2=\"520\" y2=\"80\" stroke=\"#718096\" stroke-width=\"1.5\" stroke-dasharray=\"3,3\"/>");
        svg.append("<rect x=\"480\" y=\"65\" width=\"80\" height=\"22\" rx=\"4\" fill=\"#21262d\" stroke=\"#00ffcc\"/>");
        svg.append("<text x=\"490\" y=\"80\" fill=\"#00ffcc\" font-size=\"8\">Industroyer2</text>");

        // China Cluster
        svg.append("<circle cx=\"550\" cy=\"320\" r=\"24\" fill=\"#00ffcc\" opacity=\"0.9\"/>");
        svg.append("<text x=\"532\" y=\"325\" fill=\"#000\" font-size=\"11\" font-weight=\"bold\">CHINA</text>");

        svg.append("<line x1=\"550\" y1=\"320\" x2=\"610\" y2=\"240\" stroke=\"#00ffcc\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"610\" cy=\"240\" r=\"16\" fill=\"#00bfa5\"/>");
        svg.append("<text x=\"580\" y=\"244\" fill=\"#000\" font-size=\"8\" font-weight=\"bold\">Volt Typhoon</text>");

        // DPRK Cluster
        svg.append("<circle cx=\"140\" cy=\"280\" r=\"20\" fill=\"#aa00ff\" opacity=\"0.9\"/>");
        svg.append("<text x=\"122\" y=\"284\" fill=\"#fff\" font-size=\"10\" font-weight=\"bold\">DPRK</text>");

        svg.append("<line x1=\"140\" y1=\"280\" x2=\"100\" y2=\"200\" stroke=\"#aa00ff\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"100\" cy=\"200\" r=\"16\" fill=\"#7b1fa2\"/>");
        svg.append("<text x=\"80\" y=\"204\" fill=\"#fff\" font-size=\"8\">Lazarus</text>");

        // Iran Cluster
        svg.append("<circle cx=\"180\" cy=\"90\" r=\"20\" fill=\"#ff9900\" opacity=\"0.9\"/>");
        svg.append("<text x=\"165\" y=\"94\" fill=\"#000\" font-size=\"10\" font-weight=\"bold\">IRAN</text>");

        svg.append("<line x1=\"180\" y1=\"90\" x2=\"100\" y2=\"110\" stroke=\"#ff9900\" stroke-width=\"2\"/>");
        svg.append("<circle cx=\"100\" cy=\"110\" r=\"14\" fill=\"#f57c00\"/>");
        svg.append("<text x=\"60\" y=\"114\" fill=\"#fff\" font-size=\"8\">Charming Kitten</text>");

        svg.append("</svg>");

        graphVisualization.getElement().setProperty("innerHTML", svg.toString());
    }
}
