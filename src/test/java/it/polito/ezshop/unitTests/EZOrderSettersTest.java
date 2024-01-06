package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZOrder;

public class EZOrderSettersTest {

	@Test
	public void testSetOrderId() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setOrderId(3);
		assertTrue(o.getOrderId() == 3);
	}
	
	
	@Test
	public void testSetProductCode() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setProductCode("00012345678905");
		assertTrue(o.getProductCode().equals("00012345678905") );
	}
	
	
	@Test
	public void testSetBalanceId() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setBalanceId(3);
		assertTrue(o.getBalanceId() == 3);
	}
	
	
	@Test
	public void testSetPricePerUnit() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setPricePerUnit(3.5);
		assertTrue(o.getPricePerUnit() == 3.5);
	}
	
	
	@Test
	public void testSetQuantity() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setQuantity(5);
		assertTrue(o.getQuantity() == 5);
	}
	
	@Test
	public void testSetStatus() {
		EZOrder o = new EZOrder(1, "012345678905", 50, 2.2, "ISSUED");
		
		o.setStatus("PAYED");
		assertTrue(o.getStatus().equals("PAYED"));
	}
}
