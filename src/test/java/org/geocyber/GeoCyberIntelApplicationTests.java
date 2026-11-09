package org.geocyber;

import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.geocyber.repository.IocRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GeoCyberIntelApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AptActorRepository actorRepository;

    @Autowired
    private IocRepository iocRepository;

    @Test
    void contextLoads() {
        assertThat(actorRepository.count()).isGreaterThanOrEqualTo(10);
        assertThat(iocRepository.count()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void testGetActorsApi() throws Exception {
        mockMvc.perform(get("/api/intelligence/actors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.name == 'APT28')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Volt Typhoon')]").exists());
    }

    @Test
    void testResolveAliasApi() throws Exception {
        mockMvc.perform(get("/api/intelligence/actors/resolve?alias=STRONTIUM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalActor").value("APT28"));
    }

    @Test
    void testDefangIocApi() throws Exception {
        mockMvc.perform(post("/api/intelligence/iocs/defang")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"indicator\": \"192.168.1.100\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.defanged").value("192[.]168[.]1[.]100"));
    }
}
