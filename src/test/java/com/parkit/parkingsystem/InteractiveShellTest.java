package com.parkit.parkingsystem;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.service.InteractiveShell;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class InteractiveShellTest {

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @Mock
    private static ParkingSpotDAO parkingSpotDAO;

    @Mock
    private static TicketDAO ticketDAO;

    @Mock
    private static ParkingService parkingService;

    @Test
    public void givenLoadInterface_whenCase1_thenProcessIncomingVehicleIsCalled() {
        // Given When
        when(inputReaderUtil.readSelection()).thenReturn(1, 3);
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);
        // Then
        verify(parkingService, times(1)).processIncomingVehicle();
    }

    @Test
    public void givenLoadInterface_whenCase2_thenProcessExitingVehicleIsCalled() {
        // Given When
        when(inputReaderUtil.readSelection()).thenReturn(2, 3);
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);
        // Then
        verify(parkingService, times(1)).processExitingVehicle();
    }

    @Test
    public void givenLoadInterface_whenCase3_thenExitingFromSystem() {
        // Given When
        when(inputReaderUtil.readSelection()).thenReturn(3);
        InteractiveShell.loadInterface(inputReaderUtil, parkingSpotDAO, ticketDAO, parkingService);
        // Then
        verify(parkingService, times(0)).processIncomingVehicle();
        verify(parkingService, times(0)).processExitingVehicle();
    }
}
