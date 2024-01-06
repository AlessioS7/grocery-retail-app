package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.Customer;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;

public class EZShopCustomerMethodsTest {
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
	
	// checkCustomerNameIsUnique
    
    @Test
    public void testCheckCustomerNameIsUniqueFalse() throws InvalidUsernameException, 
    InvalidPasswordException, InvalidCustomerNameException, UnauthorizedException{
    	es.login("admin", "admin");
    	es.defineCustomer("Lillo");
    	assertFalse(es.checkCustomerNameIsUnique("Lillo"));
    }

    @Test
    public void testCheckCustomerNameIsUniqueTrue() throws InvalidUsernameException,
    InvalidPasswordException, InvalidCustomerNameException, UnauthorizedException {
    	es.login("admin", "admin");
    	es.defineCustomer("Lillo");
    	assertTrue(es.checkCustomerNameIsUnique("Marco"));
    }
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	// checkCustomerCardIsUnique
	
	@Test
	public void testCheckCustomerCardIsUniqueFalse() throws InvalidUsernameException, 
	InvalidPasswordException, InvalidCustomerNameException, UnauthorizedException,
	InvalidCustomerIdException, InvalidCustomerCardException{
	es.login("admin", "admin");
	int id = es.defineCustomer("Lillo");
	String cardCode = "0123456789";
	es.attachCardToCustomer(cardCode, id);
	assertFalse(es.checkCustomerCardIsUnique(cardCode));
	}
	
	@Test
	public void testCheckCustomerCardIsUniqueTrue() throws InvalidUsernameException, 
	InvalidPasswordException, InvalidCustomerNameException, UnauthorizedException,
	InvalidCustomerIdException, InvalidCustomerCardException{
	es.login("admin", "admin");
	int id = es.defineCustomer("Lillo");
	String cardCode = "0123456789";
	es.attachCardToCustomer(cardCode, id);
	assertTrue(es.checkCustomerCardIsUnique("1234567890"));
	}
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // defineCustomer

    @Test
    public void testdefineCustomerUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.defineCustomer("Lillo"));
    }

    @Test
    public void testdefineCustomerInvalidNullName() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerNameException.class, () -> es.defineCustomer(null));
    }

    @Test
    public void testdefineCustomerInvalidEmptyName() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerNameException.class, () -> es.defineCustomer(""));
    }

    @Test
    public void testdefineCustomerNameNotUnique() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.defineCustomer("Lillo"));
        assertEquals(new Integer(-1), es.defineCustomer("Lillo"));
    }

    @Test
    public void testdefineCustomerSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.defineCustomer("Lillo"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // modifyCustomer

    @Test
    public void testmodifyCustomerUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.modifyCustomer(1,"new Lillo","1234567890"));
    }

    @Test
    public void testmodifyCustomerInvalidNullName() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerNameException.class, () -> es.modifyCustomer(1,null,"1234567890"));
    }

    @Test
    public void testmodifyCustomerInvalidEmptyName() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerNameException.class, () -> es.modifyCustomer(1,"","1234567890"));
    }

    @Test
    public void testmodifyCustomerInvalidShortCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.modifyCustomer(1,"Lillo","123456789"));
    }
    @Test
    public void testmodifyCustomerInvalidLongCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.modifyCustomer(1,"Lillo","12345678901"));
    }
    @Test
    public void testmodifyCustomerInvalidAlphabeticCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.modifyCustomer(1,"Lillo","I23A56T890"));
    }

    @Test
    public void testmodifyCustomerInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.modifyCustomer(null,"Lillo","1234567890"));
    }

    @Test
    public void testmodifyCustomerInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.modifyCustomer(-1,"Lillo","1234567890"));
    }

    @Test
    public void testmodifyCustomerNameNotUnique() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.defineCustomer("Lillo"));
        Integer id2 = es.defineCustomer("Greg");
        assertNotEquals(new Integer(-1), id2);
        assertFalse(es.modifyCustomer(id2, "Lillo", "1234567890"));
    }

    @Test
    public void testmodifyCustomerCardNotUnique() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890", id1));
        Integer id2 = es.defineCustomer("Greg");
        assertNotEquals(new Integer(-1), id2);
        assertFalse(es.modifyCustomer(id2, "new Lillo", "1234567890"));
    }

    @Test
    public void testmodifyCustomerNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.modifyCustomer(66, "new Lillo", "1234567890"));
    }

    @Test
    public void testmodifyCustomerSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.attachCardToCustomer("1234567890", id));
        assertTrue(es.modifyCustomer(id, "new Lillo", "0987654321"));
        Customer c = es.getCustomer(id);
        assertNotNull(c);
        assertEquals(c.getCustomerName(), "new Lillo");
        assertEquals(c.getCustomerCard(), "0987654321");
    }
    @Test
    public void testmodifyCustomerSuccessAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("cashier", "cashier"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.attachCardToCustomer("1234567890", id));
        assertTrue(es.modifyCustomer(id, "new Lillo", "0987654321"));
        Customer c = es.getCustomer(id);
        assertNotNull(c);
        assertEquals(c.getCustomerName(), "new Lillo");
        assertEquals(c.getCustomerCard(), "0987654321");
    }
    @Test
    public void testmodifyCustomerSuccessAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException {
        assertNotNull(es.login("manager", "manager"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.attachCardToCustomer("1234567890", id));
        assertTrue(es.modifyCustomer(id, "new Lillo", "0987654321"));
        Customer c = es.getCustomer(id);
        assertNotNull(c);
        assertEquals(c.getCustomerName(), "new Lillo");
        assertEquals(c.getCustomerCard(), "0987654321");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // deleteCustomer

    @Test
    public void testdeleteCustomerUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.deleteCustomer(1));
    }

    @Test
    public void testdeleteCustomerInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.deleteCustomer(null));
    }

    @Test
    public void testdeleteCustomerInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.deleteCustomer(-1));
    }

    @Test
    public void testdeleteCustomerNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.deleteCustomer(66));
    }

    @Test
    public void testdeleteCustomerSuccessAsAdmin() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.deleteCustomer(id));
    }

    @Test
    public void testdeleteCustomerSuccessAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("cashier", "cashier"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.deleteCustomer(id));
    }

    @Test
    public void testdeleteCustomerSuccessAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("manager", "manager"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.deleteCustomer(id));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getCustomer

    @Test
    public void testgetCustomerUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.getCustomer(1));
    }

    @Test
    public void testgetCustomerInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.getCustomer(null));
    }

    @Test
    public void testgetCustomerInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.getCustomer(-1));
    }

    @Test
    public void testgetCustomerNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        assertNull(es.getCustomer(66));
    }

    @Test
    public void testgetCustomerSuccessAsAdmin() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertNotNull(es.getCustomer(id));
    }

    @Test
    public void testgetCustomerSuccessAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("cashier", "cashier"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertNotNull(es.getCustomer(id));
    }

    @Test
    public void testgetCustomerSuccessAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("manager", "manager"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertNotNull(es.getCustomer(id));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getAllCustomers

    @Test
    public void testgetAllCustomersUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.getAllCustomers());
    }

    @Test
    public void testgetAllCustomersLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertTrue(es.getAllCustomers().isEmpty());
    }

    @Test
    public void testgetAllCustomersLoggedAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("manager", "manager"));
        assertTrue(es.getAllCustomers().isEmpty());
    }

    @Test
    public void testgetAllCustomersLoggedAsAdmin() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertTrue(es.getAllCustomers().isEmpty());
    }

    @Test
    public void testgetAllCustomersNotEmpty() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerNameException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.defineCustomer("Lillo"));
        assertFalse(es.getAllCustomers().isEmpty());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // createCard

    @Test
    public void testcreateCardUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.createCard());
    }

    @Test
    public void testcreateCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        String card = es.createCard();
        assertTrue(card.length() == 10);
        assertTrue(card.matches("[0-9]+"));
    }

    @Test
    public void testcreateCardAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        String card = es.createCard();
        assertTrue(card.length() == 10);
        assertTrue(card.matches("[0-9]+"));
    }

    @Test
    public void testcreateCardAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("manager", "manager"));
        String card = es.createCard();
        assertTrue(card.length() == 10);
        assertTrue(card.matches("[0-9]+"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // attachCardToCustomer

    @Test
    public void testattachCardToCustomerUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.attachCardToCustomer("1234567890",1));
    }

    @Test
    public void testattachCardToCustomerInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.attachCardToCustomer("1234567890",null));
    }

    @Test
    public void testattachCardToCustomerInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerIdException.class, () -> es.attachCardToCustomer("1234567890",-1));
    }

    @Test
    public void testattachCardToCustomerInvalidNullCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.attachCardToCustomer(null,1));
    }

    @Test
    public void testattachCardToCustomerInvalidEmptyCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.attachCardToCustomer("",1));
    }

    @Test
    public void testattachCardToCustomerInvalidAlphabeticCard() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.attachCardToCustomer("I23A56T890",1));
    }

    @Test
    public void testattachCardToCustomerNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.attachCardToCustomer("1234567890",1));
    }

    @Test
    public void testattachCardToCustomerNotUnique() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException, InvalidCustomerNameException {
        assertNotNull(es.login("admin", "admin"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        Integer id2 = es.defineCustomer("Greg");
        assertNotEquals(new Integer(-1), id2);
        assertFalse(es.attachCardToCustomer("1234567890",id2));
    }

    @Test
    public void testattachCardToCustomerSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException, InvalidCustomerNameException {
        assertNotNull(es.login("admin", "admin"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getCustomerCard(), "1234567890");
    }

    @Test
    public void testattachCardToCustomerSuccessAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException, InvalidCustomerNameException {
        assertNotNull(es.login("cashier", "cashier"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getCustomerCard(), "1234567890");
    }

    @Test
    public void testattachCardToCustomerSuccessAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerIdException, InvalidCustomerCardException, InvalidCustomerNameException {
        assertNotNull(es.login("manager", "manager"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getCustomerCard(), "1234567890");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // modifyPointsOnCard

    @Test
    public void testmodifyPointsOnCardUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.modifyPointsOnCard("1234567890",10));
    }

    @Test
    public void testmodifyPointsOnCardInvalidNull() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.modifyPointsOnCard(null,10));
    }

    @Test
    public void testmodifyPointsOnCardInvalidEmpty() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidCustomerCardException.class, () -> es.modifyPointsOnCard("",10));
    }

    @Test
    public void testmodifyPointsOnCardNotAssigned() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerCardException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.modifyPointsOnCard("1234567890",10));
    }

    @Test
    public void testmodifyPointsOnCardSubzero() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.attachCardToCustomer("1234567890",id));
        assertTrue(es.modifyPointsOnCard("1234567890",1));
        assertFalse(es.modifyPointsOnCard("1234567890",-2));
    }

    @Test
    public void testmodifyPointsOnCardSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        assertTrue(es.modifyPointsOnCard("1234567890",10));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getPoints(), new Integer(10));
    }

    @Test
    public void testmodifyPointsOnCardSuccessAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("cashier", "cashier"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        assertTrue(es.modifyPointsOnCard("1234567890",10));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getPoints(), new Integer(10));
    }

    @Test
    public void testmodifyPointsOnCardSuccessAsManager() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidCustomerCardException, InvalidCustomerNameException, InvalidCustomerIdException {
        assertNotNull(es.login("manager", "manager"));
        Integer id1 = es.defineCustomer("Lillo");
        assertNotEquals(new Integer(-1), id1);
        assertTrue(es.attachCardToCustomer("1234567890",id1));
        assertTrue(es.modifyPointsOnCard("1234567890",10));
        Customer c = es.getCustomer(id1);
        assertNotNull(c);
        assertEquals(c.getPoints(), new Integer(10));
    }


}
