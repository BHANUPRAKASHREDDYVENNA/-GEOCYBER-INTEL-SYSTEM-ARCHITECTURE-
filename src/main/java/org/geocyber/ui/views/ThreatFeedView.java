package org.geocyber.ui.views;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.domain.GdeltEvent;
import org.geocyber.domain.ThreatReport;
import org.geocyber.repository.GdeltEventRepository;
import org.geocyber.repository.ThreatReportRepository;
import org.geocyber.ui.MainLayout;

import java.util.List;

@Route(value = "feed", layout = MainLayout.class)
@PageTitle("Live Intelligence Feed | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class ThreatFeedView extends VerticalLayout {

    private final GdeltEventRepository gdeltRepository;
    private final ThreatReportRepository reportRepository;

    private final Grid<GdeltEvent> gdeltGrid = new Grid<>(GdeltEvent.class, false);
    private final Grid<ThreatReport> reportGrid = new Grid<>(ThreatReport.class, false);

    public ThreatFeedView(GdeltEventRepository gdeltRepository, ThreatReportRepository reportRepository) {
        this.gdeltRepository = gdeltRepository;
        this.reportRepository = reportRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("REAL-TIME INTELLIGENCE INGESTION STREAM");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Ingesting GDELT 2.0 geopolitical events and APTnotes CTI threat intelligence feeds.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        Tab gdeltTab = new Tab("GDELT 2.0 Geopolitical Events");
        Tab ctiTab = new Tab("APTnotes CTI Threat Reports");
        Tabs tabs = new Tabs(gdeltTab, ctiTab);

        configureGdeltGrid();
        configureReportGrid();

        reportGrid.setVisible(false);

        tabs.addSelectedChangeListener(e -> {
            boolean isGdelt = e.getSelectedTab().equals(gdeltTab);
            gdeltGrid.setVisible(isGdelt);
            reportGrid.setVisible(!isGdelt);
        });

        add(title, sub, tabs, gdeltGrid, reportGrid);
    }

    private void configureGdeltGrid() {
        gdeltGrid.setSizeFull();
        gdeltGrid.setItems(gdeltRepository.findAll());

        gdeltGrid.addColumn(GdeltEvent::getEventDate).setHeader("Date").setAutoWidth(true);
        gdeltGrid.addColumn(e -> e.getActor1Country() + " ↔ " + e.getActor2Country()).setHeader("Actors").setAutoWidth(true);
        gdeltGrid.addColumn(GdeltEvent::getEventCode).setHeader("CAMEO Code").setAutoWidth(true);
        gdeltGrid.addColumn(GdeltEvent::getEventDescription).setHeader("Event Description").setFlexGrow(2);
        gdeltGrid.addColumn(GdeltEvent::getGoldsteinScale).setHeader("Goldstein Scale").setAutoWidth(true);
        gdeltGrid.addColumn(GdeltEvent::getAvgTone).setHeader("Avg Tone").setAutoWidth(true);
        gdeltGrid.addColumn(GdeltEvent::getNumMentions).setHeader("Mentions").setAutoWidth(true);
    }

    private void configureReportGrid() {
        reportGrid.setSizeFull();
        reportGrid.setItems(reportRepository.findAll());

        reportGrid.addColumn(ThreatReport::getPublicationDate).setHeader("Date").setAutoWidth(true);
        reportGrid.addColumn(ThreatReport::getTitle).setHeader("Report Title").setFlexGrow(2);
        reportGrid.addColumn(ThreatReport::getPublisher).setHeader("Publisher").setAutoWidth(true);
        reportGrid.addColumn(ThreatReport::getExtractedActors).setHeader("Extracted Actors").setAutoWidth(true);
        reportGrid.addColumn(ThreatReport::getExtractedTtps).setHeader("Extracted TTPs").setAutoWidth(true);
        reportGrid.addColumn(ThreatReport::getSummary).setHeader("Executive Summary").setFlexGrow(2);
    }
}
