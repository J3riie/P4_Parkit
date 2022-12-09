package com.parkit.parkingsystem.service;

import static com.parkit.parkingsystem.constants.Fare.BIKE_RATE_PER_HOUR;
import static com.parkit.parkingsystem.constants.Fare.CAR_RATE_PER_HOUR;

import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    private static final double NO_DISCOUNT = 1;

    public void calculateFare(Ticket ticket) {
        calculate(ticket, NO_DISCOUNT);
    }

    public void calculateFare(Ticket ticket, double discount) {
        calculate(ticket, discount);
    }

    private void calculate(Ticket ticket, double discount) {
        if (ticket.isInvalid()) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        switch (ticket.getParkingSpot().getParkingType()) {
        case CAR: {
            ticket.computePrice(CAR_RATE_PER_HOUR * discount);
            break;
        }
        case BIKE: {
            ticket.computePrice(BIKE_RATE_PER_HOUR * discount);
            break;
        }
        default:
            throw new IllegalArgumentException("Unknown Parking Type");
        }
    }
}