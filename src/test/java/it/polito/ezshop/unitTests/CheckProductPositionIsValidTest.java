package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.data.EZShop;

public class CheckProductPositionIsValidTest {

	@Test
	public void testNullString() {
		assertFalse(EZShop.checkProductPositionIsValid(null));
	}
	
	@Test
	public void testEmptyString() {
		assertFalse(EZShop.checkProductPositionIsValid(""));
	}
	
	@Test
	public void testNotEmptyInvalidString() {
		assertFalse(EZShop.checkProductPositionIsValid("fqwe5132"));
	}
	
	@Test
	public void testNotEmptyValidString() {
		assertTrue(EZShop.checkProductPositionIsValid("11-aa-22"));
	}
}
