package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

import it.polito.ezshop.model.EZBalanceOperation;

public class EZBalanceOperationSettersTest {

	
	@Test
	public void testSetBalanceId() {
		EZBalanceOperation bo = new EZBalanceOperation(1, LocalDate.now(), 30.0, "CREDIT");
		
		bo.setBalanceId(3);
		assertTrue(3 == bo.getBalanceId());
	}
	
	@Test
	public void testSetDate() {
		EZBalanceOperation bo = new EZBalanceOperation(1, LocalDate.now(), 30.0, "CREDIT");
		
		bo.setDate(LocalDate.now().minusDays(1));
		assertTrue(bo.getDate().equals(LocalDate.now().minusDays(1)));
	}
	
	@Test
	public void testSetMoney() {
		EZBalanceOperation bo = new EZBalanceOperation(1, LocalDate.now(), 30.0, "CREDIT");
		
		bo.setMoney(10.0);
		assertTrue(10.0 == bo.getMoney());
	}
	
	@Test
	public void testSetType() {
		EZBalanceOperation bo = new EZBalanceOperation(1, LocalDate.now(), 30.0, "CREDIT");
		
		bo.setType("DEBIT");
		assertTrue(bo.getType().equals("DEBIT"));
	}
}
