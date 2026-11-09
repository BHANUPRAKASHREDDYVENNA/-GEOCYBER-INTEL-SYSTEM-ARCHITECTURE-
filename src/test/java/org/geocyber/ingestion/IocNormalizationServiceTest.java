package org.geocyber.ingestion;

import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.IocType;
import org.geocyber.repository.IocRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class IocNormalizationServiceTest {

    private IocRepository iocRepository;
    private IocNormalizationService service;

    @BeforeEach
    void setUp() {
        iocRepository = Mockito.mock(IocRepository.class);
        service = new IocNormalizationService(iocRepository);
    }

    @Test
    void testDefangIpAndUrl() {
        assertThat(service.defang("192.168.1.50")).isEqualTo("192[.]168[.]1[.]50");
        assertThat(service.defang("http://malware-drop.com/beacon")).isEqualTo("hxxp://malware-drop[.]com/beacon");
        assertThat(service.defang("https://c2.apt28.org/api")).isEqualTo("hxxps://c2[.]apt28[.]org/api");
    }

    @Test
    void testNormalize() {
        assertThat(service.normalize("192.168.1[.]50")).isEqualTo("192.168.1.50");
        assertThat(service.normalize("hxxp://evil[.]com/path")).isEqualTo("http://evil.com/path");
        assertThat(service.normalize("hxxps://sub[dot]domain[dot]com")).isEqualTo("https://sub.domain.com");
    }

    @Test
    void testDetectType() {
        assertThat(service.detectType("185.175.56.24")).contains(IocType.IPV4);
        assertThat(service.detectType("CVE-2024-21413")).contains(IocType.CVE);
        assertThat(service.detectType("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855")).contains(IocType.SHA256);
        assertThat(service.detectType("d41d8cd98f00b204e9800998ecf8427e")).contains(IocType.MD5);
        assertThat(service.detectType("http://command-control.net")).contains(IocType.URL);
    }

    @Test
    void testExtractFromText() {
        String logBlock = """
                Alert detected on workstation WS-04:
                Suspicious binary downloaded from hxxp://bad-server.org/payload.exe
                Connecting to C2 IP 194.26.29.112 on port 443.
                SHA256: e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855
                Exploited vulnerability CVE-2024-21413 via Outlook.
                """;

        List<IndicatorOfCompromise> extracted = service.extractFromText(logBlock, "EDR_LOGS", "Sandworm");

        assertThat(extracted).isNotEmpty();
        assertThat(extracted).anyMatch(i -> i.getIocType() == IocType.IPV4 && i.getNormalizedValue().equals("194.26.29.112"));
        assertThat(extracted).anyMatch(i -> i.getIocType() == IocType.CVE && i.getNormalizedValue().equalsIgnoreCase("CVE-2024-21413"));
        assertThat(extracted).anyMatch(i -> i.getIocType() == IocType.SHA256);
    }
}
