package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZCustomer;

public class ModifyPointsOnCardTest {

	@Test
	public void testNegativePointsToBeAddedFailure() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(20);
		
		assertFalse(c.modifyPointsOnCard(-50));
	}

	
	@Test
	public void testNegativePointsToBeAddedFailureBoundary() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(0);
		
		assertFalse(c.modifyPointsOnCard(-1));
	}
	
	
	@Test
	public void testNegativePointsToBeAddedSuccess() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(30);
		
		assertTrue(c.modifyPointsOnCard(-20));
	}

	
	@Test
	public void testNegativePointsToBeAddedSuccessBoundary() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(20);
		
		assertTrue(c.modifyPointsOnCard(-20));
	}
	
	
	@Test
	public void testPositivePointsToBeAdded() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(10);
		
		assertTrue(c.modifyPointsOnCard(50));
	}

	
	@Test
	public void testPositivePointsToBeAddedBoundary() {
		EZCustomer c = new EZCustomer(1, "Jack");
		
		c.setCustomerCard("Card1");
		c.setPoints(0);
		
		assertTrue(c.modifyPointsOnCard(1));
	}
}
