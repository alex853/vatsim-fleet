package net.simforge.vatsimfleet.processor;

import net.simforge.commons.legacy.misc.Settings;
import net.simforge.networkview.core.Network;
import net.simforge.networkview.core.Position;
import net.simforge.networkview.core.report.RegNoPatterns;
import net.simforge.networkview.core.report.compact.CompactifiedStorage;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class VatsimFleetProcessor {

    private static final Logger log = LoggerFactory.getLogger(VatsimFleetProcessor.class);

    private static final CompactifiedStorage storage = CompactifiedStorage.getStorage(Settings.get("storage.root"), Network.VATSIM);
    private static String lastProcessedReport = null;
    private static long nextTimeToLookForReport = 0;

    private static Map<Integer, PilotStatus> pilots = new TreeMap<>();
    private static Map<String, List<Aircraft>> parkedAircraft = new TreeMap<>();

    public static void processOneReport() throws IOException {
        final long now = System.currentTimeMillis();
        if (nextTimeToLookForReport != 0 && now < nextTimeToLookForReport) {
            return;
        }

        final Map<Integer, PilotStatus> pilots = new TreeMap<>(VatsimFleetProcessor.pilots);
        final Map<String, List<Aircraft>> parkedAircraft = new TreeMap<>(VatsimFleetProcessor.parkedAircraft);

        final String nextReport = lastProcessedReport != null
                ? storage.getNextReport(lastProcessedReport)
                : storage.getFirstReport();

        if (nextReport == null) {
            nextTimeToLookForReport = now + 5000;
            return;
        } else {
            nextTimeToLookForReport = 0;
        }

        log.info("Processing report {}", nextReport);
        final List<Position> positions = storage.loadPositions(nextReport);

        positions.forEach(position -> {
            final int pilotNumber = position.getPilotNumber();

            PilotStatus pilotStatus = pilots.get(pilotNumber);
            if (pilotStatus == null) {
                if (position.isInAirport() && position.isOnGround() && lastProcessedReport != null) {
                    final String airportIcao = position.getAirportIcao();
                    final String aircraftType = position.getFpAircraftType();

                    if (airportIcao != null && aircraftType != null) {
                        activateAircraft(position, parkedAircraft);
                    }
                }

                pilotStatus = new PilotStatus(position);
                pilots.put(pilotNumber, pilotStatus);
            } else {
                pilotStatus.setPosition(position);
            }
        });

        //noinspection unchecked
        final Collection<Integer> unseenPilotNumbers = CollectionUtils.subtract(
                pilots.keySet(),
                positions.stream()
                        .map(Position::getPilotNumber)
                        .collect(Collectors.toSet()));
        unseenPilotNumbers.forEach(pilotNumber -> {
            final PilotStatus pilotStatus = pilots.remove(pilotNumber);

            final Position position = pilotStatus.getPosition();
            if (!position.isOnGround() || !position.isInAirport()) {
                return;
            }

            if (position.getGroundspeed() != 0) {
                return; // this could be improved by kind of tracking, looking for several last positions
            }

            final String aircraftType = position.getFpAircraftType();
            final String airportIcao = position.getAirportIcao();

            if (airportIcao == null || aircraftType == null) {
                return;
            }

            parkAircraft(position, position.getHeading(), parkedAircraft);
        });

        VatsimFleetProcessor.pilots = pilots;
        VatsimFleetProcessor.parkedAircraft = parkedAircraft;
        lastProcessedReport = nextReport;
    }

    private static void parkAircraft(final Position position, final int heading, final Map<String, List<Aircraft>> parkedAircraft) {
        final String aircraftType = position.getFpAircraftType();
        final String airportIcao = position.getAirportIcao();
        final String regNo = position.getRegNo();
        final String airlineCode = getAirlineCode(position);

        final Aircraft aircraft = new Aircraft(
                aircraftType,
                regNo,
                airlineCode,
                position.getCoords().getLat(),
                position.getCoords().getLon(),
                heading,
                position.getReportInfo().getDt().toEpochSecond(ZoneOffset.UTC));

        final List<Aircraft> aircraftInAirport = parkedAircraft.getOrDefault(airportIcao, Collections.emptyList());
        final ArrayList<Aircraft> newAircraftInAirport = new ArrayList<>(aircraftInAirport);
        newAircraftInAirport.add(aircraft);
        parkedAircraft.put(airportIcao, Collections.unmodifiableList(newAircraftInAirport));
        log.info("Airport {} - PARK - Aircraft {} PARKED", airportIcao, aircraft);
    }

    private static void activateAircraft(final Position position, final Map<String, List<Aircraft>> parkedAircraft) {
        final String airportIcao = position.getAirportIcao();
        final String aircraftType = position.getFpAircraftType();
        final String airlineCode = getAirlineCode(position);

        final List<Aircraft> aircraftInAirport = parkedAircraft.getOrDefault(airportIcao, Collections.emptyList());
        final List<Aircraft> aircraftByType = aircraftInAirport.stream().filter(a -> Objects.equals(aircraftType, a.getAircraftType())).collect(Collectors.toList());
        final List<Aircraft> aircraftByAirline = aircraftByType.stream().filter(a -> Objects.equals(airlineCode, a.getAirlineCode())).collect(Collectors.toList());

        if (aircraftByAirline.isEmpty()) {
            log.info("Airport {} - ACTIVATE - Looking for Type {}, Airline {} - No suitable aircraft found", airportIcao, aircraftType, airlineCode);
            return;
        }

        final Aircraft aircraft = aircraftInAirport.get(0);
        final ArrayList<Aircraft> newAircraftInAirport = new ArrayList<>(aircraftInAirport);
        newAircraftInAirport.remove(aircraft);
        parkedAircraft.put(airportIcao, Collections.unmodifiableList(newAircraftInAirport));
        log.info("Airport {} - ACTIVATE - Looking for Type {}, Airline {} - Aircraft {} ACTIVATED", airportIcao, aircraftType, airlineCode, aircraft);
    }

    private static String getAirlineCode(final Position position) {
        final String callsign = position.getCallsign();
        final String regNo = position.getRegNo();

        if (callsign.equals(regNo)) {
            return null; // both callsign and regno have the same regno and there is no airline code available
        } else if (RegNoPatterns.isRegNo(callsign)) {
            return null; // callsign keeps regno so there is no airline code available
        } else {
            final String callsignHead = callsign.substring(0, Math.min(3, callsign.length()));
            if (callsignHead.matches(".*\\d.*")) {
                ErroneousCases.report(callsign, regNo, position.getReportInfo().getReport());
                return null;
            }
            return callsignHead;
        }
    }

    public static List<Aircraft> getParkedAircraftByIcao(final String icao) {
        return parkedAircraft.getOrDefault(icao, new ArrayList<>());
    }

    public static String getLastProcessedReport() {
        return lastProcessedReport;
    }

    public static int getAircraftCount() {
        return parkedAircraft.keySet().stream().mapToInt(String::length).sum();
    }

    private static class PilotStatus {
        private Position position;

        public PilotStatus(Position position) {
            this.position = position;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
    }

}
