package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

    private static ParkingSpotDAO parkingSpotDAO;

    private static TicketDAO ticketDAO;

    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1); // sets the vehicle type to CAR
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
    }

    @Test
    public void testParkingACar() throws Exception {
        // checks that a ticket is actually saved in DB and Parking table is updated with availability
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle(Clock.systemDefaultZone());
        final Ticket dbTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
        assertThat(dbTicket.getId()).isEqualTo(1);
        assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(2);
    }

    @Test
    public void testParkingLotExit() throws Exception {
        // checks that the fare generated and out time are populated correctly in the database
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        final Instant instantIn = Instant.parse("2018-08-19T16:02:42.00Z");
        parkingService.processIncomingVehicle(Clock.fixed(instantIn, ZoneId.of("Europe/Paris")));
        final Instant instantOut = Instant.parse("2022-08-19T16:02:42.00Z");
        parkingService.processExitingVehicle(Clock.fixed(instantOut, ZoneId.of("Europe/Paris")));
        final Ticket dbTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
        assertThat(dbTicket.getOutTime()).isNotNull();
    }

}
