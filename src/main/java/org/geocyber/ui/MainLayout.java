package org.geocyber.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.Lumo;
import org.geocyber.ui.views.*;
import org.springframework.security.core.userdetails.UserDetails;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        // Enforce dark cybersecurity theme across entire UI
        UI.getCurrent().getElement().getThemeList().add(Lumo.DARK);

        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        Span title = new Span("GEOCYBER INTEL");
        title.getStyle()
                .set("font-weight", "900")
                .set("font-size", "1.2rem")
                .set("letter-spacing", "2px")
                .set("color", "#00ffcc")
                .set("text-shadow", "0 0 10px rgba(0, 255, 204, 0.4)");

        Span badge = new Span("RESEARCH EDITION");
        badge.getElement().getThemeList().add("badge success small");
        badge.getStyle().set("margin-left", "10px");

        Span status = new Span("● ENGINE ONLINE");
        status.getStyle()
                .set("color", "#39ff14")
                .set("font-size", "0.75rem")
                .set("font-weight", "bold")
                .set("margin-left", "auto")
                .set("margin-right", "20px");

        String currentUsername = authContext.getAuthenticatedUser(UserDetails.class)
                .map(UserDetails::getUsername)
                .orElse("Analyst");

        Span userSpan = new Span("OPERATOR: " + currentUsername.toUpperCase());
        userSpan.getStyle()
                .set("color", "#a0aec0")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("margin-right", "15px");

        Button logoutBtn = new Button("LOGOUT", new Icon(VaadinIcon.SIGN_OUT), e -> authContext.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        logoutBtn.getStyle().set("color", "#ff5555");

        HorizontalLayout header = new HorizontalLayout(toggle, title, badge, status, userSpan, logoutBtn);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        header.getStyle()
                .set("border-bottom", "1px solid rgba(0, 255, 204, 0.15)")
                .set("background", "#0d1117");

        addToNavbar(true, header);
    }

    private void addDrawerContent() {
        H2 appName = new H2("GEOCYBER");
        appName.getStyle()
                .set("font-size", "1.1rem")
                .set("margin", "0")
                .set("color", "#00ffcc")
                .set("font-weight", "800");

        Paragraph sub = new Paragraph("APT Attribution Platform");
        sub.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#718096")
                .set("margin", "0 0 10px 0");

        Header header = new Header(appName, sub);
        header.getStyle().set("padding", "16px 16px 0 16px");

        SideNav nav = new SideNav();

        SideNavItem dashboard = new SideNavItem("Threat Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create());
        SideNavItem feed = new SideNavItem("Live Intel Feed", ThreatFeedView.class, VaadinIcon.RECORDS.create());
        SideNavItem actors = new SideNavItem("APT Actor Profiles", AptProfilesView.class, VaadinIcon.USER_CARD.create());
        SideNavItem timeline = new SideNavItem("Campaign Timeline", CampaignTimelineView.class, VaadinIcon.CALENDAR_CLOCK.create());
        SideNavItem mitre = new SideNavItem("MITRE ATT&CK", MitreMatrixView.class, VaadinIcon.GRID_SMALL.create());
        SideNavItem iocs = new SideNavItem("IOC Investigation", IocInvestigationView.class, VaadinIcon.SEARCH.create());
        SideNavItem graph = new SideNavItem("Threat Graph", ThreatGraphView.class, VaadinIcon.CLUSTER.create());
        SideNavItem aiLab = new SideNavItem("AI Attribution Lab", AttributionLabView.class, VaadinIcon.FLASK.create());
        SideNavItem cases = new SideNavItem("Investigation Cases", CaseManagementView.class, VaadinIcon.BRIEFCASE.create());
        SideNavItem benchmark = new SideNavItem("Benchmark Analytics", BenchmarkAnalyticsView.class, VaadinIcon.LINE_CHART.create());

        nav.addItem(dashboard, feed, actors, timeline, mitre, iocs, graph, aiLab, cases, benchmark);

        addToDrawer(new VerticalLayout(header, nav));
    }
}
