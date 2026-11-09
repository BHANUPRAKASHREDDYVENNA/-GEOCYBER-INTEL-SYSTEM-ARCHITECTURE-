package org.geocyber;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Theme(value = "geocyber", variant = Lumo.DARK)
public class GeoCyberIntelApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(GeoCyberIntelApplication.class, args);
    }
}
