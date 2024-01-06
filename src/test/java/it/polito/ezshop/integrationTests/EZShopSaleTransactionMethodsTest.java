package it.polito.ezshop.integrationTests;

import static org.junit.Assert.*;

//import java.util.LinkedList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.SaleTransaction;
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
import it.polito.ezshop.model.EZSaleTransaction;

public class EZShopSaleTransactionMethodsTest {
    EZShop es;
    //LinkedList<String> users;
	int Tid; // transaction ID used in many tests

    @Before
    public void init() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException, InvalidProductIdException, InvalidLocationException {
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
        //users = new LinkedList<String>();
        //users.add("admin");
        //users.add("cashier");
        //users.add("manager");
        //users.add("invalidUser");
        Tid = -1;
    }

    @After
    public void cleanup() throws InvalidTransactionIdException, UnauthorizedException, InvalidUsernameException, InvalidPasswordException {
        es.logout();
        if(Tid > 0) {
        	es.login("admin", "admin");
        	es.endSaleTransaction(Tid);
            es.logout();
        }
    }
    
    
    // ==== startSaleTransaction ====
    
	@Test
	public void test_startSaleTransaction_01_unauthUser() throws UnauthorizedException, InvalidUsernameException, InvalidPasswordException {
		//for(String userName : users) {
		//	es.login(userName, userName);
		//	if(userName.equals("admin") || userName.equals("cashier") || userName.equals("manager")) {
		//		assertTrue("new transaction not started", es.startSaleTransaction() >= 0);
		//	} else {
		//		assertThrows("should throw when invalid user", UnauthorizedException.class, ()->es.startSaleTransaction());
		//	}
		//}
		es.logout();
		assertThrows("should throw when invalid user", UnauthorizedException.class, ()->es.startSaleTransaction());
	}
	
	@Test
	public void test_startSaleTransaction_02_validUser() throws UnauthorizedException, InvalidUsernameException, InvalidPasswordException {
		assertTrue("should return a valid (positive) sale transaction ID", es.startSaleTransaction() > 0);
	}

	@Test
	public void test_startSaleTransaction_03_twoSales() throws UnauthorizedException, InvalidUsernameException, InvalidPasswordException, InvalidTransactionIdException {
		Tid = es.startSaleTransaction();
		es.endSaleTransaction(Tid);
		assertTrue("should return a valid (positive) sale transaction ID", es.startSaleTransaction() > Tid);
	}
	
	
    // ==== endSaleTransaction ====
    
	@Test
	public void test_endSaleTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.endSaleTransaction(Tid));
	}

	@Test
	public void test_endSaleTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.endSaleTransaction(null));
	}

	@Test
	public void test_endSaleTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.endSaleTransaction( 0));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.endSaleTransaction(-1));
	}

	@Test
	public void test_endSaleTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertFalse("inexistent transaction should fail", es.endSaleTransaction(10));
	}
	
	@Test
	public void test_endSaleTransaction_05_success() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
		assertTrue("open transaction was not closed", es.endSaleTransaction(Tid));
		//assertEquals("App balance should be 21.60", 21.60, es.computeBalance(), 0.001); // not here: updated on payment
	}
	
	@Test
	public void test_endSaleTransaction_06_doubleClose() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidPaymentException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertTrue("open transaction was not closed", es.endSaleTransaction(Tid));
		assertFalse("closed transaction should fail", es.endSaleTransaction(Tid));
		
		es.receiveCashPayment(Tid, 30.00);
		assertFalse("paid transaction should fail", es.endSaleTransaction(Tid));
	}
	
	
    // ==== addProductToSale ====
    
	@Test
	public void test_addProductToSale_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.addProductToSale(Tid, "012345678905", 1));
	}

	@Test
	public void test_addProductToSale_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.addProductToSale(null, "012345678905", 1));
	}

	@Test
	public void test_addProductToSale_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.addProductToSale( 0, "012345678905", 1));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.addProductToSale(-1, "012345678905", 1));
	}

	@Test
	public void test_addProductToSale_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertFalse("inexistent transaction should fail", es.addProductToSale(10, "012345678905", 1));
	}

	@Test
	public void test_addProductToSale_05_nullProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("null product code should fail", InvalidProductCodeException.class, ()->es.addProductToSale(Tid, null, 1));
	}

	@Test
	public void test_addProductToSale_06_emptyProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("empty product code should fail", InvalidProductCodeException.class, ()->es.addProductToSale(Tid, "", 1));
	}

	@Test
	public void test_addProductToSale_07_invalidProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("invalid product barcode should fail", InvalidProductCodeException.class, ()->es.addProductToSale(Tid, "012345678906", 1));
	}

	@Test
	public void test_addProductToSale_08_nonexistentProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertFalse("inexistent product should fail", es.addProductToSale(Tid, "00012345678905", 1));
	}

	@Test
	public void test_addProductToSale_09_invalidQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("negative qty should fail", InvalidQuantityException.class, ()->es.addProductToSale(Tid, "012345678905", -1));
	}

	@Test
	public void test_addProductToSale_10_tooBigQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertFalse("inserting more items than in stock should fail", es.addProductToSale(Tid, "012345678905", 6));
	}

	@Test
	public void test_addProductToSale_11_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		
		assertTrue("inserting 1 should be ok (4 left)", es.addProductToSale(Tid, "012345678905", 1));
		assertTrue("remaining amount of product should be 4", es.getProductTypeByBarCode("012345678905").getQuantity() == 4);
		
		assertTrue("inserting 0 should be ok (4 left)", es.addProductToSale(Tid, "012345678905", 0));
		assertTrue("remaining amount of product should be 4", es.getProductTypeByBarCode("012345678905").getQuantity() == 4);
		
		assertTrue("inserting 4 should be ok (0 left)", es.addProductToSale(Tid, "012345678905", 4));
		assertTrue("remaining amount of product should be 0", es.getProductTypeByBarCode("012345678905").getQuantity() == 0);
		
		es.endSaleTransaction(Tid);
		assertEquals("Transaction total should be 21.60", 21.60, es.getSaleTransaction(Tid).getPrice(), 0.001);
	}
	
	
    // ==== deleteProductFromSale ====
    
	@Test
	public void test_deleteProductFromSale_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.deleteProductFromSale(Tid, "012345678905", 1));
	}

	@Test
	public void test_deleteProductFromSale_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSale(null, "012345678905", 1));
	}

	@Test
	public void test_deleteProductFromSale_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSale( 0, "012345678905", 1));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSale(-1, "012345678905", 1));
	}

	@Test
	public void test_deleteProductFromSale_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertFalse("inexistent transaction should fail", es.deleteProductFromSale(10, "012345678905", 1));
	}

	@Test
	public void test_deleteProductFromSale_05_nullProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("null product code should fail", InvalidProductCodeException.class, ()->es.deleteProductFromSale(Tid, null, 1));
	}

	@Test
	public void test_deleteProductFromSale_06_emptyProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("empty product code should fail", InvalidProductCodeException.class, ()->es.deleteProductFromSale(Tid, "", 1));
	}

	@Test
	public void test_deleteProductFromSale_07_invalidProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("invalid product barcode should fail", InvalidProductCodeException.class, ()->es.deleteProductFromSale(Tid, "012345678906", 1));
	}

	@Test
	public void test_deleteProductFromSale_08_nonexistentProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertFalse("inexistent product should fail", es.deleteProductFromSale(Tid, "00012345678905", 1));
	}

	@Test
	public void test_deleteProductFromSale_09_invalidQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("negative qty should fail", InvalidQuantityException.class, ()->es.deleteProductFromSale(Tid, "012345678905", -1));
	}

	@Test
	public void test_deleteProductFromSale_10_tooBigQuantity() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertFalse("deleting more items than present should fail", es.deleteProductFromSale(Tid, "012345678905", 6));
	}

	@Test
	public void test_deleteProductFromSale_11_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        
		assertTrue("deleting 1 should be ok (4 left)", es.deleteProductFromSale(Tid, "012345678905", 1));
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1);
		
		assertTrue("deleting 0 should be ok (4 left)", es.deleteProductFromSale(Tid, "012345678905", 0));
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1);
		
		assertTrue("deleting 4 should be ok (0 left)", es.deleteProductFromSale(Tid, "012345678905", 4));
		assertTrue("remaining amount of product should be 5", es.getProductTypeByBarCode("012345678905").getQuantity() == 5);
		
		es.endSaleTransaction(Tid);
		assertEquals("Transaction total should be 0.00", 0.00, es.getSaleTransaction(Tid).getPrice(), 0.001);
	}
	
	
    // ==== deleteSaleTransaction ====
    
	@Test
	public void test_deleteSaleTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.deleteSaleTransaction(Tid));
	}

	@Test
	public void test_deleteSaleTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.deleteSaleTransaction(null));
	}

	@Test
	public void test_deleteSaleTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.deleteSaleTransaction( 0));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.deleteSaleTransaction(-1));
	}

	@Test
	public void test_deleteSaleTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertFalse("inexistent transaction should fail", es.deleteSaleTransaction(10));
	}
	
	@Test
	public void test_deleteSaleTransaction_05_openDelete() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
		assertFalse("open transaction was wrongly deleted", es.deleteSaleTransaction(Tid));
	}
	
	@Test
	public void test_deleteSaleTransaction_06_closedDelete() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertTrue("closed transaction was not deleted", es.deleteSaleTransaction(Tid));
		//assertEquals("App balance should be 0.00", 0.00, es.computeBalance(), 0.001); // not here: balance only updated after payment
	}
	
	@Test
	public void test_deleteSaleTransaction_07_paidDelete() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
        es.receiveCashPayment(Tid, 30.00);
		assertFalse("paid transaction was wrongly deleted", es.deleteSaleTransaction(Tid));
	}
	
	
    // ==== getSaleTransaction ====
    
	@Test
	public void test_getSaleTransaction_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.getSaleTransaction(Tid));
	}

	@Test
	public void test_getSaleTransaction_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.getSaleTransaction(null));
	}

	@Test
	public void test_getSaleTransaction_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.getSaleTransaction( 0));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.getSaleTransaction(-1));
	}

	@Test
	public void test_getSaleTransaction_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertNull("inexistent transaction should fail", es.getSaleTransaction(10));
	}
	
	@Test
	public void test_getSaleTransaction_05_openGet() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertNull("open transaction was not deleted", es.getSaleTransaction(Tid));
		
	}
	
	@Test
	public void test_getSaleTransaction_06_closedGet() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		SaleTransaction T;
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
        
        T = es.getSaleTransaction(Tid);
		assertNotNull("closed transaction was not deleted", T);
		assertEquals("Discount should be 0.00", 0.00, T.getDiscountRate(), 0.001);
		assertEquals("Transaction total should be 21.60", 21.60, T.getPrice(), 0.001);
		assertTrue("Transaction Tid wrong", T.getTicketNumber() == Tid);
		assertTrue("Transaction should contain 1 item", T.getEntries().size() == 1);
		assertTrue("Transaction item wrong barcode", T.getEntries().get(0).getBarCode().equals("012345678905"));
		assertTrue("Transaction item wrong barcode", T.getEntries().get(0).getAmount() == 5);
		assertEquals("Transaction item wrong discount (not 0.00)", 0.00, T.getEntries().get(0).getDiscountRate(), 0.001);
		assertEquals("Transaction item wrong price (not 4.32)", 4.32, T.getEntries().get(0).getPricePerUnit(), 0.001);
		assertTrue("Transaction item wrong description", T.getEntries().get(0).getProductDescription().equals("Test Item 1"));
	}
	
	@Test
	public void test_getSaleTransaction_07_paidGet() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		SaleTransaction T;
		Tid = es.startSaleTransaction(); // redundant of _06, but useful for rapid debug
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
        es.receiveCashPayment(Tid, 30.00);
        
        T = es.getSaleTransaction(Tid);
		assertNotNull("closed transaction was not deleted", T);
		assertEquals ("Discount should be 0.00", 0.00, T.getDiscountRate(), 0.001);
		assertEquals ("Transaction total should be 21.60", 21.60, T.getPrice(), 0.001);
		assertTrue   ("Transaction Tid wrong", T.getTicketNumber() == Tid);
		assertTrue   ("Transaction should contain 1 item", T.getEntries().size() == 1);
		assertTrue   ("Transaction item wrong barcode", T.getEntries().get(0).getBarCode().equals("012345678905"));
		assertTrue   ("Transaction item wrong barcode", T.getEntries().get(0).getAmount() == 5);
		assertEquals ("Transaction item wrong discount (not 0.00)", 0.00, T.getEntries().get(0).getDiscountRate(), 0.001);
		assertEquals ("Transaction item wrong price (not 4.32)", 4.32, T.getEntries().get(0).getPricePerUnit(), 0.001);
		assertTrue   ("Transaction item wrong description", T.getEntries().get(0).getProductDescription().equals("Test Item 1"));
	}
	
	
    // ==== applyDiscountRateToProduct ====
    
	@Test
	public void test_applyDiscountRateToProduct_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.applyDiscountRateToProduct(Tid, "012345678905", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToProduct(null, "012345678905", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToProduct( 0, "012345678905", 0.10));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToProduct(-1, "012345678905", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertFalse("inexistent transaction should fail", es.applyDiscountRateToProduct(10, "012345678905", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_05_nullProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("null product code should fail", InvalidProductCodeException.class, ()->es.applyDiscountRateToProduct(Tid, null, 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_06_emptyProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("empty product code should fail", InvalidProductCodeException.class, ()->es.applyDiscountRateToProduct(Tid, "", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_07_invalidProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("invalid product barcode should fail", InvalidProductCodeException.class, ()->es.applyDiscountRateToProduct(Tid, "012345678906", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_08_nonexistentProduct() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertFalse("inexistent product should fail", es.applyDiscountRateToProduct(Tid, "00012345678905", 0.10));
	}

	@Test
	public void test_applyDiscountRateToProduct_09_invalidDiscountRate() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("negative discount should fail", InvalidDiscountRateException.class, ()->es.applyDiscountRateToProduct(Tid, "012345678905", -0.001)); // -0.1%
		assertThrows("discount 100% should fail", InvalidDiscountRateException.class, ()->es.applyDiscountRateToProduct(Tid, "012345678905", 1.0));    //  100%
	}

	@Test
	public void test_applyDiscountRateToProduct_10_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertTrue("discount  0% should be ok", es.applyDiscountRateToProduct(Tid, "012345678905", 0.0));
		assertTrue("discount 99% should be ok", es.applyDiscountRateToProduct(Tid, "012345678905", 0.99));
		assertTrue("discount 10% should be ok", es.applyDiscountRateToProduct(Tid, "012345678905", 0.10));
		
		es.endSaleTransaction(Tid);
		assertEquals("Transaction total with 10% disocunt on product should be 21.60 * 0.90 = 19.44", 19.44, es.getSaleTransaction(Tid).getPrice(), 0.01);
	}

    // ==== applyDiscountRateToSale ====
    
	@Test
	public void test_applyDiscountRateToSale_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.applyDiscountRateToSale(Tid, 0.10));
	}

	@Test
	public void test_applyDiscountRateToSale_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToSale(null, 0.10));
	}

	@Test
	public void test_applyDiscountRateToSale_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToSale( 0, 0.10));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.applyDiscountRateToSale(-1, 0.10));
	}

	@Test
	public void test_applyDiscountRateToSale_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertFalse("inexistent transaction should fail", es.applyDiscountRateToSale(10, 0.10));
	}

	@Test
	public void test_applyDiscountRateToSale_05_invalidDiscountRate() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertThrows("negative discount should fail", InvalidDiscountRateException.class, ()->es.applyDiscountRateToSale(Tid, -0.001)); // -0.1%
		assertThrows("discount 100% should fail", InvalidDiscountRateException.class, ()->es.applyDiscountRateToSale(Tid, 1.0));    //  100%
	}

	@Test
	public void test_applyDiscountRateToSale_06_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		assertTrue("discount 0% should be ok", es.applyDiscountRateToSale(Tid, 0.0));
		assertTrue("discount 99% should be ok", es.applyDiscountRateToSale(Tid, 0.99));
		assertTrue("discount 10% should be ok", es.applyDiscountRateToSale(Tid, 0.10));
		
		// Test proper discount (getSaleTransaction can work properly only after the transaction is closed)
		es.endSaleTransaction(Tid);
		assertEquals("discount should be set to 10%", 0.10, es.getSaleTransaction(Tid).getDiscountRate(), 0.001);
		assertEquals("Transaction total with 10% disocunt on product should be 21.60 * 0.90 = 19.44", 19.44, es.getSaleTransaction(Tid).getPrice(), 0.01);
		Tid = 0; // do not end transaction again in @After
	}

	@Test
	public void test_applyDiscountRateToSale_07_closed_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.endSaleTransaction(Tid);
		assertTrue("discount 10% should be ok", es.applyDiscountRateToSale(Tid, 0.10));
		assertEquals("discount should be set to 10%", 0.10, es.getSaleTransaction(Tid).getDiscountRate(), 0.001);
		assertEquals("Transaction total with 10% disocunt on product should be 21.60 * 0.90 = 19.44", 19.44, es.getSaleTransaction(Tid).getPrice(), 0.01);
		Tid = 0; // do not end transaction again in @After
	}
	
	
    // ==== computePointsForSale ====
    
	@Test
	public void test_computePointsForSale_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.computePointsForSale(Tid));
	}

	@Test
	public void test_computePointsForSale_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.computePointsForSale(null));
	}

	@Test
	public void test_computePointsForSale_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.computePointsForSale( 0));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.computePointsForSale(-1));
	}

	@Test
	public void test_computePointsForSale_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException {
		assertTrue("inexistent transaction should fail", es.computePointsForSale(10) == -1);
	}
	
	@Test
	public void test_computePointsForSale_05_okOpenTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		int Pid;
		
		Tid = es.startSaleTransaction();
		
		assertTrue("0.00 should give 0 points", es.computePointsForSale(Tid) == 0); // 0.00 -> 0 points
		
        es.addProductToSale(Tid, "012345678905", 5);
		assertTrue("21.60 should give 2 points", es.computePointsForSale(Tid) == 2); // 21.60 -> 2 points
		
        Pid = es.createProductType("Test Item 2", "000000000000", 8.39, "test!");
        es.updatePosition(Pid, "1-asd-06");
        es.updateQuantity(Pid, 5);
        es.addProductToSale(Tid, "000000000000", 1);
		assertTrue("29.99 should give 2 points", es.computePointsForSale(Tid) == 2); // 29.99 -> 2 points
		
        Pid = es.createProductType("Test Item 3", "000000000109", 0.01, "test!");
        es.updatePosition(Pid, "1-asd-07");
        es.updateQuantity(Pid, 5);
        es.addProductToSale(Tid, "000000000109", 1);
		assertTrue("30.00 should give 3 points", es.computePointsForSale(Tid) == 3); // 30.00 -> 2 points
		
		es.endSaleTransaction(Tid);
		assertTrue("30.00 should give 3 points after closing the transaction", es.computePointsForSale(Tid) == 3); // 30.00 -> 2 points
		
		es.receiveCashPayment(Tid, 30.00);
		assertTrue("30.00 should give 3 points after payment is received", es.computePointsForSale(Tid) == 3); // 30.00 -> 2 points
		Tid = 0; // do not end transaction again in @After
	}

	@Test
	public void test_computePointsForSale_06_okClosedTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction();
		es.addProductToSale(Tid, "012345678905", 5);
		es.endSaleTransaction(Tid);
		assertTrue("21.60 should give 2 points", es.computePointsForSale(Tid) == 2); // 21.60 -> 2 points
		Tid = 0; // do not end transaction again in @After
	}

	@Test
	public void test_computePointsForSale_06_okPayedTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException, InvalidQuantityException, InvalidProductDescriptionException, InvalidPricePerUnitException, InvalidProductIdException, InvalidLocationException, InvalidPaymentException {
		Tid = es.startSaleTransaction();
		es.addProductToSale(Tid, "012345678905", 5);
		es.endSaleTransaction(Tid);
		assertEquals(3.4, es.receiveCashPayment(Tid, 25), 0.001);
		assertTrue("21.60 should give 2 points", es.computePointsForSale(Tid) == 2); // 21.60 -> 2 points
		Tid = 0; // do not end transaction again in @After
	}

	// ==== receiveCashPayment ====

	@Test
	public void test_receiveCashPayment_01_InvalidNullId(){
		assertThrows(InvalidTransactionIdException.class, ()-> es.receiveCashPayment(null, 10.5) );
	}

	@Test
	public void test_receiveCashPayment_02_InvalidNegativeId(){
		assertThrows(InvalidTransactionIdException.class, ()-> es.receiveCashPayment(-1, 10.5) );
	}

	@Test
	public void test_receiveCashPayment_03_InvalidCash(){
		assertThrows(InvalidPaymentException.class, ()-> es.receiveCashPayment(1, -10.5) );
	}

	@Test
	public void test_receiveCashPayment_04_UnauthorizedNotLogged(){
		es.logout();
		assertThrows(UnauthorizedException.class, ()-> es.receiveCashPayment(1, 10.5) );
	}

	@Test
	public void test_receiveCashPayment_05_TransactionNotExists() throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException{
		assertTrue(es.receiveCashPayment(1, 10.5) == -1);
	}

	@Test
	public void test_receiveCashPayment_06_CashNotEnough() throws UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidPaymentException{
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertTrue(es.receiveCashPayment(Tid, 10.5) == -1);
	}

	@Test
	public void test_receiveCashPayment_07_Success() throws UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidPaymentException{
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertEquals(3.4, es.receiveCashPayment(Tid, 25), 0.001);
	}

	// ==== receiveCreditCardPayment ====

	@Test
	public void test_receiveCreditCardPayment_01_InvalidNullId(){
		assertThrows(InvalidTransactionIdException.class, ()-> es.receiveCreditCardPayment(null, "4485370086510891") );
	}

	@Test
	public void test_receiveCreditCardPayment_02_InvalidNegativeId(){
		assertThrows(InvalidTransactionIdException.class, ()-> es.receiveCreditCardPayment(-1, "4485370086510891") );
	}

	@Test
	public void test_receiveCreditCardPayment_03_InvalidNullCreditCard(){
		assertThrows(InvalidCreditCardException.class, ()-> es.receiveCreditCardPayment(1, null) );
	}

	@Test
	public void test_receiveCreditCardPayment_04_InvalidEmptyCreditCard(){
		assertThrows(InvalidCreditCardException.class, ()-> es.receiveCreditCardPayment(1, "") );
	}

	@Test
	public void test_receiveCreditCardPayment_05_InvalidAlphaCreditCard(){
		assertThrows(InvalidCreditCardException.class, ()-> es.receiveCreditCardPayment(1, "44B537O0865I0891") );
	}

	@Test
	public void test_receiveCreditCardPayment_06_UnauthorizedNotLogged(){
		es.logout();
		assertThrows(UnauthorizedException.class, ()-> es.receiveCreditCardPayment(1, "4485370086510891") );
	}

	@Test
	public void test_receiveCreditCardPayment_07_TransactionNotExists() throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException, InvalidCreditCardException{
		assertFalse(es.receiveCreditCardPayment(66, "4485370086510891"));
	}

	@Test
	public void test_receiveCreditCardPayment_08_CreditCardNotRegistered() throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException, InvalidCreditCardException, InvalidProductCodeException, InvalidQuantityException{
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertFalse(es.receiveCreditCardPayment(Tid, "1128396337392028"));
	}

	@Test
	public void test_receiveCreditCardPayment_08_CreditCardNotEnoughMoney() throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException, InvalidCreditCardException, InvalidProductCodeException, InvalidQuantityException{
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertFalse(es.receiveCreditCardPayment(Tid, "5100293991053009"));
	}

	@Test
	public void test_receiveCreditCardPayment_09_Success() throws UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidPaymentException, InvalidCreditCardException{
		Tid = es.startSaleTransaction();
        es.addProductToSale(Tid, "012345678905", 5);
        es.endSaleTransaction(Tid);
		assertTrue(es.receiveCreditCardPayment(Tid, "4485370086510891"));
	}
}
