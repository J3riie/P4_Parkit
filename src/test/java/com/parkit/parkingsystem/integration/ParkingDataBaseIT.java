package com.parkit.parkingsystem.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();

	private static ParkingSpotDAO parkingSpotDAO;

	private static TicketDAO ticketDAO;

	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.setDataBaseConfig(dataBaseTestConfig);
		ticketDAO = new TicketDAO();
		ticketDAO.setDataBaseConfig(dataBaseTestConfig);
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
	void testParkingACar() throws Exception {
		// checks that a ticket is actually saved in DB and Parking table is updated
		// with availability
		final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		final Ticket dbTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		assertThat(dbTicket.getId()).isEqualTo(1);
		assertThat(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).isEqualTo(2);
	}

	@Test
	void testParkingLotExit() throws Exception {
		// checks that the fare generated and out time are populated correctly in the
		// database
		final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		Thread.sleep(2000);
		parkingService.processExitingVehicle();
		final Ticket dbTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		assertThat(dbTicket.getOutTime()).isNotNull();
		assertFalse(dbTicket.isRecurringUser());
	}

	@Test
	void testParkingRecurringUser() throws Exception {
		// checks that a ticket is already saved in DB
		final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		parkingService.processIncomingVehicle();
		Thread.sleep(2000);
		parkingService.processExitingVehicle();
		final Ticket previousTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		parkingService.processIncomingVehicle();
		final Ticket currentTicket = ticketDAO.getTicket(inputReaderUtil.readVehicleRegistrationNumber());
		assertFalse(previousTicket.isRecurringUser());
		assertThat(previousTicket.getOutTime()).isNotNull();
		assertTrue(currentTicket.isRecurringUser());
		assertThat(currentTicket.getOutTime()).isNull();
	}

}
