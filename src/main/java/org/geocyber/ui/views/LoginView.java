package org.geocyber.ui.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | GEOCYBER INTEL")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background", "radial-gradient(circle at center, #161b22 0%, #090d13 100%)");

        H1 title = new H1("GEOCYBER INTEL");
        title.getStyle()
                .set("font-weight", "900")
                .set("letter-spacing", "3px")
                .set("color", "#00ffcc")
                .set("margin", "0 0 5px 0")
                .set("text-shadow", "0 0 20px rgba(0, 255, 204, 0.5)");

        Paragraph subtitle = new Paragraph("Geopolitical Cyber Threat Intelligence & Multimodal APT Attribution");
        subtitle.getStyle()
                .set("color", "#a0aec0")
                .set("font-size", "0.95rem")
                .set("margin-bottom", "20px");

        loginForm.setAction("login");

        Div credentialsHint = new Div();
        credentialsHint.getStyle()
                .set("background", "rgba(0, 255, 204, 0.05)")
                .set("border", "1px solid rgba(0, 255, 204, 0.2)")
                .set("border-radius", "8px")
                .set("padding", "12px 18px")
                .set("font-size", "0.85rem")
                .set("color", "#cbd5e0")
                .set("margin-top", "15px")
                .set("max-width", "360px");

        credentialsHint.add(
                new Paragraph("Research Demonstration Accounts:"),
                new Paragraph("● Analyst: analyst / analyst123"),
                new Paragraph("● Administrator: admin / admin123"),
                new Paragraph("● Researcher: researcher / researcher123")
        );

        add(title, subtitle, loginForm, credentialsHint);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
