package it.polito.ezshop.integrationTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidCreditCardException;
import it.polito.ezshop.exceptions.InvalidDiscountRateException;
import it.polito.ezshop.exceptions.InvalidLocationException;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidPaymentException;
import it.polito.ezshop.exceptions.InvalidPricePerUnitException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidProductDescriptionException;
import it.polito.ezshop.exceptions.InvalidProductIdException;
import it.polito.ezshop.exceptions.InvalidQuantityException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class EZShopReturnTransactionMethodsTest {

    EZShop es;
    //LinkedList<String> users;
	int Tid; // closed transaction ID used in many tests
	int Rid; // commonly used return transaction id

    @Before
    public void init() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException, InvalidProductIdException, InvalidLocationException, InvalidTransactionIdException, InvalidQuantityException, InvalidPaymentException {
    	int Pid;
    	
        es = new EZShop();
        es.reset();
        es.createUser("admin", "admin", "Administrator");
        es.createUser("cashier", "cashier", "Cashier");
        es.createUser("manager", "manager", "ShopManager");
        es.login("admin",  "admin");
        Pid = es.createProductType("Test Item 1", "012345678905", 4.32, "test!");
        es.updatePosition(Pid, "1-asd-05");
        es.updateQuantity(Pid, 5);
        
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
        es.receiveCashPayment(Tid, 30.00);
        //users = new LinkedList<String>();
        //users.add("admin");
        //users.add("cashier");
        //users.add("manager");
        //users.add("invalidUser");
        Rid = -1;
    }

    @After
    public void cleanup() throws InvalidTransactionIdException, UnauthorizedException, InvalidUsernameException, InvalidPasswordException {
        es.logout();
        if(Rid > 0) {
        	es.login("admin", "admin");
        	es.endReturnTransaction(Rid, true);
            es.logout();
        }
    }
    
    
	//==== startReturnTransaction ====

	@Test
	public void test_startReturnTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.startReturnTransaction(Tid));
	}

	@Test
	public void test_startReturnTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.startReturnTransaction(null));
	}

	@Test
	public void test_startReturnTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("Negative transaction should fail", InvalidTransactionIdException.class, ()->es.startReturnTransaction(-1));
		assertThrows("Zero transaction should fail", InvalidTransactionIdException.class, ()->es.startReturnTransaction(0));
	}

	@Test
	public void test_startReturnTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertTrue("inexistent transaction should fail", es.startReturnTransaction(10) == -1);
	}
	
	@Test
	public void test_startReturnTransaction_05_success() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		assertTrue("transaction was not opened", es.startReturnTransaction(Tid) > 0);
	}
	
	
    // ==== returnProduct ====
    
	@Test
	public void test_returnProduct_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.returnProduct(Rid, "012345678905", 1));
	}

	@Test
	public void test_returnProduct_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.returnProduct(null, "012345678905", 1));
	}

	@Test
	public void test_returnProduct_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.returnProduct( 0, "012345678905", 1));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.returnProduct(-1, "012345678905", 1));
	}

	@Test
	public void test_returnProduct_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertFalse("inexistent transaction should fail", es.returnProduct(10, "012345678905", 1));
	}

	@Test
	public void test_returnProduct_05_nullProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertThrows("null product code should fail", InvalidProductCodeException.class, ()->es.returnProduct(Rid, null, 1));
	}

	@Test
	public void test_returnProduct_06_emptyProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertThrows("empty product code should fail", InvalidProductCodeException.class, ()->es.returnProduct(Rid, "", 1));
	}

	@Test
	public void test_returnProduct_07_invalidProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertThrows("invalid product barcode should fail", InvalidProductCodeException.class, ()->es.returnProduct(Rid, "012345678906", 1));
	}

	@Test
	public void test_returnProduct_08_nonexistentProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertFalse("inexistent product should fail", es.returnProduct(Rid, "00012345678905", 1));
	}

	@Test
	public void test_returnProduct_09_productNotInSale() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidProductIdException, InvalidLocationException, InvalidProductDescriptionException, InvalidPricePerUnitException {
		int Pid;
		
        Pid = es.createProductType("Test Item 2 not in transaction", "000000000109", 4.32, "test!");
        es.updatePosition(Pid, "1-asd-06");
        es.updateQuantity(Pid, 1); // existing product but not in the sale of the current return
        
		Rid = es.startReturnTransaction(Tid);
		assertFalse("inexistent product should fail", es.returnProduct(Rid, "000000000109", 1));
	}

	@Test
	public void test_returnProduct_10_invalidQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertThrows("negative qty should fail", InvalidQuantityException.class, ()->es.returnProduct(Rid, "012345678905", -1));
		assertThrows("zero qty should fail", InvalidQuantityException.class, ()->es.returnProduct(Rid, "012345678905", 0));
	}

	@Test
	public void test_returnProduct_11_tooBigQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertFalse("returning more items than present should fail", es.returnProduct(Rid, "012345678905", 6));
	}

	@Test
	public void test_returnProduct_12_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
        
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
//		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1); // not like addProductToSale, qty updated only at the end 
		
//		assertTrue("returning 0 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 0)); // not like addProductToSale, zero is not valid
//		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1);
		
		assertTrue("returning 4 should be ok (0 left)", es.returnProduct(Rid, "012345678905", 4));
//		assertTrue("remaining amount of product should be 5", es.getProductTypeByBarCode("012345678905").getQuantity() == 5); // not like addProductToSale, qty updated only at the end
		
		//es.endSaleTransaction(Tid);
		//assertEquals("Transaction total should be 0.00", 0.00, es.getSaleTransaction(Tid).getPrice(), 0.001);
	}
	
	
	
    // ==== endReturnTransaction ====
    
	@Test
	public void test_endReturnTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.endReturnTransaction(Rid, true));
	}

	@Test
	public void test_endReturnTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.endReturnTransaction(null, true));
	}

	@Test
	public void test_endReturnTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.endReturnTransaction( 0, true));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.endReturnTransaction(-1, true));
	}

	@Test
	public void test_endReturnTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertFalse("inexistent transaction should fail", es.endReturnTransaction(10, true));
	}
	
	@Test
	public void test_endReturnTransaction_05_rollback() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
        
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		assertTrue("rollback transaction failed", es.endReturnTransaction(Rid, false));
		assertTrue("remaining amount of product should be 0", es.getProductTypeByBarCode("012345678905").getQuantity() == 0); // rollback
		assertEquals("Transaction total should be 21.60", 21.60, es.getSaleTransaction(Tid).getPrice(), 0.001);
		//assertEquals("App balance should be 21.60", 21.60, es.computeBalance(), 0.001); // balance updated only after return*Payment()
		Rid = 0; // do not end transaction again in @After
	}
	
	@Test
	public void test_endReturnTransaction_06_commit() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
        
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		assertTrue("commit transaction failed", es.endReturnTransaction(Rid, true));
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1); // rollback
		assertEquals("Transaction total should be 17.28", 17.28, es.getSaleTransaction(Tid).getPrice(), 0.001);
		//assertEquals("App balance should be 17.28", 17.28, es.computeBalance(), 0.001); // balance updated only after return*Payment()
		
		Rid = es.startReturnTransaction(Tid);
		
		assertTrue("returning 4 should be ok (0 left)", es.returnProduct(Rid, "012345678905", 4));
		assertTrue("commit transaction failed", es.endReturnTransaction(Rid, true));
		assertTrue("remaining amount of product should be 5", es.getProductTypeByBarCode("012345678905").getQuantity() == 5); // rollback
		assertEquals("Transaction total should be 0.00", 0.00, es.getSaleTransaction(Tid).getPrice(), 0.001);
		//assertEquals("App balance should be 0.00", 0.00, es.computeBalance(), 0.001); // balance updated only after return*Payment()
		
		Rid = 0; // do not end transaction again in @After
	}
	
	
    // ==== deleteReturnTransaction ====
    
	@Test
	public void test_deleteReturnTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		es.returnProduct(Rid, "012345678905", 1);
		es.endReturnTransaction(Rid, true);
		es.logout();
		
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.deleteReturnTransaction(Rid));
		
		Rid = 0;
	}

	@Test
	public void test_deleteReturnTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.deleteReturnTransaction(null));
	}

	@Test
	public void test_deleteReturnTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.deleteReturnTransaction( 0));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.deleteReturnTransaction(-1));
	}

	@Test
	public void test_deleteReturnTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertFalse("inexistent transaction should fail", es.deleteReturnTransaction(10));
	}
	
	@Test
	public void test_deleteReturnTransaction_05_openFail() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		
		assertFalse("delete transaction should fail", es.deleteReturnTransaction(Rid));
		
		assertTrue("commit transaction should be ok", es.endReturnTransaction(Rid, true)); // transaction should still exist 
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1); // transaction result should be observable 
		assertEquals("Transaction total should be 17.28", 17.28, es.getSaleTransaction(Tid).getPrice(), 0.001);
		
		Rid = 0; // do not end transaction again in @After
	}
	
	@Test
	public void test_deleteReturnTransaction_06_rolledBackFail() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		assertTrue("commit transaction should be ok", es.endReturnTransaction(Rid, false)); // rollback 
		
		assertFalse("delete transaction should fail", es.deleteReturnTransaction(Rid));
		
		assertTrue("remaining amount of product should be 0", es.getProductTypeByBarCode("012345678905").getQuantity() == 0); // transaction result should be observable 
		assertEquals("Transaction total should be 21.60", 21.60, es.getSaleTransaction(Tid).getPrice(), 0.001);
		
		Rid = 0; // do not end transaction again in @After
	}
	
	@Test
	public void test_deleteReturnTransaction_07_committedSuccess() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		assertTrue("commit transaction failed", es.endReturnTransaction(Rid, true));
		
		assertTrue("delete transaction failed", es.deleteReturnTransaction(Rid));
		
		assertTrue("remaining amount of product should be 0", es.getProductTypeByBarCode("012345678905").getQuantity() == 0);
		assertEquals("Transaction total should be 21.60", 21.60, es.getSaleTransaction(Tid).getPrice(), 0.001);
		
		Rid = 0; // do not end transaction again in @After
	}
	
	@Test
	public void test_deleteReturnTransaction_08_paidBackFail() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		assertTrue("returning 1 should be ok (4 left)", es.returnProduct(Rid, "012345678905", 1));
		assertTrue("commit transaction failed", es.endReturnTransaction(Rid, true));
		assertEquals("should return 4.32", 4.32, es.returnCashPayment(Rid), 0.001);
		
		assertFalse("delete transaction failed", es.deleteReturnTransaction(Rid));
		
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1);
		assertEquals("Transaction total after return should be 17.28", 17.28, es.getSaleTransaction(Tid).getPrice(), 0.001);
		
		Rid = 0; // do not end transaction again in @After
	}

	// ==== returnCashPayment ====

    @Test
	public void testReturnCashPaymentInvalidNullReturnId() {
		assertThrows("No valid returnId", InvalidTransactionIdException.class, ()->es.returnCashPayment(null));
	}

	@Test
	public void testReturnCashPaymentInvalidReturnId() {
		assertThrows("No valid returnId", InvalidTransactionIdException.class, ()->es.returnCashPayment(-1));
	}
	
	@Test
	public void testReturnCashPaymentUnauthorized() {
		es.logout();
		assertThrows("No authorized user", UnauthorizedException.class, ()->es.returnCashPayment(1));
	}
	
	@Test
	public void testReturnCashPaymentNotExistingReturnTransaction() throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
		assertTrue(-1 == es.returnCashPayment(66));
	}
	
	@Test
	public void testReturnCashPaymentSuccess() throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		es.returnProduct(Rid, "012345678905", 1);
		es.endReturnTransaction(Rid, true);
		assertTrue(es.returnCashPayment(Rid) > 0);
	}

	// ==== returnCreditCardPayment ====
    
	@Test
	public void testReturnCreditCardPaymentInvalidReturnId() {
		assertThrows("No valid returnId", InvalidTransactionIdException.class, ()->es.returnCreditCardPayment(-1, "79927398713"));
	}
	
	@Test
	public void testReturnCreditCardPaymentUnauthorized() {
		es.logout();
		assertThrows("No authorized user", UnauthorizedException.class, ()->es.returnCreditCardPayment(1, "79927398713"));
	}
	
	@Test
	public void testReturnCreditCardPaymentInvalidCreditCard() {
		assertThrows("No valid credit card", InvalidCreditCardException.class, ()->es.returnCreditCardPayment(1, "ABC123"));
	}
	
	
	@Test
	public void testReturnCreditCardPaymentNotExistingReturnTransaction() throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {
		assertTrue(-1 == es.returnCreditCardPayment(7, "79927398713"));
	}
	
	@Test
	public void testReturnCreditCardPaymentSuccess() throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException, InvalidProductCodeException, InvalidQuantityException {
		Rid = es.startReturnTransaction(Tid);
		es.returnProduct(Rid, "012345678905", 1);
		es.endReturnTransaction(Rid, true);
		assertTrue(es.returnCreditCardPayment(Rid, "79927398713") > 0);
	}
}
