package org.geocyber.ui.views;

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.geocyber.domain.CyberCampaign;
import org.geocyber.repository.CyberCampaignRepository;
import org.geocyber.ui.MainLayout;

import java.util.List;

@Route(value = "timeline", layout = MainLayout.class)
@PageTitle("Campaign Timeline | GEOCYBER INTEL")
@RolesAllowed({"ROLE_ANALYST", "ROLE_ADMIN", "ROLE_RESEARCHER"})
public class CampaignTimelineView extends VerticalLayout {

    public CampaignTimelineView(CyberCampaignRepository campaignRepository) {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 title = new H2("CYBER CAMPAIGN & GEOPOLITICAL EVENT CORRELATION TIMELINE");
        title.getStyle().set("color", "#00ffcc").set("margin", "0");
        Paragraph sub = new Paragraph("Chronological temporal tracking aligning cyber intrusion bursts with peak bilateral diplomatic and kinetic tension events.");
        sub.getStyle().set("color", "#718096").set("margin", "0 0 15px 0");

        Div timelineContainer = new Div();
        timelineContainer.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "16px")
                .set("max-width", "900px")
                .set("overflow-y", "auto");

        List<CyberCampaign> campaigns = campaignRepository.findAll();
        for (CyberCampaign c : campaigns) {
            Div card = new Div();
            card.getStyle()
                    .set("background", "#161b22")
                    .set("border", "1px solid rgba(255, 255, 255, 0.08)")
                    .set("border-left", "4px solid #00ffcc")
                    .set("border-radius", "6px")
                    .set("padding", "16px 20px");

            H3 cName = new H3(c.getName() + " — Attributed to " + c.getAttributedActorName());
            cName.getStyle().set("color", "#00ffcc").set("margin", "0 0 4px 0").set("font-size", "1.1rem");

            Span dates = new Span("Active Window: " + c.getStartDate() + " to " + (c.getEndDate() != null ? c.getEndDate() : "Present"));
            dates.getStyle().set("color", "#718096").set("font-size", "0.75rem").set("display", "block").set("margin-bottom", "8px");

            Paragraph obj = new Paragraph("Strategic Geopolitical Objective: " + c.getGeopoliticalObjective());
            obj.getStyle().set("color", "#cbd5e0").set("font-size", "0.85rem").set("margin", "0 0 8px 0");

            Div pills = new Div();
            pills.getStyle().set("display", "flex").set("gap", "8px").set("flex-wrap", "wrap");

            pills.add(
                    createPill("Target Nations: " + c.getTargetNations(), "#0099ff"),
                    createPill("Target Sectors: " + c.getTargetSectors(), "#ff9900"),
                    createPill("GDELT Correlation: " + String.format("%.2f", c.getGdeltCorrelationScore()), "#39ff14")
            );

            card.add(cName, dates, obj, pills);
            timelineContainer.add(card);
        }

        add(title, sub, timelineContainer);
    }

    private Span createPill(String text, String color) {
        Span pill = new Span(text);
        pill.getStyle()
                .set("background", "rgba(255, 255, 255, 0.05)")
                .set("border", "1px solid " + color)
                .set("color", color)
                .set("border-radius", "12px")
                .set("padding", "2px 10px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");
        return pill;
    }
}
