package org.geocyber.rest;

import org.geocyber.benchmark.BenchmarkResult;
import org.geocyber.benchmark.BenchmarkService;
import org.geocyber.domain.BenchmarkRecord;
import org.geocyber.repository.BenchmarkRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/benchmark")
public class BenchmarkRestController {

    private final BenchmarkService benchmarkService;
    private final BenchmarkRecordRepository recordRepository;

    public BenchmarkRestController(BenchmarkService benchmarkService,
                                   BenchmarkRecordRepository recordRepository) {
        this.benchmarkService = benchmarkService;
        this.recordRepository = recordRepository;
    }

    @PostMapping("/run")
    public List<BenchmarkResult> runBenchmarkSuite() {
        return benchmarkService.runComprehensiveBenchmarkSuite();
    }

    @GetMapping("/records")
    public List<BenchmarkRecord> getHistoricalRecords() {
        return recordRepository.findTop20ByOrderByRunTimestampDesc();
    }
}
