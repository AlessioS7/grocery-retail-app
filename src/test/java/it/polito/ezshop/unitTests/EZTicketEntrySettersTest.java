package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZTicketEntry;

public class EZTicketEntrySettersTest {

	@Test
	public void testSetBarCode() {
		EZTicketEntry te = new EZTicketEntry("0123456789005", "Prod1", 2, 1.0);
		
		te.setBarCode("00012345678905");
		assertTrue(te.getBarCode().equals("00012345678905"));
	}
	
	
	@Test
	public void testSetProductDescription() {
		EZTicketEntry te = new EZTicketEntry("0123456789005", "Prod1", 2, 1.0);
		
		te.setProductDescription("Prod4");
		assertTrue(te.getProductDescription().equals("Prod4"));
	}
	
	
	@Test
	public void testSetAmount() {
		EZTicketEntry te = new EZTicketEntry("0123456789005", "Prod1", 2, 1.0);
		
		te.setAmount(7);
		assertTrue(te.getAmount() == 7);
	}
	
	
	@Test
	public void testSetPricePerUnit() {
		EZTicketEntry te = new EZTicketEntry("0123456789005", "Prod1", 2, 1.0);
		
		te.setPricePerUnit(1.5);
		assertTrue(te.getPricePerUnit() == 1.5);
	}
	
	@Test
	public void testSetDiscountRate() {
		EZTicketEntry te = new EZTicketEntry("0123456789005", "Prod1", 2, 1.0);
		
		te.setDiscountRate(5);
		assertTrue(te.getDiscountRate() == 5);
	}
}
