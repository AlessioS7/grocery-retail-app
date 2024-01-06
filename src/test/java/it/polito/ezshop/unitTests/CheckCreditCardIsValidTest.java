package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.data.EZShop;

public class CheckCreditCardIsValidTest {

	@Test
	public void testNullString() {
		assertFalse(EZShop.checkCreditCardIsValid(null));
	}
	
	@Test
	public void testEmptyString() {
		assertFalse(EZShop.checkCreditCardIsValid(""));
	}
	
	@Test
	public void testNotEmptyInvalidString() {
		assertFalse(EZShop.checkCreditCardIsValid("afv42"));
	}
	
	@Test
	public void testNotEmptyValidString() {
		assertTrue(EZShop.checkCreditCardIsValid("79927398713"));
	}
	
	@Test
	public void testSingleIteration() {
		assertFalse(EZShop.checkCreditCardIsValid("3"));
	}
}
