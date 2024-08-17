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
import java.util.List;

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
        log.info("retrieving parked aircraft by icao");
        final List<Aircraft> parkedAircraft = VatsimFleetProcessor.getParkedAircraftByIcao(icao);
        return ResponseEntity.ok(parkedAircraft);
    }

    @GetMapping("status")
    public ResponseEntity<String> getStatus() {
        final Report lastProcessedReport = VatsimFleetProcessor.getLastProcessedReport();
        if (lastProcessedReport == null) {
            return ResponseEntity.ok("FAIL-NOTHING-PROCESSED");
        }

        final LocalDateTime reportTimestamp = ReportUtils.fromTimestampJava(lastProcessedReport.getReport());
        final double hours = JavaTime.hoursBetween(reportTimestamp, JavaTime.nowUtc());

        return ResponseEntity.ok(hours <= 0.2 ? "OK" : "FAIL-EXPIRED");
    }
}
