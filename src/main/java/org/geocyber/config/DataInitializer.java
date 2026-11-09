package org.geocyber.config;

import org.geocyber.ai.model.MultimodalCrossAttentionTransformer;
import org.geocyber.benchmark.BenchmarkService;
import org.geocyber.domain.*;
import org.geocyber.ingestion.AptActorService;
import org.geocyber.ingestion.AptNotesIngestionService;
import org.geocyber.ingestion.IocNormalizationService;
import org.geocyber.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AptActorRepository actorRepository;
    private final MitreTechniqueRepository mitreRepository;
    private final GdeltEventRepository gdeltRepository;
    private final ThreatReportRepository reportRepository;
    private final IocRepository iocRepository;
    private final CyberCampaignRepository campaignRepository;
    private final AttributionCaseRepository caseRepository;
    private final PasswordEncoder passwordEncoder;
    private final AptActorService actorService;
    private final BenchmarkService benchmarkService;

    public DataInitializer(UserRepository userRepository,
                           AptActorRepository actorRepository,
                           MitreTechniqueRepository mitreRepository,
                           GdeltEventRepository gdeltRepository,
                           ThreatReportRepository reportRepository,
                           IocRepository iocRepository,
                           CyberCampaignRepository campaignRepository,
                           AttributionCaseRepository caseRepository,
                           PasswordEncoder passwordEncoder,
                           AptActorService actorService,
                           BenchmarkService benchmarkService) {
        this.userRepository = userRepository;
        this.actorRepository = actorRepository;
        this.mitreRepository = mitreRepository;
        this.gdeltRepository = gdeltRepository;
        this.reportRepository = reportRepository;
        this.iocRepository = iocRepository;
        this.campaignRepository = campaignRepository;
        this.caseRepository = caseRepository;
        this.passwordEncoder = passwordEncoder;
        this.actorService = actorService;
        this.benchmarkService = benchmarkService;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing GEOCYBER INTEL Security, Intelligence, and Benchmark data...");

        initUsers();
        initMitreTechniques();
        initAptActors();
        initGdeltEvents();
        initThreatReports();
        initCyberCampaigns();
        initAttributionCases();
        initIocs();

        // Warm up and pre-seed benchmark records
        benchmarkService.runComprehensiveBenchmarkSuite();

        log.info("GEOCYBER INTEL Data Initialization Complete.");
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            userRepository.save(new User("analyst", passwordEncoder.encode("analyst123"),
                    "Senior CTI Analyst", "analyst@geocyber.internal", Role.ROLE_ANALYST, true));
            userRepository.save(new User("admin", passwordEncoder.encode("admin123"),
                    "System Administrator", "admin@geocyber.internal", Role.ROLE_ADMIN, true));
            userRepository.save(new User("researcher", passwordEncoder.encode("researcher123"),
                    "Lead AI/CTI Researcher", "researcher@geocyber.internal", Role.ROLE_RESEARCHER, true));
            log.info("Initialized 3 default system user accounts (analyst, admin, researcher).");
        }
    }

    private void initMitreTechniques() {
        if (mitreRepository.count() == 0) {
            List<MitreAttackTechnique> list = List.of(
                    new MitreAttackTechnique("T1566.001", "Spearphishing Attachment", "Initial Access", "Adversaries send spearphishing emails with malicious attachments.", "Windows, Linux, macOS", "Monitor email attachments and sandbox execution."),
                    new MitreAttackTechnique("T1566.002", "Spearphishing Link", "Initial Access", "Adversaries send emails with malicious URLs to trick users.", "Windows, Linux, macOS", "Web proxy filtering and user awareness."),
                    new MitreAttackTechnique("T1190", "Exploit Public-Facing Application", "Initial Access", "Adversaries exploit vulnerabilities in internet-facing services.", "Windows, Linux", "Patch management and WAF inspects."),
                    new MitreAttackTechnique("T1078", "Valid Accounts", "Initial Access", "Adversaries obtain credentials to gain access via legitimate channels.", "Windows, Linux, Cloud", "MFA enforcement and abnormal login velocity alerts."),
                    new MitreAttackTechnique("T1059.001", "PowerShell", "Execution", "Adversaries abuse PowerShell commands and scripts.", "Windows", "Enable Script Block Logging and Constrained Language Mode."),
                    new MitreAttackTechnique("T1059.003", "Windows Command Shell", "Execution", "Adversaries execute cmd.exe commands.", "Windows", "Process creation event auditing (Event ID 4688)."),
                    new MitreAttackTechnique("T1053.005", "Scheduled Task", "Persistence", "Adversaries create scheduled tasks to maintain persistence.", "Windows", "Monitor Task Scheduler creation events (Event ID 4698)."),
                    new MitreAttackTechnique("T1055", "Process Injection", "Privilege Escalation", "Adversaries inject code into processes to evade defenses.", "Windows, Linux", "Sysmon Event ID 8 (CreateRemoteThread)."),
                    new MitreAttackTechnique("T1027", "Obfuscated Files or Information", "Defense Evasion", "Adversaries encrypt or encode payloads to conceal indicators.", "Windows, Linux", "Entropy analysis and heuristic anti-malware."),
                    new MitreAttackTechnique("T1562.001", "Disable or Modify Tools", "Defense Evasion", "Adversaries tamper with security software or event loggers.", "Windows", "Audit changes to EDR services and registry tamper protection."),
                    new MitreAttackTechnique("T1003.001", "LSASS Memory", "Credential Access", "Adversaries dump credentials from the LSASS process (Mimikatz).", "Windows", "Run LSASS as Protected Process Light (PPL)."),
                    new MitreAttackTechnique("T1082", "System Information Discovery", "Discovery", "Adversaries query OS version, architecture, and patch levels.", "Windows, Linux", "Command line auditing on systeminfo / uname."),
                    new MitreAttackTechnique("T1021.001", "Remote Desktop Protocol", "Lateral Movement", "Adversaries use RDP to move laterally within victim networks.", "Windows", "Enforce Network Level Authentication and jump hosts."),
                    new MitreAttackTechnique("T1071.001", "Web Protocols", "Command and Control", "Adversaries communicate with C2 servers using HTTP or HTTPS.", "Network", "SSL/TLS inspection and C2 beaconing analysis."),
                    new MitreAttackTechnique("T1486", "Data Encrypted for Impact", "Impact", "Adversaries encrypt files on target systems to disrupt operations or demand ransom.", "Windows, Linux", "File modification rate monitoring and offline backups.")
            );
            mitreRepository.saveAll(list);
            log.info("Initialized {} core MITRE ATT&CK Enterprise techniques.", list.size());
        }
    }

    private void initAptActors() {
        if (actorRepository.count() == 0) {
            List<AptActor> list = List.of(
                    new AptActor("APT28", "RU", "Russian Federation", "Fancy Bear, Sofacy, STRONTIUM, Sednit, Forest Blizzard, Pawn Storm",
                            "Espionage", "Defense, Government, NATO, Media, Think Tanks", "US, UA, DE, UK, PL, FR",
                            "T1566.001, T1059.001, T1027, T1071.001, T1003.001", "X-Agent, Zebrocy, CHOPSTICK, Drovorub", 0.95,
                            "2007", "2026", true, "Russian Main Intelligence Directorate (GRU) Unit 26165 military cyber offensive division.", "Mandiant, CrowdStrike, CISA"),
                    new AptActor("APT29", "RU", "Russian Federation", "Cozy Bear, The Dukes, NOBELIUM, Midnight Blizzard, StellarParticle",
                            "Espionage", "Government, Foreign Policy, Think Tanks, Technology, Cloud Providers", "US, UK, DE, NATO, UA",
                            "T1190, T1078, T1059.001, T1071.001, T1082", "SUNBURST, CosmicDuke, WellMess, GoldFinder", 0.96,
                            "2008", "2026", true, "Russian Foreign Intelligence Service (SVR) specializing in stealthy long-term collection and cloud compromise.", "CISA, Microsoft CTI, Mandiant"),
                    new AptActor("Sandworm", "RU", "Russian Federation", "Voodoo Bear, BlackEnergy Group, TeleBots, Seashell Blizzard, Iron Viking",
                            "Sabotage", "Energy, Critical Infrastructure, Water, Railway, Government", "UA, US, NATO, FR",
                            "T1486, T1562.001, T1055, T1059.003, T1071.001", "BlackEnergy, Industroyer, Industroyer2, HermeticWiper, CaddyWiper", 0.98,
                            "2009", "2026", true, "Russian GRU Unit 74455 responsible for disruptive cyber warfare, power grid blackouts, and destructive wipers.", "Mandiant, ESET, CISA"),
                    new AptActor("Lazarus Group", "KP", "Democratic People's Republic of Korea", "Hidden Cobra, Zinc, Diamond Sleet, Labyrinth Chollima, Whois Team",
                            "Financial / Theft", "Financial, Cryptocurrency, Defense, Aerospace, Media", "US, KR, JP, SG, VN, IL",
                            "T1566.002, T1059.001, T1486, T1027, T1071.001", "WannaCry, Manuscrypt, FALLCHILL, BLINDINGCAN, Dtrack", 0.94,
                            "2009", "2026", true, "Reconnaissance General Bureau (RGB) North Korean state-sponsored threat group conducting financial theft and espionage.", "US DOJ, CISA, Kaspersky"),
                    new AptActor("Volt Typhoon", "CN", "People's Republic of China", "Bronze Silhouette, Vanguard Panda, Voltzite, Insidious Taurus",
                            "Sabotage / Pre-positioning", "Critical Infrastructure, Water, Ports, Telecom, Energy", "US, GU, TW, JP",
                            "T1078, T1190, T1059.003, T1082, T1071.001", "Living-off-the-land utilities, FastReverseProxy, KV-botnet", 0.95,
                            "2021", "2026", true, "Chinese state-sponsored espionage group focused on covert pre-positioning across Western critical infrastructure.", "CISA, NSA, Microsoft, Mandiant"),
                    new AptActor("APT41", "CN", "People's Republic of China", "Barium, Wicked Panda, Brass Typhoon, Winnti Group, Red Kelpie",
                            "Espionage / Financial", "Telecom, Healthcare, High-Tech, Gaming, Aviation", "US, TW, IN, JP, EU",
                            "T1190, T1055, T1027, T1071.001, T1059.001", "ShadowPad, Crosswalk, PlugX, Cobalt Strike", 0.92,
                            "2012", "2026", true, "Dual-mission Chinese threat group carrying out state-directed espionage alongside financially motivated intrusions.", "FireEye/Mandiant, DOJ Indictment"),
                    new AptActor("Charming Kitten", "IR", "Islamic Republic of Iran", "Phosphorus, Mint Sandstorm, APT35, Cobalt Illusion, Yellow Garuda",
                            "Espionage", "Think Tanks, Journalists, Human Rights Activists, Defense, Nuclear", "US, IL, UK, SA, DE",
                            "T1566.002, T1078, T1059.005, T1071.001, T1082", "HYPERSCRAPE, BellaCiao, CharmPower, DownPaper", 0.91,
                            "2013", "2026", true, "Iranian Islamic Revolutionary Guard Corps (IRGC) intelligence-gathering group conducting credential harvesting.", "Microsoft, Mandiant, Certfa"),
                    new AptActor("Turla", "RU", "Russian Federation", "Waterbug, Venomous Bear, Krypton, Snake, Group 88",
                            "Espionage", "Government, Diplomacy, Military, Embassies, Research Institutes", "US, EU, UA, Middle East",
                            "T1566.001, T1055, T1027, T1071.004, T1041", "Snake rootkit, ComRAT, Carbon, Gazer", 0.93,
                            "1996", "2026", true, "Russian Federal Security Service (FSB) Center 16 elite cyber espionage group targeting government communications.", "CISA, NCSC, Mandiant, ESET"),
                    new AptActor("MuddyWater", "IR", "Islamic Republic of Iran", "Mercury, Mango Sandstorm, Seedworm, Static Kitten, TEMP.Zagros",
                            "Espionage", "Government, Telecom, Energy, Oil & Gas", "IL, SA, TR, AE, IQ, US",
                            "T1566.001, T1059.001, T1059.005, T1071.001, T1027", "POWERSTATS, Small Sieve, Mori, Ligolo", 0.89,
                            "2017", "2026", true, "Iranian Ministry of Intelligence and Security (MOIS) cyber actor targeting regional Middle Eastern entities.", "CISA, US Cyber Command"),
                    new AptActor("Equation Group", "US", "United States", "TAO, EquationDrug, Longhorn",
                            "Espionage / Sabotage", "Nuclear, Telecom, Military, Cryptography, Financial", "IR, RU, CN, SY, AF",
                            "T1190, T1055, T1027, T1547.001, T1041", "Fanny worm, DoublePulsar, EternalBlue, GrayFish", 0.97,
                            "2001", "2026", true, "Highly sophisticated cyber operation group affiliated with US signals intelligence and precision exploitation.", "Kaspersky Lab, Shadow Brokers")
            );
            actorRepository.saveAll(list);
            actorService.rebuildAliasIndex();
            log.info("Initialized {} canonical APT actors and alias mappings.", list.size());
        }
    }

    private void initGdeltEvents() {
        if (gdeltRepository.count() == 0) {
            List<GdeltEvent> list = List.of(
                    new GdeltEvent("GDELT-2026-UA-01", LocalDate.now().minusDays(5), "RUS", "RU", "UKR", "UA",
                            "190", "Use conventional military force and infrastructure strikes", -9.5, -7.8, 1420, "UA", 50.45, 30.52, "https://gdeltproject.org/events/2026-UA-01"),
                    new GdeltEvent("GDELT-2026-TW-02", LocalDate.now().minusDays(3), "CHN", "CN", "TWN", "TW",
                            "180", "Military naval and aerospace blockade exercises in Taiwan Strait", -8.2, -6.4, 980, "TW", 25.03, 121.56, "https://gdeltproject.org/events/2026-TW-02"),
                    new GdeltEvent("GDELT-2026-IL-03", LocalDate.now().minusDays(2), "IRN", "IR", "ISR", "IL",
                            "193", "Retaliatory missile barrage and proxy conflict escalation", -9.8, -8.9, 2150, "IL", 31.76, 35.21, "https://gdeltproject.org/events/2026-IL-03"),
                    new GdeltEvent("GDELT-2026-KR-04", LocalDate.now().minusDays(8), "PRK", "KP", "KOR", "KR",
                            "112", "Accuse of espionage, airspace violation, and nuclear posturing", -7.5, -5.2, 640, "KR", 37.56, 126.97, "https://gdeltproject.org/events/2026-KR-04"),
                    new GdeltEvent("GDELT-2026-US-05", LocalDate.now().minusDays(1), "RUS", "RU", "USA", "US",
                            "160", "Expulsion of senior diplomats and imposition of secondary sanctions", -6.5, -4.5, 890, "US", 38.90, -77.03, "https://gdeltproject.org/events/2026-US-05")
            );
            gdeltRepository.saveAll(list);
            log.info("Initialized {} GDELT geopolitical events.", list.size());
        }
    }

    private void initThreatReports() {
        if (reportRepository.count() == 0) {
            List<ThreatReport> list = List.of(
                    new ThreatReport("Operation Ghostwriter: Disinformation and Credential Spearphishing Campaign", "Mandiant",
                            LocalDate.now().minusDays(12), "https://mandiant.com/reports/ghostwriter", "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                            "Comprehensive analysis of Russian military intelligence cyber campaign targeting NATO Eastern Flank.",
                            "Observed APT28 leveraging spearphishing emails with malicious macro attachments (T1566.001) and PowerShell payloads to compromise governmental exchange servers.",
                            "APT28", "X-Agent, Zebrocy", "T1566.001, T1059.001, T1027"),
                    new ThreatReport("Volt Typhoon: Stealthy Living-Off-the-Land Pre-Positioning in Critical Infrastructure", "CISA",
                            LocalDate.now().minusDays(18), "https://cisa.gov/news-events/cybersecurity-advisories/aa24-038a", "4a5a54b39b3be282126806a6c6a7ec0da83cf040c57176a9f4e24efd26786e24",
                            "Joint cybersecurity advisory on People's Republic of China state-sponsored cyber operations targeting US critical infrastructure.",
                            "Volt Typhoon utilized compromised SOHO routers and valid accounts (T1078) to pre-position for disruptive sabotage within water and transportation sectors.",
                            "Volt Typhoon", "KV-botnet, FastReverseProxy", "T1078, T1190, T1059.003"),
                    new ThreatReport("Industroyer2: Targeted High-Voltage Substation Disruption Attempt", "ESET",
                            LocalDate.now().minusDays(25), "https://welivesecurity.com/2022/04/12/industroyer2-industroyer-reloaded", "d4735e3a265e16eee03f59718b9b5d03019c07d8b6c51f90da3a666eec13ab35",
                            "Sandworm targeted Ukrainian regional electricity distribution grid using specialized ICS/SCADA malware.",
                            "The attack employed IEC-104 protocol telecontrol commands alongside CaddyWiper to wipe SCADA operator workstations.",
                            "Sandworm", "Industroyer2, CaddyWiper", "T1486, T1562.001, T1055")
            );
            reportRepository.saveAll(list);
            log.info("Initialized {} CTI threat reports.", list.size());
        }
    }

    private void initCyberCampaigns() {
        if (campaignRepository.count() == 0) {
            List<CyberCampaign> list = List.of(
                    new CyberCampaign("Operation Ghostwriter", "APT28", LocalDate.now().minusMonths(6), LocalDate.now().minusMonths(1),
                            "UA, PL, LT, LV", "Government, Defense, Media", "Undermine NATO troop deployments in Eastern Europe via credential theft",
                            "T1566.001, T1059.001, T1027", "X-Agent, Zebrocy", "185.175.56.24, update-defense[.]org", 0.88),
                    new CyberCampaign("SolarWinds Supply Chain", "APT29", LocalDate.now().minusYears(2), LocalDate.now().minusMonths(18),
                            "US, UK, NATO", "Federal Government, Tech, Defense", "Strategic long-term intelligence collection on foreign policy leadership",
                            "T1190, T1078, T1071.001", "SUNBURST, TEARDROP", "13.59.205.66, avsvmcloud[.]com", 0.92),
                    new CyberCampaign("Guam Critical Infrastructure Infiltration", "Volt Typhoon", LocalDate.now().minusMonths(4), LocalDate.now(),
                            "US, GU, JP", "Ports, Energy, Water, Telecom", "Strategic pre-positioning for regional conflict disruption in Western Pacific",
                            "T1078, T1190, T1059.003", "Living-off-the-land", "45.154.255.89, cisco-telemetry[.]net", 0.94),
                    new CyberCampaign("HermeticWiper Substation Assault", "Sandworm", LocalDate.now().minusMonths(3), LocalDate.now().minusMonths(1),
                            "UA", "Energy, Financial, Government", "Paralyze Ukrainian public services and critical infrastructure during military assault",
                            "T1486, T1562.001, T1055", "HermeticWiper, HermeticWizard", "194.26.29.112, windows-update-auth[.]com", 0.96)
            );
            campaignRepository.saveAll(list);
            log.info("Initialized {} cyber campaigns.", list.size());
        }
    }

    private void initAttributionCases() {
        if (caseRepository.count() == 0) {
            List<AttributionCase> list = List.of(
                    new AttributionCase("CASE-2026-0001", "Critical Power Substation SCADA Disruption", "Alex Mercer (Senior CTI)",
                            "ATTRIBUTED", LocalDate.now().minusDays(4), "UA", "Energy / Power Grid",
                            "T1486, T1562.001, T1055, T1059.003", "194.26.29.112, caddywiper.exe, CVE-2024-21413",
                            "Co-occurred with escalated missile strikes on thermal power plants and GDELT tension index 0.95.",
                            "Sandworm", 0.942, false, "Wiper artifacts show code reuse with Industroyer2 telecontrol binary.", ""),
                    new AttributionCase("CASE-2026-0002", "Deep Pacific Port Terminal Telemetry Probe", "Elena Rostova (Analyst)",
                            "ATTRIBUTED", LocalDate.now().minusDays(2), "GU", "Maritime Logistics / Port Infrastructure",
                            "T1078, T1190, T1059.003, T1082", "45.154.255.89, fortinet-vpn-bypass",
                            "Followed Chinese naval blockades in Philippine Sea and intense diplomatic disputes.",
                            "Volt Typhoon", 0.915, false, "Heavy reliance on living-off-the-land Windows binaries and compromised FortiOS gateway.", ""),
                    new AttributionCase("CASE-2026-0003", "Foreign Ministry Cloud Tenant Identity Exfiltration", "Marcus Vance (Analyst)",
                            "ATTRIBUTED", LocalDate.now().minusDays(6), "DE", "Government / Foreign Affairs",
                            "T1190, T1078, T1059.001, T1071.001", "13.59.205.66, ms-graph-sync[.]org",
                            "European summit on security guarantees and international sanctions coordination.",
                            "APT29", 0.887, false, "OAuth application abuse and token theft targeting diplomatic email delegations.", "")
            );
            caseRepository.saveAll(list);
            log.info("Initialized {} active CTI investigation cases.", list.size());
        }
    }

    private void initIocs() {
        if (iocRepository.count() == 0) {
            List<IndicatorOfCompromise> list = List.of(
                    new IndicatorOfCompromise(IocType.IPV4, "185.175.56.24", "185.175.56.24", "185.175.56[.]24", 0.95, "HIGH", "Mandiant", "APT28", "Operation Ghostwriter"),
                    new IndicatorOfCompromise(IocType.IPV4, "45.154.255.89", "45.154.255.89", "45.154.255[.]89", 0.92, "HIGH", "CISA Advisory", "Volt Typhoon", "Guam Critical Infrastructure"),
                    new IndicatorOfCompromise(IocType.IPV4, "194.26.29.112", "194.26.29.112", "194.26.29[.]112", 0.98, "HIGH", "ESET Research", "Sandworm", "HermeticWiper Substation Assault"),
                    new IndicatorOfCompromise(IocType.DOMAIN, "avsvmcloud.com", "avsvmcloud.com", "avsvmcloud[.]com", 0.99, "HIGH", "CISA", "APT29", "SolarWinds Supply Chain"),
                    new IndicatorOfCompromise(IocType.DOMAIN, "update-defense.org", "update-defense.org", "update-defense[.]org", 0.90, "HIGH", "CrowdStrike", "APT28", "Operation Ghostwriter"),
                    new IndicatorOfCompromise(IocType.SHA256, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", 0.96, "HIGH", "Mandiant", "APT28", "Operation Ghostwriter"),
                    new IndicatorOfCompromise(IocType.CVE, "CVE-2024-21413", "cve-2024-21413", "CVE-2024-21413", 0.88, "HIGH", "NVD", "APT28", "Microsoft Outlook Moniker RCE"),
                    new IndicatorOfCompromise(IocType.CVE, "CVE-2023-4966", "cve-2023-4966", "CVE-2023-4966", 0.95, "HIGH", "CISA KEV", "Volt Typhoon", "Citrix Bleed Memory Disclosure")
            );
            iocRepository.saveAll(list);
            log.info("Initialized {} indicators of compromise (IOCs).", list.size());
        }
    }
}
