
package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;

public class EZShopOrderMethodsTest {
    EZShop es;
    Integer orderId2;

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

    // issueOrder

    @Test
    public void testissueOrderUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.issueOrder("9788838668821", 10, 1.0));
    }

    @Test
    public void testissueOrderUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.issueOrder("9788838668821", 10, 1.0));
    }

    @Test
    public void testIssueOrderInvalidBarcode() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductDescriptionException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductCodeException.class, () -> es.issueOrder("978883861", 10, 1.0));
    }

    @Test
    public void testIssureOrderInvalidQuantity() throws InvalidUsernameException, InvalidPasswordException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidQuantityException.class, () -> es.issueOrder("9788838668821", -10, 1.0));
    }

    @Test
    public void testIssueOrderInvalidPricePerUnit() throws InvalidUsernameException, InvalidPasswordException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidPricePerUnitException.class, () -> es.issueOrder("9788838668821", 10, -1.0));
    }

    @Test
    public void testIssueOrderNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException{
        assertNotNull(es.login("admin", "admin"));
        assertEquals(new Integer(-1), es.issueOrder("9788838668821", 10, 1.0));
    }

    @Test
    public void testIssueOrderSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductDescriptionException{
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertNotEquals(new Integer(-1), es.issueOrder("9788838668821", 5, 1.0));
        assertNotEquals(new Integer(-1), es.issueOrder("9788838668821", 5, 1.0));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // payOrderFor

    @Test
    public void testpayOrderForUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.payOrderFor("9788838668821", 10, 1.0));
    }

    @Test
    public void testpayOrderForUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.payOrderFor("9788838668821", 10, 1.0));
    }

    @Test
    public void testpayOrderForInvalidProductCode() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductCodeException.class, () -> es.payOrderFor("9738668821", -10, 1.0));
    }

    @Test
    public void testpayOrderForInvalidQuantity() throws InvalidUsernameException, InvalidPasswordException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidQuantityException.class, () -> es.payOrderFor("9788838668821", -10, 1.0));
    }

    @Test
    public void testpayOrderForInvalidPricePerUnit() throws InvalidUsernameException, InvalidPasswordException{
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidPricePerUnitException.class, () -> es.payOrderFor("9788838668821", 10, -1.0));
    }

    @Test
    public void testpayOrderForNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException{
        assertNotNull(es.login("admin", "admin"));
        assertEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 1.0));
    }

    @Test
    public void testpayOrderForNotEnoughBalance() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductDescriptionException{
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(10)); // +10
        assertEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 2.0)); // -20
    }

    @Test
    public void testpayOrderForSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductDescriptionException{
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(21)); // +21
        assertNotEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 2.0)); // -20
    }

    @Test
    public void testpayOrderForSuccessMultiple() throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductDescriptionException{
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(41)); // +41
        assertNotEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 2.0)); // -20
        assertNotEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 2.0)); // -20
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // payOrder

    @Test
    public void testpayOrderUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.payOrder(1));
    }

    @Test
    public void testpayOrderUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.payOrder(1));
    }

    @Test
    public void testpayOrderInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.payOrder(null));
    }

    @Test
    public void testpayOrderInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.payOrder(-1));
    }

    @Test
    public void testpayOrderNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.payOrder(66));
    }

    @Test
    public void testpayOrderNotEnoughBalance() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        Integer id = es.issueOrder("9788838668821", 11, 1.0); // -11
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.recordBalanceUpdate(10)); // +10
        assertFalse(es.payOrder(id));
    }

    @Test
    public void testpayOrderSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        Integer id = es.issueOrder("9788838668821", 10, 1.0); // -10
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.recordBalanceUpdate(11)); // +11
        assertTrue(es.payOrder(id));
    }

    @Test
    public void testpayOrderAlreadyPayed() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        Integer id = es.issueOrder("9788838668821", 10, 1.0); // -10
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.recordBalanceUpdate(21)); // +21
        assertTrue(es.payOrder(id));
        assertFalse(es.payOrder(id));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // recordOrderArrival

    @Test
    public void testrecordOrderArrivalUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.recordOrderArrival(1));
    }

    @Test
    public void testrecordOrderArrivalUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.recordOrderArrival(1));
    }

    @Test
    public void testrecordOrderArrivalInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.recordOrderArrival(null));
    }

    @Test
    public void testrecordOrderArrivalInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.recordOrderArrival(-1));
    }

    @Test
    public void testrecordOrderArrivalNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.recordOrderArrival(66));
    }

    @Test
    public void testrecordOrderArrivalExistsNoLocation() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer id = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), id); // -20
        assertThrows(InvalidLocationException.class, () -> es.recordOrderArrival(id));
    }

    @Test
    public void testrecordOrderArrivalSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException, InvalidProductIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrival(orderId));
    }

    @Test
    public void testrecordOrderArrivalAlreadyCompleted() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException, InvalidProductIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrival(orderId));
        assertFalse(es.recordOrderArrival(orderId));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // recordOrderArrivalRFID

    @Test
    public void testrecordOrderArrivalRFIDUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.recordOrderArrivalRFID(1, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.recordOrderArrivalRFID(1, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDInvalidNullId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.recordOrderArrivalRFID(null, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidOrderIdException.class, () -> es.recordOrderArrivalRFID(-1, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDOrderNotExists() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidRFIDException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.recordOrderArrivalRFID(66, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDCodeNotValid() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer id = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), id); // -20
        
		assertThrows("null RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(id, null));
		assertThrows("empty RFID should fail", InvalidRFIDException.class, ()->es.addProductToSaleRFID(id, ""));
		assertThrows("short RFID should fail", InvalidRFIDException.class, ()->es.recordOrderArrivalRFID(id, "01234567890"));
		assertThrows("long RFID should fail", InvalidRFIDException.class, ()->es.recordOrderArrivalRFID(id, "0123456789012"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.recordOrderArrivalRFID(id, "012345_78901"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.recordOrderArrivalRFID(id, "A01234567890"));
		assertThrows("non-numeric RFID should fail", InvalidRFIDException.class, ()->es.recordOrderArrivalRFID(id, "01234567890z"));
    }

    @Test
    public void testrecordOrderArrivalRFIDExistsNoLocation() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer id = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), id); // -20
        assertThrows(InvalidLocationException.class, () -> es.recordOrderArrivalRFID(id, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDSuccess() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException, InvalidProductIdException, InvalidRFIDException {
        assertNotNull(es.login("admin", "admin"));
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670000"));
    }

    @Test
    public void testrecordOrderArrivalRFIDAlreadyCompleted() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException, InvalidProductIdException, InvalidRFIDException {
        assertNotNull(es.login("admin", "admin"));
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(21)); // +21
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670000"));
        assertFalse(es.recordOrderArrivalRFID(orderId, "012345670000"));
    }
    
    @Test
    public void testrecordOrderArrivalRFIDCodeAlreadyTaken() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidOrderIdException, InvalidLocationException, InvalidQuantityException, InvalidProductIdException, InvalidRFIDException {
        assertNotNull(es.login("admin", "admin"));
        
        Integer productId = es.createProductType("desc", "9788838668821", 2.0, "everything valid");
        assertNotEquals(new Integer(-1), productId);
        assertTrue(es.recordBalanceUpdate(50.00)); // +50
        
        // First order
        Integer orderId = es.payOrderFor("9788838668821", 10, 2.0);
        assertNotEquals(new Integer(-1), orderId); // -20
        assertTrue(es.updatePosition(productId, "12-ab-34"));
        assertTrue(es.recordOrderArrivalRFID(orderId, "012345670010")); // put in store with RFIDs 012345670010 ~ 012345670019
        
        productId = es.createProductType("desc2", "012345678905", 2.0, "everything valid second product");
        assertNotEquals(new Integer(-1), productId);

        // Second order: Try to put in store with conflicting RFIDs
        orderId2 = es.payOrderFor("012345678905", 5, 2.0);
        assertNotEquals(new Integer(-1), orderId2); // -20
        assertTrue(es.updatePosition(productId, "12-ab-35"));
//        assertFalse(es.recordOrderArrivalRFID(orderId, "012345670010")); // try to put in store with RFIDs 012345670010 ~ 012345670014
//        assertFalse(es.recordOrderArrivalRFID(orderId, "012345670015")); // try to put in store with RFIDs 012345670015 ~ 012345670019
//        assertFalse(es.recordOrderArrivalRFID(orderId, "012345670006")); // try to put in store with RFIDs 012345670006 ~ 012345670010
//        assertFalse(es.recordOrderArrivalRFID(orderId, "012345670019")); // try to put in store with RFIDs 012345670019 ~ 012345670023
        assertThrows(InvalidRFIDException.class, ()-> es.recordOrderArrivalRFID(orderId2, "012345670010")); // try to put in store with RFIDs 012345670010 ~ 012345670014
        assertThrows(InvalidRFIDException.class, () -> es.recordOrderArrivalRFID(orderId2, "012345670015")); // try to put in store with RFIDs 012345670015 ~ 012345670019
        assertThrows(InvalidRFIDException.class, () -> es.recordOrderArrivalRFID(orderId2, "012345670006")); // try to put in store with RFIDs 012345670006 ~ 012345670010
        assertThrows(InvalidRFIDException.class, () -> es.recordOrderArrivalRFID(orderId2, "012345670019")); // try to put in store with RFIDs 012345670019 ~ 012345670023
        assertTrue(es.recordOrderArrivalRFID(orderId2, "012345670020")); // put in store with RFIDs 012345670020 ~ 012345670024
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getAllOrders

    @Test
    public void testgetAllOrdersUnauthorizedNotLogged() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertThrows(UnauthorizedException.class, () -> es.getAllOrders());
    }

    @Test
    public void testgetAllOrdersUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.getAllOrders());
    }

    @Test
    public void testgetAllOrdersEmpty() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertTrue(es.getAllOrders().isEmpty());
    }

    @Test
    public void testgetAllOrdersNotEmpty() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidQuantityException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 2.0, "everything valid"));
        assertTrue(es.recordBalanceUpdate(21)); // +21
        assertNotEquals(new Integer(-1), es.payOrderFor("9788838668821", 10, 2.0)); // -20
        assertFalse(es.getAllOrders().isEmpty());
    }

}
