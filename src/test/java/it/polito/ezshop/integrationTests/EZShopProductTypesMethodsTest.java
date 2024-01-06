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

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.ProductType;
import it.polito.ezshop.exceptions.*;

public class EZShopProductTypesMethodsTest {

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
	
	// checkProductBarcodeIsUnique
    
    @Test
    public void testCheckProductBarcodeIsUniqueFalse() throws InvalidUsernameException, InvalidPasswordException,
    InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
    	es.login("admin", "admin");
    	es.createProductType("item1", "012345678905", 1.0, "everything valid");
    	assertFalse(es.checkProductBarcodeIsUnique("012345678905"));
    }

    @Test
    public void testCheckProductBarcodeIsUniqueTrue() throws InvalidUsernameException, InvalidPasswordException,
    InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
    	es.login("admin", "admin");
    	es.createProductType("item1", "012345678905", 1.0, "everything valid");
    	assertTrue(es.checkProductBarcodeIsUnique("9788838668821"));
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // createProductType

    @Test
    public void testCreateProductTypeInvalidNullDescription() {
        assertThrows(InvalidProductDescriptionException.class,
                () -> es.createProductType(null, "9788838668821", 1.0, "null description"));
    }

    @Test
    public void testCreateProductTypeInvalidEmptyDescription() {
        assertThrows(InvalidProductDescriptionException.class,
                () -> es.createProductType(null, "9788838668821", 1.0, "empty description"));
    }

    // should we test the product barcode validity again?

    @Test
    public void testCreateProductTypeInvalidPricePerUnit() {
        assertThrows(InvalidPricePerUnitException.class,
                () -> es.createProductType("desc", "9788838668821", -1.0, "invalid price per unit"));
    }

    @Test
    public void testCreateProductTypeUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class,
                () -> es.createProductType("desc", "9788838668821", 1.0, "everything valid, not logged"));
    }

    @Test
    public void testCreateProductTypeUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class,
                () -> es.createProductType("desc", "9788838668821", 1.0, "everything valid, but logged as Cashier"));
    }

    @Test
    public void testCreateProductTypeBarcodeNotUnique()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        es.createProductType("desc", "9788838668821", 1.0, "everything valid");
        assertEquals(new Integer(-1),
                es.createProductType("desc", "9788838668821", 1.0, "everything valid, but barcode is not unique"));
    }

    @Test
    public void testCreateProductTypeSuccess()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("desc", "9788838668821", 1.0, "everything valid"));
        assertNotNull(es.getProductTypeByBarCode("9788838668821"));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // updateProduct

    @Test
    public void testUpdateProductInvalidNullId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class,
                () -> es.updateProduct(null, "new description", "9788838668821", 1.0, "null id"));
    }

    @Test
    public void testUpdateProductInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class,
                () -> es.updateProduct(-1, "new description", "9788838668821", 1.0, "negative id"));
    }

    @Test
    public void testUpdateProductInvalidNullDescription() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductDescriptionException.class,
                () -> es.updateProduct(1, null, "9788838668821", 1.0, "null description"));
    }

    @Test
    public void testUpdateProductInvalidEmptyDescription() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductDescriptionException.class,
                () -> es.updateProduct(1, "", "9788838668821", 1.0, "empty description"));
    }

    // should we test the product barcode validity again?

    @Test
    public void testUpdateProductInvalidPricePerUnit() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidPricePerUnitException.class,
                () -> es.updateProduct(1, "new description", "9788838668821", -1.0, "invalid price per unit"));
    }

    @Test
    public void testUpdateProductUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class,
                () -> es.updateProduct(1, "new description", "9788838668821", 1.0, "everything valid, not logged"));
    }

    @Test
    public void testUpdateProductUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.updateProduct(1, "new description", "9788838668821", 1.0,
                "everything valid, but logged as Cashier"));
    }

    @Test
    public void testUpdateProductNotExists() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException,
            InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.updateProduct(66, "new description", "9788838668821", 1.0,
                "everything valid, but product 66 doesn't exists"));
    }

    @Test
    public void testUpdateProductBarcodeNotUnique() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException, InvalidProductIdException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("item1", "012345678905", 1.0, "everything valid"));
        Integer id2 = es.createProductType("item2", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id2);
        assertFalse(es.updateProduct(id2, "item2", "012345678905", 1.0,
                "trying to change item2's barcode to item1's barcode"));
    }

    @Test
    public void testUpdateProductSuccess() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException, InvalidProductIdException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updateProduct(id, "new desc", "012345678905", 2.0,
                "changing item's barcode, description, price per unit, and note"));
        ProductType p = es.getProductTypeByBarCode("012345678905");
        assertNotNull(p);
        assertEquals(p.getProductDescription(), "new desc");
        assertEquals(p.getBarCode(), "012345678905");
        assertEquals(p.getPricePerUnit(), new Double(2.0));
        assertEquals(p.getNote(), "changing item's barcode, description, price per unit, and note");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // deleteProductType

    @Test
    public void testDeleteProductTypeInvalidNullId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.deleteProductType(null));
    }

    @Test
    public void testDeleteProductTypeInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.deleteProductType(-1));
    }

    @Test
    public void testDeleteProductTypeUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.deleteProductType(1));
    }

    @Test
    public void testDeleteProductTypeUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.deleteProductType(1));
    }

    @Test
    public void testDeleteProductTypeNotExists() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.deleteProductType(66));
    }

    @Test
    public void testDeleteProductTypeSuccess()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductIdException, UnauthorizedException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.deleteProductType(id));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getAllProductTypes

    @Test
    public void testGetAllProductTypesUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.getAllProductTypes());
    }

    @Test
    public void testGetAllProductTypesEmpty()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("cashier", "cashier"));
        assertTrue(es.getAllProductTypes().isEmpty());
    }

    @Test
    public void testGetAllProductTypesNotEmpty()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertFalse(es.getAllProductTypes().isEmpty());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getProductTypeByBarCode

    @Test
    public void testGetProductTypeByBarCodeNotValid() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductCodeException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductCodeException.class, () -> es.getProductTypeByBarCode("978868821"));
    }

    @Test
    public void testGetProductTypeByBarCodeUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.getProductTypeByBarCode("9788838668821"));
    }

    @Test
    public void testGetProductTypeByBarCodeUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.getProductTypeByBarCode("9788838668821"));
    }

    @Test
    public void testGetProductTypeByBarCodeNotExists() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductCodeException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertNull(es.getProductTypeByBarCode("9788838668821"));
    }

    @Test
    public void testGetProductTypeByBarCodeExists()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductCodeException,
            UnauthorizedException, InvalidProductDescriptionException, InvalidPricePerUnitException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        ProductType p = es.getProductTypeByBarCode("9788838668821");
        assertNotNull(p);
        assertEquals(p.getProductDescription(), "item");
        assertEquals(p.getBarCode(), "9788838668821");
        assertEquals(p.getPricePerUnit(), new Double(1.0));
        assertEquals(p.getNote(), "everything valid");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // getProductTypesByDescription

    @Test
    public void testGetProductTypesByDescriptionUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.getProductTypesByDescription("desc"));
    }

    @Test
    public void testGetProductTypesByDescriptionUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.getProductTypesByDescription("desc"));
    }

    @Test
    public void testGetProductTypesByDescriptionNoProductsNoFilter()
            throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertTrue(es.getProductTypesByDescription(null).isEmpty());
    }

    @Test
    public void testGetProductTypesByDescriptionSomeProductsNoFilter()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("item1", "012345678905", 1.0, "everything valid"));
        assertNotEquals(new Integer(-1), es.createProductType("item2", "9788838668821", 1.0, "everything valid"));
        assertTrue(es.getProductTypesByDescription(null).size() == 2);
    }

    @Test
    public void testGetProductTypesByDescriptionSomeProductsFilter()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertNotEquals(new Integer(-1), es.createProductType("yes1", "012345678905", 1.0, "everything valid"));
        assertNotEquals(new Integer(-1), es.createProductType("yes2", "9788838668821", 2.0, "everything valid"));
        assertNotEquals(new Integer(-1), es.createProductType("no", "54326476412231", 3.0, "everything valid"));
        assertTrue(es.getProductTypesByDescription("yes").size() == 2);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // updateQuantity

    @Test
    public void testUpdateQuantityInvalidNullId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.updateQuantity(null, 10));
    }

    @Test
    public void testUpdateQuantityInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.updateQuantity(-1, 10));
    }

    @Test
    public void testUpdateQuantityUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.updateQuantity(1, 10));
    }

    @Test
    public void testUpdateQuantityUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.updateQuantity(1, 10));
    }

    @Test
    public void testUpdateQuantityNotExists() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.updateQuantity(66, 10));
    }

    @Test
    public void testUpdateQuantityNegative() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, UnauthorizedException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updatePosition(id, "12-ab-34"));
        assertTrue(es.updateQuantity(id, 1));
        assertFalse(es.updateQuantity(id, -2));
    }

    @Test
    public void testUpdateQuantityNoLocation()
            throws InvalidUsernameException, InvalidPasswordException, InvalidProductIdException, UnauthorizedException,
            InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertFalse(es.updateQuantity(id, 1));
    }

    @Test
    public void testUpdateQuantity() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, UnauthorizedException, InvalidProductDescriptionException,
            InvalidProductCodeException, InvalidPricePerUnitException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updatePosition(id, "12-ab-34"));
        assertTrue(es.updateQuantity(id, 1));
        ProductType p = es.getProductTypeByBarCode("9788838668821");
        assertNotNull(p);
        assertEquals(p.getQuantity(), new Integer(1));
        assertTrue(es.updateQuantity(id, 2));
        assertEquals(p.getQuantity(), new Integer(3));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // updatePosition

    @Test
    public void testUpdatePositionInvalidNullId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.updatePosition(null, "12-ab-34"));
    }

    @Test
    public void testUpdatePositionInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("admin", "admin"));
        assertThrows(InvalidProductIdException.class, () -> es.updatePosition(-1, "12-ab-34"));
    }

    // should we check position validity again?

    @Test
    public void testUpdatePositionUnauthorizedNotLogged() {
        assertThrows(UnauthorizedException.class, () -> es.updatePosition(1, "12-ab-34"));
    }

    @Test
    public void testUpdatePositionUnauthorizedLoggedAsCashier()
            throws InvalidUsernameException, InvalidPasswordException {
        assertNotNull(es.login("cashier", "cashier"));
        assertThrows(UnauthorizedException.class, () -> es.updatePosition(1, "12-ab-34"));
    }

    @Test
    public void testUpdatePositionProductNotExists() throws InvalidUsernameException, InvalidPasswordException,
            InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        assertNotNull(es.login("admin", "admin"));
        assertFalse(es.updatePosition(66, "12-ab-34"));
    }

    @Test
    public void testUpdatePositionNotValid() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item1", "012345678905", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertThrows(InvalidLocationException.class, () -> es.updatePosition(id, "ii-11-ii"));
    }

    @Test
    public void testUpdatePositionNotFree() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id1 = es.createProductType("item1", "012345678905", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id1);
        Integer id2 = es.createProductType("item2", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id2);

        assertTrue(es.updatePosition(id1, "12-ab-34"));
        assertFalse(es.updatePosition(id2, "12-ab-34"));
    }

    @Test
    public void testUpdatePositionResetNull() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item1", "012345678905", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updatePosition(id, null));
        ProductType p = es.getProductTypeByBarCode("012345678905");
        assertNotNull(p);
        assertEquals(p.getLocation(), "");
    }

    @Test
    public void testUpdatePositionResetEmpty() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("item1", "012345678905", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updatePosition(id, ""));
        ProductType p = es.getProductTypeByBarCode("012345678905");
        assertNotNull(p);
        assertEquals(p.getLocation(), "");
    }

    @Test
    public void testUpdatePosition() throws InvalidUsernameException, InvalidPasswordException, InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException, UnauthorizedException, InvalidProductIdException, InvalidLocationException {
        assertNotNull(es.login("admin", "admin"));
        Integer id = es.createProductType("desc", "9788838668821", 1.0, "everything valid");
        assertNotEquals(new Integer(-1), id);
        assertTrue(es.updatePosition(id, "12-ab-34"));
        ProductType p = es.getProductTypeByBarCode("9788838668821");
        assertNotNull(p);
        assertEquals(p.getLocation(), "12-ab-34");
    }

}
