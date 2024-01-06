package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;

import org.junit.Test;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.model.AccountBook;

public class AccountBookSettersTest {

	
	@Test
	public void testSetCurrentBalance() {
		AccountBook u = new AccountBook();
		
		u.setCurrentBalance(300);
		assertTrue(u.computeBalance() == 300);
	}

	
	@Test
	public void testSetOperations() {
		AccountBook u = new AccountBook();
		
		u.setOperations(new LinkedList<BalanceOperation>());
		assertTrue(u.getOperations() != null);
	}
}
