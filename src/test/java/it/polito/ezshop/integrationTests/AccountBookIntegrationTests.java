package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.model.AccountBook;
import it.polito.ezshop.model.EZBalanceOperation;

public class AccountBookIntegrationTests {
	
	AccountBook ab;

    @Before
    public void init() {
        ab = new AccountBook();
        ab.addBalanceOperation(new EZBalanceOperation(1, LocalDate.now().minusDays(3), 50, "CREDIT"));
        ab.addBalanceOperation(new EZBalanceOperation(2, LocalDate.now(), 20, "DEBIT"));
        ab.addBalanceOperation(new EZBalanceOperation(3, LocalDate.now().plusDays(3), 30, "CREDIT"));
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    
	// getIdOfNextBalanceOperation
	
	@Test
	public void getIdOfNextBalanceOperationTest() {
	assertTrue(ab.getIdOfNextBalanceOperation() == 4);
	}

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // addBalanceOperation
    
    @Test
    public void testAddBalanceOperationInvalidType() {
        assertFalse(ab.addBalanceOperation(new EZBalanceOperation(4, LocalDate.now(), 100, "notAType")));
    }
    
    @Test
    public void testAddBalanceOperationCreditType() {
        assertTrue(ab.addBalanceOperation(new EZBalanceOperation(4, LocalDate.now(), 100, "CREDIT")));
    }
    
    @Test
    public void testAddBalanceOperationDebitType() {
        assertTrue(ab.addBalanceOperation(new EZBalanceOperation(4, LocalDate.now(), 100, "DEBIT")));
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    // getOperationBetween
    
    @Test
    public void testGetOperationBetweenNullList() {
        assertTrue(ab.getOperationBetween(null, null).size() == ab.getOperations().size());
    }
    
    @Test
    public void testGetOperationBetweenNullFrom() {
        assertTrue(ab.getOperationBetween(null, LocalDate.now()).size() == 2);
    }
    
    @Test
    public void testGetOperationBetweenNullTo() {
        assertTrue(ab.getOperationBetween(LocalDate.now(), null).size() == 2);
    }
    
    @Test
    public void testGetOperationBetweenTwoDates() {
        assertTrue(ab.getOperationBetween(LocalDate.now().minusDays(7), LocalDate.now().plusDays(7)).size() == 3);
    }
    
    @Test
    public void testGetOperationBetweenTwoInvertedDates() {
        assertTrue(ab.getOperationBetween(LocalDate.now().plusDays(7), LocalDate.now().minusDays(7)).size() == 3);
    }
}
