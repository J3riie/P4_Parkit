package com.parkit.parkingsystem.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.parkit.parkingsystem.util.InputReaderUtil;

public class InteractiveShell {

	private static final Logger logger = LogManager.getLogger("InteractiveShell");

	private InteractiveShell() {
		// This constructor is empty because it should not be instanciated
	}

	public static void loadInterface(InputReaderUtil inputReaderUtil, ParkingService parkingService) {
		logger.info("App initialized!!!");
		logger.info("Welcome to Parking System!");

		boolean continueApp = true;

		while (continueApp) {
			loadMenu();
			final int option = inputReaderUtil.readSelection();
			switch (option) {
			case 1: {
				parkingService.processIncomingVehicle();
				break;
			}
			case 2: {
				parkingService.processExitingVehicle();
				break;
			}
			case 3: {
				logger.info("Exiting from the system!");
				continueApp = false;
				break;
			}
			default:
				logger.warn("Unsupported option. Please enter a number corresponding to the provided menu");
			}
		}
	}

	private static void loadMenu() {
		logger.info("Please select an option. Simply enter the number to choose an action");
		logger.info("1 New Vehicle Entering - Allocate Parking Space");
		logger.info("2 Vehicle Exiting - Generate Ticket Price");
		logger.info("3 Shutdown System");
	}

}
