package net.simforge.vatsimfleet.processor;

import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.Position;
import net.simforge.networkview.core.report.persistence.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class VatsimFleetProcessor {

    private static final Logger log = LoggerFactory.getLogger(VatsimFleetProcessor.class);

    private static final ReportSessionManager sessionManager = new ReportSessionManager();
    private static final ReportOpsService reportOpsService = new BaseReportOpsService(sessionManager, Network.VATSIM);
    private static Report lastProcessedReport = null;

    private static Map<Integer, PilotStatus> pilots = new TreeMap<>();
    private static Map<String, List<Aircraft>> parkedAircraft = new TreeMap<>();

    public static void processOneReport() {
        final Map<Integer, PilotStatus> pilots = new TreeMap<>(VatsimFleetProcessor.pilots);
        final Map<String, List<Aircraft>> parkedAircraft = new TreeMap<>(VatsimFleetProcessor.parkedAircraft);

        final Report nextReport = lastProcessedReport != null
                ? reportOpsService.loadNextReport(lastProcessedReport.getReport())
                : reportOpsService.loadFirstReport();

        if (nextReport == null || !nextReport.getParsed()) {
            return;
        }

        log.info("Processing report {}", nextReport);
        final List<ReportPilotPosition> positions = reportOpsService.loadPilotPositions(nextReport);

        positions.forEach(p -> {
            final int pilotNumber = p.getPilotNumber();

            PilotStatus pilotStatus = pilots.get(pilotNumber);
            if (pilotStatus == null) {
                Position position = Position.create(p);
                if (position.isInAirport() && position.isOnGround()) {
                    final String airportIcao = position.getAirportIcao();
                    final String aircraftType = position.getFpAircraftType();

                    if (airportIcao != null && aircraftType != null) {
                        //noinspection MismatchedQueryAndUpdateOfCollection
                        final List<Aircraft> aircraftInAirport = parkedAircraft.getOrDefault(airportIcao, new ArrayList<>());

                        final Optional<Aircraft> aircraft = aircraftInAirport.stream().filter(a -> aircraftType.equals(a.getAircraftType())).findFirst();
                        if (aircraft.isPresent()) {
                            aircraftInAirport.remove(aircraft.get());
                            log.info("Aircraft {} UNparked at {}", aircraft, airportIcao);
                        }
                    }
                }

                pilotStatus = new PilotStatus(p);
                pilots.put(pilotNumber, pilotStatus);
            } else {
                pilotStatus.setPosition(p);
            }
        });

        //noinspection unchecked
        final Collection<Integer> unseenPilotNumbers = CollectionUtils.subtract(pilots.keySet(), positions.stream().map(ReportPilotPosition::getPilotNumber).collect(Collectors.toSet()));
        unseenPilotNumbers.forEach(pilotNumber -> {
            final PilotStatus pilotStatus = pilots.remove(pilotNumber);

            ReportPilotPosition reportPilotPosition = pilotStatus.getPosition();
            Position position = Position.create(reportPilotPosition);
            if (!position.isOnGround() || !position.isInAirport()) {
                return;
            }

            final String aircraftType = position.getFpAircraftType();
            final String airportIcao = position.getAirportIcao();

            if (airportIcao == null || aircraftType == null) {
                return;
            }

            final Aircraft aircraft = new Aircraft(aircraftType);

            final List<Aircraft> aircraftInAirport = parkedAircraft.computeIfAbsent(airportIcao, icao -> new ArrayList<>());
            aircraftInAirport.add(aircraft);
            log.info("Aircraft {} parked at {}", aircraft, airportIcao);
        });

        VatsimFleetProcessor.pilots = pilots;
        VatsimFleetProcessor.parkedAircraft = parkedAircraft;
        lastProcessedReport = nextReport;
    }

    public static List<Aircraft> getParkedAircraftByIcao(final String icao) {
        return parkedAircraft.getOrDefault(icao, new ArrayList<>());
    }

    public static Report getLastProcessedReport() {
        return lastProcessedReport;
    }

    private static class PilotStatus {
        private ReportPilotPosition position;

        public PilotStatus(ReportPilotPosition position) {
            this.position = position;
        }

        public ReportPilotPosition getPosition() {
            return position;
        }

        public void setPosition(ReportPilotPosition position) {
            this.position = position;
        }
    }

}
