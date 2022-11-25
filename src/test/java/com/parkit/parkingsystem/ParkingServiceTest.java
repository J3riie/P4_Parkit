package com.parkit.parkingsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;

    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            final Ticket ticket = new Ticket();
            final Instant instantIn = Instant.parse("2022-08-19T16:02:42.00Z");
            final Date dateIn = Date.from(LocalDate.now(Clock.fixed(instantIn, ZoneId.of("Europe/Paris"))).atStartOfDay().toInstant(ZoneOffset.UTC));
            ticket.setInTime(dateIn);
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() {
        final Instant instantOut = Instant.parse("2022-08-19T17:02:42.00Z");
        parkingService.processExitingVehicle(Clock.fixed(instantOut, ZoneId.of("Europe/Paris")));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processIncomingVehiculeTest() {
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        final Ticket expectedTicket = new Ticket();
        final Instant instantIn = Instant.parse("2022-08-19T16:02:42.00Z");
        final Date dateIn = Date.from(LocalDate.now(Clock.fixed(instantIn, ZoneId.of("Europe/Paris"))).atStartOfDay().toInstant(ZoneOffset.UTC));
        expectedTicket.setInTime(dateIn);
        expectedTicket.setParkingSpot(parkingSpot);
        expectedTicket.setVehicleRegNumber("ABCDEF");
        parkingService.processIncomingVehicle(Clock.fixed(instantIn, ZoneId.of("Europe/Paris")));
        final Instant instantOut = Instant.parse("2022-08-19T17:02:42.00Z");
        parkingService.processExitingVehicle(Clock.fixed(instantOut, ZoneId.of("Europe/Paris")));
        assertThat(ticketDAO.getTicket("ABCDEF").getVehicleRegNumber()).isEqualTo(expectedTicket.getVehicleRegNumber());
    }

    @Test
    public void calculateFareCareWithOneHourParkingTime() {
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        final Ticket expectedTicket = new Ticket();
        final Instant instantIn = Instant.parse("2022-08-19T16:02:42.00Z");
        final Date dateIn = Date.from(LocalDate.now(Clock.fixed(instantIn, ZoneId.of("Europe/Paris"))).atStartOfDay().toInstant(ZoneOffset.UTC));
        expectedTicket.setInTime(dateIn);
        expectedTicket.setParkingSpot(parkingSpot);
        expectedTicket.setVehicleRegNumber("ABCDEF");
        expectedTicket.setPrice(1.5);
        parkingService.processIncomingVehicle(Clock.fixed(instantIn, ZoneId.of("Europe/Paris")));
        final Instant instantOut = Instant.parse("2022-08-19T17:02:42.00Z");
        when(ticketDAO.getTicket("ABCDEF")).thenReturn(expectedTicket);
        parkingService.processExitingVehicle(Clock.fixed(instantOut, ZoneId.of("Europe/Paris")));
        assertThat(ticketDAO.getTicket("ABCDEF").getPrice()).isEqualTo(Fare.CAR_RATE_PER_HOUR);
    }

    // @Test
    // public void calculateFareWithDiscount() {
    // final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
    // final Ticket expectedTicket = new Ticket();
    // final Instant instantIn = Instant.parse("2022-08-19T16:02:42.00Z");
    // final Date dateIn = Date.from(LocalDate.now(Clock.fixed(instantIn, ZoneId.of("Europe/Paris"))).atStartOfDay().toInstant(ZoneOffset.UTC));
    // expectedTicket.setInTime(dateIn);
    // expectedTicket.setParkingSpot(parkingSpot);
    // expectedTicket.setVehicleRegNumber("ABCDEF");
    // parkingService.processIncomingVehicle(Clock.fixed(instantIn, ZoneId.of("Europe/Paris")));
    // final Instant instantOut = Instant.parse("2022-08-19T17:02:42.00Z");
    // parkingService.processExitingVehicle(Clock.fixed(instantOut, ZoneId.of("Europe/Paris")));
    //
    // final Instant secondInstantIn = Instant.parse("2022-08-19T19:02:42.00Z");
    // final Date secondDateIn = Date.from(LocalDate.now(Clock.fixed(secondInstantIn, ZoneId.of("Europe/Paris"))).atStartOfDay().toInstant(ZoneOffset.UTC));
    // expectedTicket.setInTime(secondDateIn);
    // expectedTicket.setParkingSpot(parkingSpot);
    // expectedTicket.setVehicleRegNumber("ABCDEF");
    // parkingService.processIncomingVehicle(Clock.fixed(secondInstantIn, ZoneId.of("Europe/Paris")));
    // final Instant secondInstantOut = Instant.parse("2022-08-19T20:02:42.00Z");
    // parkingService.processExitingVehicle(Clock.fixed(secondInstantOut, ZoneId.of("Europe/Paris")));
    //
    // assertThat(ticketDAO.getTicket("ABCDEF").getPrice()).isEqualTo(2 * Fare.CAR_RATE_PER_HOUR * 0.95);
    // }
}
