package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import it.polito.ezshop.model.EZSaleTransaction;
import it.polito.ezshop.data.TicketEntry;

public class EZSaleTransactionSettersTest {

	@Test
	public void testSetTicketNumber() {
		EZSaleTransaction st = new EZSaleTransaction(1);
		
		st.setTicketNumber(3);
		assertTrue(st.getTicketNumber() == 3);
	}

	
	@Test
	public void testSetEntries() {
		EZSaleTransaction st = new EZSaleTransaction(1);
		
		List<TicketEntry> entries = new LinkedList<TicketEntry>();
		st.setEntries(entries);
		assertTrue(st.getEntries() == entries);
	}
	
	
	@Test
	public void testSetDiscountRate() {
		EZSaleTransaction st = new EZSaleTransaction(1);
		
		st.setDiscountRate(5);
		assertTrue(st.getDiscountRate() == 5);
	}
	
	
	@Test
	public void testSetPrice() {
		EZSaleTransaction st = new EZSaleTransaction(1);
		
		st.setPrice(2.5);
		assertTrue(st.getPrice() == 2.5);
	}
	
	
	@Test
	public void testSetStatus() {
		EZSaleTransaction st = new EZSaleTransaction(1);
		
		st.setStatus("PAYED");
		assertTrue(st.getStatus().equals("PAYED"));
	}
}
