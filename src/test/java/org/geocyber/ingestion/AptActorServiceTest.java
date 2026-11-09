package org.geocyber.ingestion;

import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AptActorServiceTest {

    private AptActorRepository actorRepository;
    private AptActorService service;

    @BeforeEach
    void setUp() {
        actorRepository = Mockito.mock(AptActorRepository.class);
        AptActor apt28 = new AptActor("APT28", "RU", "Russia", "Fancy Bear, STRONTIUM, Sednit, Forest Blizzard",
                "Espionage", "Defense", "US, UA", "T1566.001", "X-Agent", 0.95, "2007", "2026", true, "GRU", "Mandiant");
        AptActor apt29 = new AptActor("APT29", "RU", "Russia", "Cozy Bear, NOBELIUM, Midnight Blizzard",
                "Espionage", "Government", "US", "T1190", "SUNBURST", 0.96, "2008", "2026", true, "SVR", "CISA");
        AptActor volt = new AptActor("Volt Typhoon", "CN", "China", "Bronze Silhouette, Vanguard Panda",
                "Sabotage", "Critical Infra", "US, GU", "T1078", "LOTL", 0.94, "2021", "2026", true, "PLA", "NSA");

        when(actorRepository.findAll()).thenReturn(List.of(apt28, apt29, volt));

        service = new AptActorService(actorRepository);
        service.rebuildAliasIndex();
    }

    @Test
    void testResolveKnownAliases() {
        assertThat(service.resolveAlias("STRONTIUM")).isEqualTo("APT28");
        assertThat(service.resolveAlias("Fancy Bear")).isEqualTo("APT28");
        assertThat(service.resolveAlias("Midnight Blizzard")).isEqualTo("APT29");
        assertThat(service.resolveAlias("NOBELIUM")).isEqualTo("APT29");
        assertThat(service.resolveAlias("Bronze Silhouette")).isEqualTo("Volt Typhoon");
    }

    @Test
    void testResolveUnknownAliasReturnsRaw() {
        assertThat(service.resolveAlias("UnknownActorX")).isEqualTo("UnknownActorX");
        assertThat(service.resolveAlias("")).isEqualTo("UNKNOWN");
    }
}
