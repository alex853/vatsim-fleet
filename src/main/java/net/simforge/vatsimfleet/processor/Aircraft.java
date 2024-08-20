package net.simforge.vatsimfleet.processor;

import net.simforge.commons.misc.JavaTime;

import java.time.ZoneOffset;

public class Aircraft {
    private final String aircraftType;
    private final String regNo;
    private final String airlineCode;
    private final double lat;
    private final double lon;
    private final int heading;
    private final long parkedAt;

    public Aircraft(final String aircraftType,
                    final String regNo,
                    final String airlineCode,
                    final double lat,
                    final double lon,
                    final int heading,
                    final long parkedAt) {
        this.aircraftType = aircraftType;
        this.regNo = regNo;
        this.airlineCode = airlineCode;
        this.lat = lat;
        this.lon = lon;
        this.heading = heading;
        this.parkedAt = parkedAt;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getHeading() {
        return heading;
    }

    public int getParkedMinutesAgo() {
        return (int) ((JavaTime.nowUtc().toEpochSecond(ZoneOffset.UTC) - parkedAt) / 60);
    }

    @Override
    public String toString() {
        return String.format("Aircraft{ type: %s, airline: %s, regNo: %s, lat: %.4f, lon: %.4f, hdg: %s, parkedAgo: %s }",
                aircraftType, airlineCode, regNo, lat, lon, heading, getParkedMinutesAgo());
    }
}
