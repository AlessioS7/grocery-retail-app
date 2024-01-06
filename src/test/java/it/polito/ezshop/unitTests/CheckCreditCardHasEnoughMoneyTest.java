package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.data.EZShop;

public class CheckCreditCardHasEnoughMoneyTest {

	@Test
	public void testNullCreditCard() {
		assertFalse(EZShop.checkCreditCardHasEnoughMoney(null, 150.0));
	}
	
	@Test
	public void testEmptyCreditCardValue() {
		assertFalse(EZShop.checkCreditCardHasEnoughMoney("", 150.0));
	}
	
	@Test
	public void testNotEmptyMissingCreditCard() {
		assertFalse(EZShop.checkCreditCardHasEnoughMoney("4716258049958645", 0.0));
	}
	
	@Test
	public void testNotEmptyValidCreditCard() {
		assertTrue(EZShop.checkCreditCardHasEnoughMoney("5100293991053009", 9.00));
	}
	
	@Test
	public void testNotEmptyValidCreditCardNotEnoughMoney() {
		assertFalse(EZShop.checkCreditCardHasEnoughMoney("5100293991053009", 20.00));
	}
}
