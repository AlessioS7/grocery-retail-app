package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZUser;

public class EZUserSettersTest {

	@Test
	public void testSetId() {
		EZUser u = new EZUser(1, "Jack", "ps", "Administrator");
		
		u.setId(7);
		assertTrue(u.getId() == 7);
	}
	
	
	@Test
	public void testSetUsername() {
		EZUser u = new EZUser(1, "Jack", "ps", "Administrator");
		
		u.setUsername("John");
		assertTrue(u.getUsername().equals("John"));
	}
	
	
	@Test
	public void testSetPassword() {
		EZUser u = new EZUser(1, "Jack", "ps", "Administrator");
		
		u.setPassword("test");
		assertTrue(u.getPassword().equals("test"));
	}
	
	
	@Test
	public void testSetRole() {
		EZUser u = new EZUser(1, "Jack", "ps", "Administrator");
		
		u.setRole("Cashier");
		assertTrue(u.getRole().equals("Cashier"));
	}

}
