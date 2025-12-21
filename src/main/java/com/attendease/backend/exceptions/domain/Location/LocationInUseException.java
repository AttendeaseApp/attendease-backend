package com.attendease.backend.exceptions.domain.Location;

public class LocationInUseException extends LocationException {

    private final String locationName;
    private final long eventSessionCount;
    private final long attendanceRecordCount;

    public LocationInUseException(String locationName, long eventSessionCount, long attendanceRecordCount, String operation) {
        super(buildMessage(locationName, eventSessionCount, attendanceRecordCount, operation));
        this.locationName = locationName;
        this.eventSessionCount = eventSessionCount;
        this.attendanceRecordCount = attendanceRecordCount;
    }

    private static String buildMessage(String locationName, long eventCount, long attendanceCount, String operation) {
        StringBuilder message = new StringBuilder("Cannot ")
            .append(operation)
            .append(" location '")
            .append(locationName)
            .append("' as it is used by ");

        boolean hasEvents = eventCount > 0;
        boolean hasAttendance = attendanceCount > 0;

        if (hasEvents && hasAttendance) {
            message.append(eventCount)
                .append(" event session(s) and ")
                .append(attendanceCount)
                .append(" attendance record(s)");
        } else if (hasEvents) {
            message.append(eventCount).append(" event session(s)");
        } else {
            message.append(attendanceCount).append(" attendance record(s)");
        }
        message.append(". Please reassign or delete the dependent records first.");
        return message.toString();
    }

    public String getLocationName() {
        return locationName;
    }

    public long getEventSessionCount() {
        return eventSessionCount;
    }

    public long getAttendanceRecordCount() {
        return attendanceRecordCount;
    }
}
