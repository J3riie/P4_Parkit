package com.parkit.parkingsystem.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private final InputReaderUtil inputReaderUtil;

    private final ParkingSpotDAO parkingSpotDAO;

    private final TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle(Clock clock) {
        try {
            final ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                final String vehicleRegNumber = getVehichleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);// allot this parking space and mark it's availability as false

                final Date inTime = Date.from(LocalDate.now(clock).atStartOfDay().toInstant(ZoneOffset.UTC));
                final Ticket ticket = new Ticket();
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                logger.info("Generated Ticket and saved in DB");
                logger.info("Please park your vehicle in spot number {}", parkingSpot.getId());
                logger.info("Recorded in-time for vehicle number {} is {}", vehicleRegNumber, inTime);
            }
        } catch (final Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    private String getVehichleRegNumber() throws Exception {
        logger.info("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            final ParkingType parkingType = getVehichleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (final IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (final Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehichleType() {
        logger.info("Please select vehicle type from menu");
        logger.info("1 CAR");
        logger.info("2 BIKE");
        final int input = inputReaderUtil.readSelection();
        switch (input) {
        case 1: {
            return ParkingType.CAR;
        }
        case 2: {
            return ParkingType.BIKE;
        }
        default: {
            logger.warn("Incorrect input provided");
            throw new IllegalArgumentException("Entered input is invalid");
        }
        }
    }

    public void processExitingVehicle(Clock clock) {
        try {
            final String vehicleRegNumber = getVehichleRegNumber();
            final Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            final Date outTime = Date.from(LocalDate.now(clock).atStartOfDay().toInstant(ZoneOffset.UTC));
            ticket.setOutTime(outTime);
            fareCalculatorService.calculateFare(ticket);
            if (ticketDAO.updateTicket(ticket)) {
                final ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                logger.info("Please pay the parking fare: {}", ticket.getPrice());
                logger.info("Recorded out-time for vehicle number {} is {}", ticket.getVehicleRegNumber(), outTime);
            } else {
                logger.warn("Unable to update ticket information. Error occurred");
            }
        } catch (final Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }
}
