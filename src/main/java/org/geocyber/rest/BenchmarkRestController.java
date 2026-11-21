package org.geocyber.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.geocyber.benchmark.BenchmarkResult;
import org.geocyber.benchmark.BenchmarkService;
import org.geocyber.domain.BenchmarkRecord;
import org.geocyber.repository.BenchmarkRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/benchmark")
@Tag(name = "Research Benchmark & Evaluation", description = "Endpoints for reproducing 5-fold cross-validation, baseline comparisons, and ablation metrics")
public class BenchmarkRestController {

    private final BenchmarkService benchmarkService;
    private final BenchmarkRecordRepository recordRepository;

    public BenchmarkRestController(BenchmarkService benchmarkService,
                                   BenchmarkRecordRepository recordRepository) {
        this.benchmarkService = benchmarkService;
        this.recordRepository = recordRepository;
    }

    @Operation(summary = "Execute full research benchmark suite across baselines and ablation configurations")
    @PostMapping("/run")
    public List<BenchmarkResult> runBenchmarkSuite() {
        return benchmarkService.runComprehensiveBenchmarkSuite();
    }

    @Operation(summary = "Retrieve historical benchmark run records")
    @GetMapping("/records")
    public List<BenchmarkRecord> getHistoricalRecords() {
        return recordRepository.findTop20ByOrderByRunTimestampDesc();
    }
}
