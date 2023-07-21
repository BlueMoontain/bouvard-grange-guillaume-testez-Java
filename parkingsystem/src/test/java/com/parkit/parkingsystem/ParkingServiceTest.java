
package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


import java.util.Date;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)

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

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
    }


    @Test
    public void testProcessIncomingVehicle() {

   //Mock utilisateur pour sélectionner une voiture (1)
   when(inputReaderUtil.readSelection()).thenReturn(1);

   //Mock de la sauvegarde du ticket
   when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

   //Mock renvoyant 1 pour la place de parking
   when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

   //Mock renvoyant 0 pour nbTicket (jamais venu)
    when(ticketDAO.getNbTicket(anyString())).thenReturn(0);

   //Appel de la méthode à tester
   parkingService.processIncomingVehicle();

   // Vérification que le parking a été mis à jour 1 fois 
   verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

   // Vérification que le ticket a été enregistré 1 fois 
   verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));

   // Vérification que nbTicket a été utilisé 1 fois
   verify(ticketDAO, times(1)).getNbTicket(anyString());
}


@Test
public void processExitingVehicleTestUnableUpdate() {
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
}


@Test
public void testGetNextParkingNumberIfAvailable() {

    when(inputReaderUtil.readSelection()).thenReturn(1);

    // Crée un objet ParkingSpot avec ID=1 et disponibilité=true
    ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

    // Mock méthode getNextAvailableSlot() pour renvoyer le parkingSpot créé
    when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(parkingSpot.getId());

    // Appel de la méthode à tester
    ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que la méthode getNextAvailableSlot() est appelée avec le bon type de véhicule
    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));

    // Vérifie que le résultat est parkingSpot avec ID=1 et disponibilité=true
    assertNotNull(result);
    assertEquals(1, result.getId());
    assertTrue(result.isAvailable());
}

@Test
public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

    when(inputReaderUtil.readSelection()).thenReturn(1);

    // Mock la méthode getNextAvailableSlot() pour renvoyer null
    when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

    // Appel de la méthode à tester
    ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que getNextAvailableSlot() est appelée avec le bon type de véhicule
    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);

    // vérifie que parkingSpot renvoie "null" lorsque aucun emplacement de parking disponible n'est trouvé
    assertNull(parkingSpot);
}

@Test
public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
    // Mock méthode readSelection() pour renvoyer 3
    when(inputReaderUtil.readSelection()).thenReturn(3);

    // Appel de la méthode à tester
    ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

    // Vérifie que readSelection() est appelée
    verify(inputReaderUtil, times(1)).readSelection();

    // Vérifiez que getNextAvailableSlot() n'est pas appelée
    verify(parkingSpotDAO, never()).getNextAvailableSlot(any(ParkingType.class));

    // Vérifiez que le résultat renvoie "null"
    assertNull(result);
}

}