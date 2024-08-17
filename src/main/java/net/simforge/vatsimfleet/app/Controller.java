package net.simforge.vatsimfleet.app;

import net.simforge.vatsimfleet.processor.Aircraft;
import net.simforge.vatsimfleet.processor.VatsimFleetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        List<Aircraft> parkedAircraft = VatsimFleetProcessor.getParkedAircraftByIcao(icao);
        return ResponseEntity.ok(parkedAircraft);
    }

    @GetMapping("status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("status"); // todo
    }
}
