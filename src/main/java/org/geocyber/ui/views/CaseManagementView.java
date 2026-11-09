package org.geocyber.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
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
import org.geocyber.ai.attribution.AttributionReport;
import org.geocyber.domain.AttributionCase;
import org.geocyber.investigation.InvestigationCaseService;
import org.geocyber.repository.AttributionCaseRepository;
import org.geocyber.ui.MainLayout;

import java.time.LocalDate;

@Route(value = "cases", layout = MainLayout.class)
@PageTitle("Investigation Cases | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class CaseManagementView extends VerticalLayout {

    private final InvestigationCaseService caseService;
    private final AttributionCaseRepository caseRepository;
    private final Grid<AttributionCase> grid = new Grid<>(AttributionCase.class, false);

    public CaseManagementView(InvestigationCaseService caseService, AttributionCaseRepository caseRepository) {
        this.caseService = caseService;
        this.caseRepository = caseRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("CTI INVESTIGATION CASE MANAGEMENT & REPORTING");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Track active threat campaigns, manage investigative hypotheses, and generate structured CTI attribution briefs.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 10px 0");

        Button newCaseBtn = new Button("CREATE NEW INVESTIGATION CASE", e -> openNewCaseDialog());
        newCaseBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newCaseBtn.getStyle().set("background", "#00ffcc").set("color", "#000").set("font-weight", "bold");

        HorizontalLayout topBar = new HorizontalLayout(new VerticalLayout(title, sub), newCaseBtn);
        topBar.setWidthFull();
        topBar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid();

        add(topBar, grid);
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.setItems(caseRepository.findAll());

        grid.addColumn(AttributionCase::getCaseNumber).setHeader("Case Ref").setAutoWidth(true);
        grid.addColumn(AttributionCase::getTitle).setHeader("Incident Title").setFlexGrow(2);
        grid.addColumn(AttributionCase::getLeadAnalyst).setHeader("Lead Analyst").setAutoWidth(true);
        grid.addColumn(AttributionCase::getStatus).setHeader("Status").setAutoWidth(true);
        grid.addColumn(AttributionCase::getIncidentTargetCountry).setHeader("Target Nation").setAutoWidth(true);
        grid.addColumn(AttributionCase::getAttributedActor).setHeader("Attributed Actor").setAutoWidth(true);
        grid.addColumn(c -> c.getCalibratedConfidence() != null ? String.format("%.1f%%", c.getCalibratedConfidence() * 100.0) : "N/A")
                .setHeader("Confidence").setAutoWidth(true);

        grid.addComponentColumn(c -> {
            Button runBtn = new Button("Run AI Attribution", e -> {
                AttributionReport rep = caseService.runAttributionOnCase(c.getId());
                grid.setItems(caseRepository.findAll());
                Notification n = Notification.show("Attributed case " + c.getCaseNumber() + " to " + rep.getTopAttributedActor(),
                        3000, Notification.Position.TOP_CENTER);
                n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });
            runBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            runBtn.getStyle().set("background", "#0099ff");

            Button reportBtn = new Button("Export Report", e -> openReportDialog(c.getId()));
            reportBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);

            HorizontalLayout actions = new HorizontalLayout(runBtn, reportBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }

    private void openReportDialog(Long caseId) {
        Dialog d = new Dialog();
        d.setWidth("800px");
        d.setHeight("600px");

        String md = caseService.exportIntelligenceReportMarkdown(caseId);

        H3 dTitle = new H3("Exported CTI Attribution Brief");
        dTitle.getStyle().set("color", "#00ffcc").set("margin", "0 0 10px 0");

        Pre pre = new Pre(md);
        pre.getStyle()
                .set("background", "#0d1117")
                .set("color", "#00ffcc")
                .set("padding", "15px")
                .set("border-radius", "4px")
                .set("font-size", "12px")
                .set("overflow", "auto")
                .set("height", "450px");

        Button closeBtn = new Button("Close", e -> d.close());

        d.add(dTitle, pre, closeBtn);
        d.open();
    }

    private void openNewCaseDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("650px");

        H3 dTitle = new H3("Create New Investigation Case");
        dTitle.getStyle().set("color", "#00ffcc").set("margin", "0 0 10px 0");

        TextField titleF = new TextField("Case Title");
        titleF.setWidthFull();

        TextField analystF = new TextField("Lead Analyst");
        analystF.setValue("Alex Mercer");
        analystF.setWidthFull();

        DatePicker dateP = new DatePicker("Incident Date");
        dateP.setValue(LocalDate.now());

        TextField countryF = new TextField("Target Country Code (e.g. UA, US, TW)");
        countryF.setValue("UA");

        TextField sectorF = new TextField("Target Sector");
        sectorF.setValue("Energy / Power");

        TextArea ttpsF = new TextArea("Observed MITRE ATT&CK TTPs");
        ttpsF.setValue("T1486, T1562.001, T1055");
        ttpsF.setWidthFull();

        TextArea iocsF = new TextArea("Observed Forensic IOCs");
        iocsF.setValue("194.26.29.112, caddywiper.exe");
        iocsF.setWidthFull();

        TextArea notesF = new TextArea("Geopolitical Context & Notes");
        notesF.setValue("Intrusion coincides with peak GDELT conflict code 190.");
        notesF.setWidthFull();

        Button createBtn = new Button("CREATE CASE", e -> {
            caseService.createCase(
                    titleF.getValue(), analystF.getValue(), dateP.getValue(),
                    countryF.getValue(), sectorF.getValue(),
                    ttpsF.getValue(), iocsF.getValue(),
                    notesF.getValue(), notesF.getValue()
            );
            grid.setItems(caseRepository.findAll());
            dialog.close();
            Notification.show("Case created successfully!", 3000, Notification.Position.TOP_CENTER);
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createBtn.getStyle().set("background", "#00ffcc").set("color", "#000");

        Button cancelBtn = new Button("Cancel", e -> dialog.close());

        HorizontalLayout btnRow = new HorizontalLayout(createBtn, cancelBtn);

        dialog.add(dTitle, titleF, analystF, new HorizontalLayout(dateP, countryF, sectorF), ttpsF, iocsF, notesF, btnRow);
        dialog.open();
    }
}
