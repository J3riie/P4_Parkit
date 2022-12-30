package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;

class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;

	private Ticket ticket;

	@Mock
	private TicketDAO ticketDAO;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	void calculateFareCar() {
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
	}

	@Test
	void calculateFareBike() {
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
	}

	@Test
	void calculateFareUnkownType() {
		final Date inTime = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	void calculateFareBikeWithFutureInTime() {
		final Date inTime = new Date(System.currentTimeMillis() + (60 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	void calculateFareBikeWithLessThanOneHourParkingTime() {
		// 45 minutes parking time should give 3/4th parking fare
		final Date inTime = new Date(System.currentTimeMillis() - (45 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	void calculateFareCarWithLessThanOneHourParkingTime() {
		// 45 minutes parking time should give 3/4th parking fare
		final Date inTime = new Date(System.currentTimeMillis() - (45 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	void calculateFareCarWithMoreThanADayParkingTime() {
		// 24 hours parking time should give 24 * parking fare per hour
		final Date inTime = new Date(System.currentTimeMillis() - (24 * 60 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
	}

	@Test
	void calculateFareCarUnderHalfAnHourParkingTime() {
		// 20 minutes parking time should be free
		final Date inTime = new Date(System.currentTimeMillis() - (20 * 60 * 1000));
		final Date outTime = new Date();
		final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertEquals(0, ticket.getPrice());
	}
}
