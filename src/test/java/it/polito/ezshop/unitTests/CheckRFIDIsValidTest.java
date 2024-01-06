package it.polito.ezshop.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidLocationException;
import it.polito.ezshop.exceptions.InvalidOrderIdException;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidPricePerUnitException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidProductDescriptionException;
import it.polito.ezshop.exceptions.InvalidProductIdException;
import it.polito.ezshop.exceptions.InvalidQuantityException;
import it.polito.ezshop.exceptions.InvalidRFIDException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class CheckRFIDIsValidTest {

	// ==== CheckRFIDIsValid() ====
	
	@Test
	public void test01_nullRFID() {
		assertFalse("checkRFIDIsValid(null) should be false", EZShop.checkRFIDIsValid(null));
	}

	@Test
	public void test02_emptyRFID() {
		assertFalse("checkRFIDIsValid(\"\") should be false", EZShop.checkRFIDIsValid(""));
	}

	@Test
	public void test03_WrongLengthRFID() {
		assertFalse("checkRFIDIsValid(\"01234567890\") should be false",
				EZShop.checkRFIDIsValid("01234567890"));
		assertFalse("checkRFIDIsValid(\"0123456789012\") should be false",
				EZShop.checkRFIDIsValid("0123456789012"));
	}

	@Test
	public void test04_LettersInRFID() {
		assertFalse("checkRFIDIsValid(\"01234567890A\") should be false", // one letter at the end
				EZShop.checkRFIDIsValid("01234567890A"));
		assertFalse("checkRFIDIsValid(\"012345678901A\") should be false", // 12 numbers but then one letter
				EZShop.checkRFIDIsValid("012345678901A"));
		assertFalse("checkRFIDIsValid(\"a12345678901\") should be false", // one letter at the beginning
				EZShop.checkRFIDIsValid("a12345678901"));
		assertFalse("checkRFIDIsValid(\"01234_678901\") should be false", // one _ in the middle
				EZShop.checkRFIDIsValid("01234_678901"));
		assertFalse("checkRFIDIsValid(\"-01234567890\") should be false", // one - could be mistaken as sign
				EZShop.checkRFIDIsValid("-01234567890"));
	}

	@Test
	public void test05_correctRFID() {
		assertTrue("checkRFIDIsValid(\"012345678901\") should be correct",
				EZShop.checkRFIDIsValid("012345678901"));
	}
	

	// ==== checkRFIDIsUnique() ====

// checkRFIDIsUnique() is an internal only method, therefore it does not check the validity of the RFID passed. It is assumed that it is valid.

//	@Test
//	public void test01checkRFIDIsUnique_nullRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
//        EZShop es = new EZShop();
//        es.reset();
//        es.createUser("admin", "admin", "Administrator");
//        assertNotNull(es.login("admin", "admin"));
//        
//        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
//        assertNotEquals(new Integer(-1), productId);
//        assertTrue(es.recordBalanceUpdate(50.00)); // +50
//        
//        // First order
//        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
//        assertNotEquals(new Integer(-1), orderId); // -20
//        assertTrue(es.updatePosition(productId, "12-ab-34"));
//        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
//        
//		assertFalse("checkRFIDIsUnique(null) should be false", es.checkRFIDIsUnique(null));
//	}
//
//	@Test
//	public void test02checkRFIDIsUnique_emptyRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
//        EZShop es = new EZShop();
//        es.reset();
//        es.createUser("admin", "admin", "Administrator");
//        assertNotNull(es.login("admin", "admin"));
//        
//        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
//        assertNotEquals(new Integer(-1), productId);
//        assertTrue(es.recordBalanceUpdate(50.00)); // +50
//        
//        // First order
//        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
//        assertNotEquals(new Integer(-1), orderId); // -20
//        assertTrue(es.updatePosition(productId, "12-ab-34"));
//        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
//        
//		assertFalse("checkRFIDIsUnique(\"\") should be false", es.checkRFIDIsUnique(""));
//	}
//
//	@Test
//	public void test03checkRFIDIsUnique_WrongLengthRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
//        EZShop es = new EZShop();
//        es.reset();
//        es.createUser("admin", "admin", "Administrator");
//        assertNotNull(es.login("admin", "admin"));
//        
//        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
//        assertNotEquals(new Integer(-1), productId);
//        assertTrue(es.recordBalanceUpdate(50.00)); // +50
//        
//        // First order
//        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
//        assertNotEquals(new Integer(-1), orderId); // -20
//        assertTrue(es.updatePosition(productId, "12-ab-34"));
//        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
//        
//		assertFalse("checkRFIDIsUnique(\"01234567890\") should be false",
//				es.checkRFIDIsUnique("01234567890"));
//		assertFalse("checkRFIDIsUnique(\"0123456789012\") should be false",
//				es.checkRFIDIsUnique("0123456789012"));
//	}
//
//	@Test
//	public void test04checkRFIDIsUnique_LettersInRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
//        EZShop es = new EZShop();
//        es.reset();
//        es.createUser("admin", "admin", "Administrator");
//        assertNotNull(es.login("admin", "admin"));
//        
//        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
//        assertNotEquals(new Integer(-1), productId);
//        assertTrue(es.recordBalanceUpdate(50.00)); // +50
//        
//        // First order
//        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
//        assertNotEquals(new Integer(-1), orderId); // -20
//        assertTrue(es.updatePosition(productId, "12-ab-34"));
//        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
//        
//		assertFalse("checkRFIDIsUnique(\"01234567890A\") should be false", // one letter at the end
//				es.checkRFIDIsUnique("01234567890A"));
//		assertFalse("checkRFIDIsUnique(\"012345678901A\") should be false", // 12 numbers but then one letter
//				es.checkRFIDIsUnique("012345678901A"));
//		assertFalse("checkRFIDIsUnique(\"a12345678901\") should be false", // one letter at the beginning
//				es.checkRFIDIsUnique("a12345678901"));
//		assertFalse("checkRFIDIsUnique(\"01234_678901\") should be false", // one _ in the middle
//				es.checkRFIDIsUnique("01234_678901"));
//		assertFalse("checkRFIDIsUnique(\"-01234567890\") should be false", // one - could be mistaken as sign
//				es.checkRFIDIsUnique("-01234567890"));
//	}

	@Test
	public void test05checkRFIDIsUnique_correctUniqueRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
        EZShop es = new EZShop();
        es.reset();
        es.createUser("admin", "admin", "Administrator");
        assertNotNull(es.login("admin", "admin"));
        
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(50.00)); // +50
        
        // First order
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
        
		assertTrue("checkRFIDIsUnique(\"012345678901\") should be correct",
				es.checkRFIDIsUnique("012345678901"));
	}

	@Test
	public void test06checkRFIDIsUnique_correctNonUniqueRFID() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidOrderIdException, InvalidRFIDException {
        EZShop es = new EZShop();
        es.reset();
        es.createUser("admin", "admin", "Administrator");
        assertNotNull(es.login("admin", "admin"));
        
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(50.00)); // +50
        
        // First order
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
        
		assertFalse("checkRFIDIsUnique(\"012345670010\") should be non unique",
				es.checkRFIDIsUnique("012345670010"));
		assertFalse("checkRFIDIsUnique(\"012345670011\") should be non unique",
				es.checkRFIDIsUnique("012345670011"));
		assertFalse("checkRFIDIsUnique(\"012345670019\") should be non unique",
				es.checkRFIDIsUnique("012345670019"));
	}
}
