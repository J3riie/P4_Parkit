package com.parkit.parkingsystem;

import static com.parkit.parkingsystem.constants.ParkingType.CAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

	private void setUpWorkingTest() {
		try {
			when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	@Test
	public void processIncomingVehicule_shouldParkVehicle_whenParkingSlotIsAvailable() throws SQLException {
		setUpWorkingTest();
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

		parkingService.processIncomingVehicle();

		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
	}

	@Test
	public void processIncomingVehicle_shouldThrowException_whenParkingSpotIsIllegal() throws SQLException {
		parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		when(inputReaderUtil.readSelection()).thenReturn(2);
		when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

		parkingService.processIncomingVehicle();

		verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, times(0)).saveTicket(any(Ticket.class));
	}

	@Test
	public void processExitingVehicle_shouldUpdateParking() throws SQLException {
		setUpWorkingTest();
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Ticket ticket = generateTicket(CAR, inTime);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

		parkingService.processExitingVehicle();

		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		assertTrue(ticket.getParkingSpot().isAvailable());
	}

	@Test
	public void processExitingVehicle_shouldNotUpdateParking_whenErrorOccurred() throws SQLException {
		setUpWorkingTest();
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Ticket ticket = generateTicket(CAR, inTime);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

		parkingService.processExitingVehicle();

		assertEquals(0, parkingSpotDAO.getNextAvailableSlot(CAR));
		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
		assertFalse(ticket.getParkingSpot().isAvailable());
	}

	@Test
	public void processExitingVehicle_shouldThrowSQLException_whenGetTicket() throws SQLException {
		setUpWorkingTest();
		new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		when(ticketDAO.getTicket(anyString())).thenThrow(new SQLException("Failed to get ticket"));

		parkingService.processExitingVehicle();

		verify(ticketDAO, times(0)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
	}

	@Test
	public void parkingForLessThan30Minutes_shouldBeFree() throws SQLException {
		setUpWorkingTest();
		final Date inTime = new Date(System.currentTimeMillis() - (20 * 60 * 1000));
		final Ticket ticket = generateTicket(CAR, inTime);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

		parkingService.processExitingVehicle();

		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		assertEquals(0, ticket.getPrice());
		assertTrue(ticket.getParkingSpot().isAvailable());

	}

	@Test
	public void applyDiscountForRecurringUser() throws SQLException {
		setUpWorkingTest();
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Ticket ticket = generateTicket(CAR, inTime);
		ticket.setRecurringUser(true);
		when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
		when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
		when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

		parkingService.processExitingVehicle();

		assertEquals(0.95 * 1.5, ticket.getPrice());
		verify(ticketDAO, times(1)).updateTicket(ticket);
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		assertTrue(ticket.getParkingSpot().isAvailable());
	}

	private Ticket generateTicket(ParkingType type, Date inTime) {
		final ParkingSpot parkingSpot = new ParkingSpot(1, type, false);
		final Ticket ticket = new Ticket();
		ticket.setInTime(inTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setVehicleRegNumber("ABCDEF");
		return ticket;
	}
}
