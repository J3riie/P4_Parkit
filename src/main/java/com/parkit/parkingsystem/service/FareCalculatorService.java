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
        final Date inHour = ticket.getInTime();
        final Date outHour = ticket.getOutTime();
        final LocalDateTime inLocalDateTime = LocalDateTime.ofInstant(inHour.toInstant(), ZoneId.systemDefault());
        final LocalDateTime outLocalDateTime = LocalDateTime.ofInstant(outHour.toInstant(), ZoneId.systemDefault());

        final Duration duration = Duration.between(inLocalDateTime, outLocalDateTime);
        final long minuteDuration = duration.toMinutes();

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
        }
    }
}