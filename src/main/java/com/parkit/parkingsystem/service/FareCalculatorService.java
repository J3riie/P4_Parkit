package com.parkit.parkingsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        final long minuteDuration = calculateDurationInMinutes(ticket);
        // if (minuteDuration < 30) {
        // ticket.setPrice(0);
        // } else {
        switch (ticket.getParkingSpot().getParkingType()) {
        case CAR: {
            ticket.setPrice(minuteDuration / 60.0 * Fare.CAR_RATE_PER_HOUR);
            break;
        }
        case BIKE: {
            ticket.setPrice(minuteDuration / 60.0 * Fare.BIKE_RATE_PER_HOUR);
            break;
        }
        default:
            throw new IllegalArgumentException("Unkown Parking Type");
        // }
        }
    }

    public long calculateDurationInMinutes(Ticket ticket) {
        final Date inTime = ticket.getInTime();
        final Date outTime = ticket.getOutTime();
        final LocalDateTime inLocalDateTime = LocalDateTime.ofInstant(inTime.toInstant(), ZoneId.systemDefault());
        final LocalDateTime outLocalDateTime = LocalDateTime.ofInstant(outTime.toInstant(), ZoneId.systemDefault());

        final Duration duration = Duration.between(inLocalDateTime, outLocalDateTime);
        return duration.toMinutes();
    }
}