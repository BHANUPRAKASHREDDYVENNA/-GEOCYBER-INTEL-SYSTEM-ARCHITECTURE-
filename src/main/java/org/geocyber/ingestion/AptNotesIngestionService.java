package org.geocyber.ingestion;

import org.geocyber.domain.IndicatorOfCompromise;
import org.geocyber.domain.ThreatReport;
import org.geocyber.repository.ThreatReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AptNotesIngestionService {

    private static final Logger log = LoggerFactory.getLogger(AptNotesIngestionService.class);

    private final ThreatReportRepository reportRepository;
    private final IocNormalizationService iocService;
    private final AptActorService actorService;

    public AptNotesIngestionService(ThreatReportRepository reportRepository,
                                    IocNormalizationService iocService,
                                    AptActorService actorService) {
        this.reportRepository = reportRepository;
        this.iocService = iocService;
        this.actorService = actorService;
    }

    public List<ThreatReport> getRecentReports() {
        return reportRepository.findTop20ByOrderByPublicationDateDesc();
    }

    @Transactional
    public ThreatReport ingestReport(ThreatReport report) {
        log.info("Ingesting Threat Report: {}", report.getTitle());

        // Resolve actor canonical names if present
        if (report.getExtractedActors() != null && !report.getExtractedActors().isBlank()) {
            String canonical = actorService.resolveAlias(report.getExtractedActors());
            report.setExtractedActors(canonical);
        }

        ThreatReport savedReport = reportRepository.save(report);

        // Automatically extract IOCs from the text content and save them
        if (report.getFullContent() != null && !report.getFullContent().isBlank()) {
            List<IndicatorOfCompromise> iocs = iocService.extractFromText(
                    report.getFullContent(),
                    report.getPublisher(),
                    report.getExtractedActors()
            );
            for (IndicatorOfCompromise ioc : iocs) {
                iocService.saveOrUpdate(ioc);
            }
            log.info("Extracted and normalized {} IOCs from report {}", iocs.size(), savedReport.getTitle());
        }

        return savedReport;
    }
}
