package org.geocyber.ingestion;

import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.IocType;
import org.geocyber.repository.IocRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class IocNormalizationService {

    private static final Logger log = LoggerFactory.getLogger(IocNormalizationService.class);

    private final IocRepository iocRepository;

    // Robust Regex Patterns for IOC extraction
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\[?\\.\\]?){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\b"
    );
    private static final Pattern SHA256_PATTERN = Pattern.compile("\\b[a-fA-F0-9]{64}\\b");
    private static final Pattern SHA1_PATTERN = Pattern.compile("\\b[a-fA-F0-9]{40}\\b");
    private static final Pattern MD5_PATTERN = Pattern.compile("\\b[a-fA-F0-9]{32}\\b");
    private static final Pattern CVE_PATTERN = Pattern.compile("\\bCVE-\\d{4}-\\d{4,7}\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern URL_PATTERN = Pattern.compile("h(?:xx|tt)ps?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("\\b(?:[a-zA-Z0-9](?:[a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\[?\\.\\]?)+(?:com|net|org|ru|cn|ir|kp|io|cc|xyz|info|top|biz|gov|mil|ua)\\b", Pattern.CASE_INSENSITIVE);

    public IocNormalizationService(IocRepository iocRepository) {
        this.iocRepository = iocRepository;
    }

    /**
     * Defang a suspicious indicator to prevent accidental clicks or execution.
     * e.g., 192.168.1.1 -> 192.168.1[.]1, http://evil.com -> hxxp://evil[.]com
     */
    public String defang(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return "";
        String defanged = rawValue.trim();
        defanged = defanged.replaceAll("(?i)^http://", "hxxp://");
        defanged = defanged.replaceAll("(?i)^https://", "hxxps://");
        defanged = defanged.replaceAll("\\.", "[.]");
        return defanged;
    }

    /**
     * Refang / normalize a defanged indicator back to standard form.
     */
    public String normalize(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.trim();
        normalized = normalized.replaceAll("(?i)^hxxp://", "http://");
        normalized = normalized.replaceAll("(?i)^hxxps://", "https://");
        normalized = normalized.replaceAll("\\[\\.\\]", ".");
        normalized = normalized.replaceAll("\\[dot\\]", ".");
        return normalized.toLowerCase();
    }

    /**
     * Detect IOC Type from string
     */
    public Optional<IocType> detectType(String indicator) {
        String norm = normalize(indicator);
        if (IPV4_PATTERN.matcher(norm).matches()) return Optional.of(IocType.IPV4);
        if (norm.toUpperCase().matches("^CVE-\\d{4}-\\d{4,7}$")) return Optional.of(IocType.CVE);
        if (norm.matches("^[a-f0-9]{64}$")) return Optional.of(IocType.SHA256);
        if (norm.matches("^[a-f0-9]{40}$")) return Optional.of(IocType.SHA1);
        if (norm.matches("^[a-f0-9]{32}$")) return Optional.of(IocType.MD5);
        if (norm.startsWith("http://") || norm.startsWith("https://")) return Optional.of(IocType.URL);
        if (EMAIL_PATTERN.matcher(norm).matches()) return Optional.of(IocType.EMAIL);
        if (norm.matches("^[a-z0-9.-]+\\.[a-z]{2,}$")) return Optional.of(IocType.DOMAIN);
        return Optional.empty();
    }

    /**
     * Extracts all IOCs from an arbitrary unstructured text block (e.g. threat report or incident log).
     */
    public List<IndicatorOfCompromise> extractFromText(String text, String sourceFeed, String associatedActor) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        List<IndicatorOfCompromise> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // Extract SHA256
        Matcher sha256Matcher = SHA256_PATTERN.matcher(text);
        while (sha256Matcher.find()) {
            String hash = sha256Matcher.group().toLowerCase();
            if (seen.add(hash)) {
                results.add(new IndicatorOfCompromise(IocType.SHA256, hash, hash, hash, 0.95, "HIGH", sourceFeed, associatedActor, ""));
            }
        }

        // Extract MD5
        Matcher md5Matcher = MD5_PATTERN.matcher(text);
        while (md5Matcher.find()) {
            String hash = md5Matcher.group().toLowerCase();
            if (seen.add(hash)) {
                results.add(new IndicatorOfCompromise(IocType.MD5, hash, hash, hash, 0.90, "HIGH", sourceFeed, associatedActor, ""));
            }
        }

        // Extract CVEs
        Matcher cveMatcher = CVE_PATTERN.matcher(text);
        while (cveMatcher.find()) {
            String cve = cveMatcher.group().toUpperCase();
            if (seen.add(cve)) {
                results.add(new IndicatorOfCompromise(IocType.CVE, cve, cve, cve, 0.85, "HIGH", sourceFeed, associatedActor, ""));
            }
        }

        // Extract IPv4
        Matcher ipMatcher = IPV4_PATTERN.matcher(text);
        while (ipMatcher.find()) {
            String ip = normalize(ipMatcher.group());
            if (seen.add(ip) && !ip.startsWith("127.") && !ip.startsWith("0.")) {
                results.add(new IndicatorOfCompromise(IocType.IPV4, ip, ip, defang(ip), 0.80, "MEDIUM", sourceFeed, associatedActor, ""));
            }
        }

        // Extract URLs
        Matcher urlMatcher = URL_PATTERN.matcher(text);
        while (urlMatcher.find()) {
            String rawUrl = urlMatcher.group();
            String normUrl = normalize(rawUrl);
            if (seen.add(normUrl)) {
                results.add(new IndicatorOfCompromise(IocType.URL, rawUrl, normUrl, defang(normUrl), 0.88, "HIGH", sourceFeed, associatedActor, ""));
            }
        }

        return results;
    }

    /**
     * Ingest and save an IOC idempotently
     */
    @Transactional
    public IndicatorOfCompromise saveOrUpdate(IndicatorOfCompromise ioc) {
        Optional<IndicatorOfCompromise> existing = iocRepository.findByNormalizedValue(ioc.getNormalizedValue());
        if (existing.isPresent()) {
            IndicatorOfCompromise found = existing.get();
            found.setLastSeen(LocalDateTime.now());
            if (ioc.getAssociatedActors() != null && !found.getAssociatedActors().contains(ioc.getAssociatedActors())) {
                found.setAssociatedActors(found.getAssociatedActors() + ", " + ioc.getAssociatedActors());
            }
            return iocRepository.save(found);
        } else {
            return iocRepository.save(ioc);
        }
    }
}
