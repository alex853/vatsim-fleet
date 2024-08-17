package net.simforge.vatsimfleet.app;

import net.simforge.commons.misc.JavaTime;
import net.simforge.networkview.core.report.ReportUtils;
import net.simforge.networkview.core.report.persistence.Report;
import net.simforge.vatsimfleet.processor.Aircraft;
import net.simforge.vatsimfleet.processor.VatsimFleetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("service/v1")
@CrossOrigin
public class Controller {
    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    @GetMapping("hello-world")
    public String getHelloWorld() {
        return "Hello, World!";
    }

    @GetMapping("parked-aircraft-by-icao")
    public ResponseEntity<List<Aircraft>> getParkedAircraftByIcao(@RequestParam("icao") final String icao) {
        log.info("retrieving parked aircraft by icao - {}", icao);
        final List<Aircraft> parkedAircraft = VatsimFleetProcessor.getParkedAircraftByIcao(icao);
        return ResponseEntity.ok(parkedAircraft);
    }

    @GetMapping("status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();

        final Report lastProcessedReport = VatsimFleetProcessor.getLastProcessedReport();
        final LocalDateTime reportTimestamp = lastProcessedReport != null
                ? ReportUtils.fromTimestampJava(lastProcessedReport.getReport())
                : LocalDateTime.MIN;
        final double hours = JavaTime.hoursBetween(reportTimestamp, JavaTime.nowUtc());
        final boolean ok = hours <= 0.2;

        status.put("status", (ok ? "OK" : "FAIL"));

        status.put("lastProcessedReport", lastProcessedReport != null ? lastProcessedReport.getReport() : null);

        Map<String, Integer> memoryReport = new TreeMap<>();
        memoryReport.put("used", MemoryReport.getUsedMB());
        memoryReport.put("free", MemoryReport.getFreeMB());
        memoryReport.put("total", MemoryReport.getTotalMB());
        memoryReport.put("max", MemoryReport.getMaxMB());
        status.put("memory", memoryReport);

        return ResponseEntity.ok(status);
    }
}
