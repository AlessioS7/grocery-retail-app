package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZCustomer;

public class EZCustomerSettersTest {

	@Test
	public void testSetCustomerName() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerName("Alex");
		assertTrue(c.getCustomerName().equals("Alex"));
	}
	
	@Test
	public void testSetCustomerCard() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("0123456789");
		assertTrue(c.getCustomerCard().equals("0123456789"));
	}

	
	@Test
	public void testSetId() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setId(3);
		assertTrue(3 == c.getId());
	}
	
	@Test
	public void testSetPointsCardDoesntExist() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setPoints(42);
		// the method should not set the points when we haven't assigned a card yet
		assertTrue(c.getPoints() == 0);
	}
	
	@Test
	public void testSetPointsCardExists() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("0123456789");
		c.setPoints(42);
		assertTrue(c.getPoints() == 42);
	}

}
