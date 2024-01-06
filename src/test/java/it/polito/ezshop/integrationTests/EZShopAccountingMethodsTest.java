package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidPricePerUnitException;
import it.polito.ezshop.exceptions.InvalidProductCodeException;
import it.polito.ezshop.exceptions.InvalidProductDescriptionException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;

public class EZShopAccountingMethodsTest {
	
	EZShop es;

    @Before
    public void init() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException {
        es = new EZShop();
        es.reset();
        es.createUser("admin", "admin", "Administrator");
        es.createUser("cashier", "cashier", "Cashier");
        es.createUser("manager", "manager", "ShopManager");

    }

    @After
    public void cleanup() {
        es.logout();
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	// recordBalanceUpdate
	
    @Test
    public void testRecordBalanceUpdateUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class,
                () -> es.recordBalanceUpdate(20.5));
    }
    
    @Test
    public void testRecordBalanceUpdateUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class,
                () -> es.recordBalanceUpdate(20.5));
    }
    
    @Test
    public void testRecordBalanceUpdateNegativeBalance()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.recordBalanceUpdate(-50));
    }
    
    @Test
    public void testRecordBalanceUpdateCredit()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertTrue(es.recordBalanceUpdate(100));
    }
    
    @Test
    public void testRecordBalanceUpdateDebit()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        es.recordBalanceUpdate(100);
        assertTrue(es.recordBalanceUpdate(-50));
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	// getCreditsAndDebits
    
    @Test
    public void testGetCreditsAndDebitsUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class,
                () -> es.getCreditsAndDebits(null, null));
    }
    
    @Test
    public void testGetCreditsAndDebitsUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class,
                () -> es.getCreditsAndDebits(null, null));
    }
    
    @Test
    public void testGetCreditsAndDebitsSuccess()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        
        es.recordBalanceUpdate(100);
        assertTrue(es.getCreditsAndDebits(null, null).size() != 0);
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	// computeBalance
    
    @Test
    public void testComputeBalanceUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class,
                () -> es.computeBalance());
    }
    
    @Test
    public void testComputeBalanceUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class,
                () -> es.computeBalance());
    }
    
    @Test
    public void testComputeBalanceSuccess()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        
        es.recordBalanceUpdate(100);
        assertTrue(es.computeBalance() != 0);
    }
}
