package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import it.polito.ezshop.model.ReturnTransaction;

public class ReturnTransactionSettersTest {

	@Test
	public void testSetUsername() {
		ReturnTransaction rt = new ReturnTransaction(1, 1);
		
		rt.setReturnId(3);
		assertTrue(rt.getReturnId() == 3);
	}
	
	@Test
	public void testSetSaleId() {
		ReturnTransaction rt = new ReturnTransaction(1, 1);
		
		rt.setSaleId(3);
		assertTrue(rt.getSaleId() == 3);
	}
	
	@Test
	public void testSetProducts() {
		ReturnTransaction rt = new ReturnTransaction(1, 1);
		
		rt.setProducts(new HashMap<String, Integer>());
		assertTrue(rt.getProducts() != null);
	}
	
	@Test
	public void testSetMoneyToReturn() {
		ReturnTransaction rt = new ReturnTransaction(1, 1);
		
		rt.setMoneyToReturn(45.0);
		assertTrue(rt.getMoneyToReturn() == 45.0);
	}
	
	@Test
	public void testSetState() {
		ReturnTransaction rt = new ReturnTransaction(1, 1);
		
		rt.setState("CLOSED");
		assertTrue(rt.getState().equals("CLOSED"));
	}
}
