package net.simforge.vatsimfleet.processor;

public class Aircraft {
    private final String aircraftType;
    private final String regNo;
    private final String airlineCode;
    private final double lat;
    private final double lon;

    public Aircraft(final String aircraftType,
                    final String regNo,
                    final String airlineCode,
                    final double lat,
                    final double lon) {
        this.aircraftType = aircraftType;
        this.regNo = regNo;
        this.airlineCode = airlineCode;
        this.lat = lat;
        this.lon = lon;
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

    @Override
    public String toString() {
        return String.format("Aircraft{ type: %s, regNo: %s, airline: %s, %.4f, %.4f }",
                aircraftType, regNo,airlineCode, lat, lon);
    }
}
