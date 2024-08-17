package net.simforge.vatsimfleet.processor;

public class Aircraft {
    private final String aircraftType;

    public Aircraft(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    @Override
    public String toString() {
        return "Aircraft{" +
                "aircraftType='" + aircraftType + '\'' +
                '}';
    }
}
