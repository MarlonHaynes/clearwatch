package com.clearwatch.controller;

import com.clearwatch.domain.LogLevel;
import com.clearwatch.dto.LogEntryDto;
import com.clearwatch.dto.Mappers;
import com.clearwatch.repository.LogEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final LogEntryRepository logEntryRepository;

    public LogController(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @GetMapping
    public List<LogEntryDto> search(@RequestParam(required = false) Long serviceId,
                                     @RequestParam(required = false) LogLevel level,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "1h") String window,
                                     @RequestParam(defaultValue = "200") int limit) {
        Instant now = Instant.now();
        Instant from = now.minus(parseWindow(window));
        var page = logEntryRepository.search(serviceId, level, from, now, blankToNull(q),
                PageRequest.of(0, Math.min(limit, 1000), Sort.by(Sort.Direction.DESC, "timestamp")));
        return page.getContent().stream().map(Mappers::toDto).toList();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Duration parseWindow(String window) {
        return switch (window) {
            case "15m" -> Duration.ofMinutes(15);
            case "1h" -> Duration.ofHours(1);
            case "24h" -> Duration.ofHours(24);
            default -> Duration.ofHours(1);
        };
    }
}
