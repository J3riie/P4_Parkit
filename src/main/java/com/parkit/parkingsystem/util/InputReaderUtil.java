package com.parkit.parkingsystem.util;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InputReaderUtil {

    private static Scanner scan = new Scanner(System.in);

    private static final Logger logger = LogManager.getLogger("InputReaderUtil");

    public int readSelection() {
        try {
            final int input = Integer.parseInt(scan.nextLine());
            return input;
        } catch (final Exception e) {
            logger.error("Error while reading user input from Shell", e);
            logger.warn("Error reading input. Please enter valid number for proceeding further");
            return -1;
        }
    }

    public String readVehicleRegistrationNumber() throws Exception {
        try {
            final String vehicleRegNumber = scan.nextLine();
            if (vehicleRegNumber == null || vehicleRegNumber.trim().length() == 0) {
                throw new IllegalArgumentException("Invalid input provided");
            }
            return vehicleRegNumber;
        } catch (final Exception e) {
            logger.error("Error while reading user input from Shell", e);
            logger.warn("Error reading input. Please enter a valid string for vehicle registration number");
            throw e;
        }
    }

}
