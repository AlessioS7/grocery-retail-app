package it.polito.ezshop.integrationTests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidLocationException;
import it.polito.ezshop.exceptions.InvalidOrderIdException;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidPaymentException;
import it.polito.ezshop.exceptions.InvalidPricePerUnitException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidProductDescriptionException;
import it.polito.ezshop.exceptions.InvalidProductIdException;
import it.polito.ezshop.exceptions.InvalidQuantityException;
import it.polito.ezshop.exceptions.InvalidRFIDException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidTransactionIdException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class EZShopRFIDSaleTransactionMethodsTest {

    EZShop es;
    //LinkedList<String> users;
	int Tid; // transaction ID used in many tests
	int Rid; // commonly used return transaction id
	
    @Before
    public void init() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException, InvalidProductIdException, InvalidLocationException, InvalidQuantityException, InvalidOrderIdException, InvalidRFIDException {
    	int Pid, Pid2, Oid, Oid2;
    	
        es = new EZShop();
        es.reset();
        es.createUser("admin", "admin", "Administrator");
        es.createUser("cashier", "cashier", "Cashier");
        es.createUser("manager", "manager", "ShopManager");
        es.login("admin",  "admin");
		// Product 1 creation
        Pid = es.createProductType("Test Item 1", "012345678905", 4.32, "test!");
        es.updatePosition(Pid, "1-asd-05");
        //es.updateQuantity(Pid, 5);
        Oid = es.issueOrder("012345678905", 5, 3.60);
        es.recordBalanceUpdate(100.00);
        es.payOrder(Oid);
        es.recordOrderArrivalRFID(Oid, "012345670000");
		// Product 2 creation
		Pid2 = es.createProductType("Test Item 2", "123456789012", 4.32, "test2!");
        es.updatePosition(Pid2, "1-bsd-05");
        Oid2 = es.issueOrder("123456789012", 5, 3.60);
        es.recordBalanceUpdate(100.00);
        es.payOrder(Oid2);
        es.recordOrderArrivalRFID(Oid2, "012345670010");
        //users = new LinkedList<String>();
        //users.add("admin");
        //users.add("cashier");
        //users.add("manager");
        //users.add("invalidUser");
        Tid = -1;
        Rid = -1;
    }

    @After
    public void cleanup() throws InvalidTransactionIdException, UnauthorizedException, InvalidUsernameException, InvalidPasswordException {
        es.logout();
        if(Tid > 0) {
        	es.login("admin", "admin");
        	es.endSaleTransaction(Tid);
            es.logout();
        }
        if(Rid > 0) {
        	es.login("admin", "admin");
        	es.endReturnTransaction(Rid, true);
            es.logout();
        }
    }
	
	
    // ==== addProductToSaleRFID ====
    
	@Test
	public void test_addProductToSaleRFID_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.addProductToSaleRFID(Tid, "012345670002"));
	}

	@Test
	public void test_addProductToSaleRFID_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.addProductToSaleRFID(null, "012345670002"));
	}

	@Test
	public void test_addProductToSaleRFID_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.addProductToSaleRFID( 0, "012345670002"));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.addProductToSaleRFID(-1, "012345670002"));
	}

	@Test
	public void test_addProductToSaleRFID_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		assertFalse("inexistent transaction should fail", es.addProductToSaleRFID(10, "012345670002"));
	}

	@Test
	public void test_addProductToSaleRFID_05_nullRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("null RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, null));
	}

	@Test
	public void test_addProductToSaleRFID_06_emptyRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("empty RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, ""));
	}

	@Test
	public void test_addProductToSaleRFID_07_invalidRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		Tid = es.startSaleTransaction();
		assertThrows("short RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "01234567890"));
		assertThrows("long RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "0123456789012"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "012345_78901"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "A01234567890"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "01234567890z"));
	}

	@Test
	public void test_addProductToSaleRFID_08_nonexistentRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
		assertFalse("inexistent product should fail", es.addProductToSaleRFID(Tid, "012345670005"));
	}

	@Test
	public void test_addProductToSaleRFID_09_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
		
		assertTrue("inserting 1 should be ok (4 left)", es.addProductToSaleRFID(Tid, "012345670000"));
		assertTrue("remaining amount of product should be 4", es.getProductTypeByBarCode("012345678905").getQuantity() == 4);
		
		assertTrue("inserting 1 should be ok (3 left)", es.addProductToSaleRFID(Tid, "012345670004"));
		assertTrue("remaining amount of product should be 3", es.getProductTypeByBarCode("012345678905").getQuantity() == 3);
		
		assertTrue("inserting 1 should be ok (2 left)", es.addProductToSaleRFID(Tid, "012345670001"));
		assertTrue("remaining amount of product should be 2", es.getProductTypeByBarCode("012345678905").getQuantity() == 2);
		
		assertTrue("inserting 1 should be ok (1 left)", es.addProductToSaleRFID(Tid, "012345670002"));
		assertTrue("remaining amount of product should be 1", es.getProductTypeByBarCode("012345678905").getQuantity() == 1);
		
		assertTrue("inserting 1 should be ok (0 left)", es.addProductToSaleRFID(Tid, "012345670003"));
		assertTrue("remaining amount of product should be 0", es.getProductTypeByBarCode("012345678905").getQuantity() == 0);
		
		
		es.endSaleTransaction(Tid);
		assertEquals("Transaction total should be 21.60", 21.60, es.getSaleTransaction(Tid).getPrice(), 0.001);
	}
	
	
    // ==== deleteProductFromSaleRFID ====
    
	@Test
	public void test_deleteProductFromSaleRFID_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670002");
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.deleteProductFromSaleRFID(Tid, "012345670002"));
	}

	@Test
	public void test_deleteProductFromSaleRFID_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSaleRFID(null, "012345670002"));
	}

	@Test
	public void test_deleteProductFromSaleRFID_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSaleRFID( 0, "012345670002"));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.deleteProductFromSaleRFID(-1, "012345670002"));
	}

	@Test
	public void test_deleteProductFromSaleRFID_04_inexistentTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		assertFalse("inexistent transaction should fail", es.deleteProductFromSaleRFID(10, "012345670002"));
	}

	@Test
	public void test_deleteProductFromSaleRFID_05_nullRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670002");
		assertThrows("null product code should fail", InvalidRFIDException.class, ()->es.deleteProductFromSaleRFID(Tid, null));
	}

	@Test
	public void test_deleteProductFromSaleRFID_06_emptyRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670002");
		assertThrows("empty product code should fail", InvalidRFIDException.class, ()->es.deleteProductFromSaleRFID(Tid, ""));
	}

	@Test
	public void test_deleteProductFromSaleRFID_07_invalidRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670002");
		assertThrows("short RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "01234567890"));
		assertThrows("long RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "0123456789012"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "012345_78901"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "A01234567890"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(Tid, "01234567890z"));
	}

	@Test
	public void test_deleteProductFromSaleRFID_08_nonexistentRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670002");
		assertFalse("inexistent product should fail", es.deleteProductFromSaleRFID(Tid, "012345670005"));
	}

//	@Test // This is not currently required nor supported
//	public void test_deleteProductFromSaleRFID_10_notInSale() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
//		Tid = es.startSaleTransaction();
//        es.addProductToSaleRFID(Tid, "012345670002");
//		assertFalse("deleting items not in sale should fail", es.deleteProductFromSaleRFID(Tid, "012345670003"));
//	}

	@Test
	public void test_deleteProductFromSaleRFID_09_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
        es.addProductToSaleRFID(Tid, "012345670000");
        es.addProductToSaleRFID(Tid, "012345670001");
        es.addProductToSaleRFID(Tid, "012345670002");
        es.addProductToSaleRFID(Tid, "012345670003");
        es.addProductToSaleRFID(Tid, "012345670004");
        
		assertTrue("deleting 1 should be ok (4 left)", es.deleteProductFromSaleRFID(Tid, "012345670003"));
		assertEquals("remaining amount of product should be 1", 1, (int) es.getProductTypeByBarCode("012345678905").getQuantity());
		
		assertTrue("deleting 1 should be ok (3 left)", es.deleteProductFromSaleRFID(Tid, "012345670000"));
		assertEquals("remaining amount of product should be 2", 2, (int) es.getProductTypeByBarCode("012345678905").getQuantity());
		
		assertTrue("deleting 1 should be ok (2 left)", es.deleteProductFromSaleRFID(Tid, "012345670004"));
		assertEquals("remaining amount of product should be 3", 3, (int) es.getProductTypeByBarCode("012345678905").getQuantity());
		
		assertTrue("deleting 1 should be ok (1 left)", es.deleteProductFromSaleRFID(Tid, "012345670001"));
		assertEquals("remaining amount of product should be 4", 4, (int) es.getProductTypeByBarCode("012345678905").getQuantity());
		
		assertTrue("deleting 1 should be ok (0 left)", es.deleteProductFromSaleRFID(Tid, "012345670002"));
		assertEquals("remaining amount of product should be 5", 5, (int) es.getProductTypeByBarCode("012345678905").getQuantity());
		
		es.endSaleTransaction(Tid);
		assertEquals("Transaction total should be 0.00", 0.00, es.getSaleTransaction(Tid).getPrice(), 0.001);
	}
	
	// ==== returnProductRFID ====
	
	@Test
	public void test_returnProductRFID_01_unauthUser() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		es.logout();
		assertThrows("No user should fail", UnauthorizedException.class, ()->es.returnProductRFID(Rid, "012345670002"));
	}
	
	@Test
	public void test_returnProductRFID_02_nullTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("null transaction should fail", InvalidTransactionIdException.class, ()->es.returnProductRFID(null, "012345670002"));
	}

	@Test
	public void test_returnProductRFID_03_invalidTransaction() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException {
		assertThrows("transaction ID = 0 should fail", InvalidTransactionIdException.class, ()->es.returnProductRFID( 0, "012345670002"));
		assertThrows("transaction ID < 0 should fail", InvalidTransactionIdException.class, ()->es.returnProductRFID(-1, "012345670002"));
	}
	
	@Test
	public void test_returnProductRFID_04_invalidRFID() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
		es.addProductToSaleRFID(Tid, "012345670002");
		es.endSaleTransaction(Tid);
        try {
			es.receiveCashPayment(Tid, 30.00);
		} catch (InvalidPaymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Rid = es.startReturnTransaction(Tid);
		assertThrows("short RFID should fail", InvalidRFIDException.class, ()->es.returnProductRFID(Rid, "01234567890"));
		assertThrows("long RFID should fail", InvalidRFIDException.class, ()->es.returnProductRFID(Rid, "0123456789012"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.returnProductRFID(Rid, "012345_78901"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.returnProductRFID(Rid, "A01234567890"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.returnProductRFID(Rid, "01234567890z"));
	}
	
	@Test
	public void test_returnProductRFID_05_successful() throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException, InvalidRFIDException {
		Tid = es.startSaleTransaction();
		es.addProductToSaleRFID(Tid, "012345670002");
		es.endSaleTransaction(Tid);
        try {
			es.receiveCashPayment(Tid, 30.00);
		} catch (InvalidPaymentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Rid = es.startReturnTransaction(Tid);
	   /*
		* @return  true if the operation is successful
		*          false   if the the product to be returned does not exists,
		*                  if it was not in the transaction,
		*                  if the transaction does not exist
		*/
		int Tid2 = es.startSaleTransaction();
		es.addProductToSaleRFID(Tid, "012345670001");
		assertFalse("The product should not exist", es.returnProductRFID(Tid, "012345670005"));
		assertFalse("The product should not be in the transaction", es.returnProductRFID(Tid, "012345670010"));
		assertFalse("The transaction should not exists", es.returnProductRFID(Tid2, "012345670002"));
		assertTrue("The return is successful", es.returnProductRFID(Tid, "012345670002"));
	}
}
