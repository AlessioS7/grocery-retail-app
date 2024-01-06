package it.polito.ezshop.integrationTests;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.exceptions.*;

public class EZShopUserMethodsTest {

	EZShop es;

	@Before
	public void init() throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
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

	// createUser

	@Test
	public void testCreateUserInvalidUsername() {
		assertThrows(InvalidUsernameException.class, () -> es.createUser("", "ps", "Administrator"));
	}

	@Test
	public void testCreateUserInvalidPassword() {
		assertThrows(InvalidPasswordException.class, () -> es.createUser("user", "", "Administrator"));
	}

	@Test
	public void testCreateUserInvalidRole() {
		assertThrows(InvalidRoleException.class, () -> es.createUser("user", "ps", "notARole"));
	}

	@Test
	public void testCreateUserSuccess()
			throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
		assertTrue(4 == es.createUser("user", "ps", "Administrator"));
	}

	@Test
	public void testCreateUserAlreadyExists()
			throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
		assertTrue(-1 == es.createUser("admin", "ps", "Administrator"));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// deleteUser

	@Test
	public void testDeleteUserUnauthorized() {
		assertThrows(UnauthorizedException.class, () -> es.deleteUser(7));
	}

	@Test
	public void testDeleteUserInvalidId() throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidUserIdException.class, () -> es.deleteUser(-77));
	}

	@Test
	public void testDeleteUserIdDoesntExist()
			throws InvalidUsernameException, InvalidPasswordException, InvalidUserIdException, UnauthorizedException {
		es.login("admin", "admin");
		assertFalse(es.deleteUser(35));
	}

	@Test
	public void testDeleteUserSuccess() throws InvalidUsernameException, InvalidPasswordException,
			InvalidUserIdException, UnauthorizedException, InvalidRoleException {
		es.login("admin", "admin");
		Integer id = es.createUser("test", "test", "Cashier");
		assertTrue(es.deleteUser(id));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// getAllUsers

	@Test
	public void testGetAllUsersUnauthorized() {
		assertThrows(UnauthorizedException.class, () -> es.getAllUsers());
	}

	@Test
	public void testGetAllUsersAuthorizedNotEmpty()
			throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException {
		es.login("admin", "admin");
		assertFalse(es.getAllUsers().isEmpty());
	}

	@Test
	public void testGetAllUsersAuthorizedEmpty()
			throws InvalidUsernameException, InvalidPasswordException, UnauthorizedException, InvalidUserIdException {
		es.login("admin", "admin");
		es.deleteUser(1);
		es.deleteUser(2);
		es.deleteUser(3);
		assertTrue(es.getAllUsers().isEmpty());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// getUser

	@Test
	public void testGetUserUnauthorizedNotLogged() {
		assertThrows(UnauthorizedException.class, () -> es.getUser(1));
	}

	@Test
	public void testGetUserUnauthorizedLoggedAsCashier() throws InvalidUsernameException, InvalidPasswordException {
		es.login("cashier", "cashier");
		assertThrows(UnauthorizedException.class, () -> es.getUser(1));
	}

	@Test
	public void testGetUserUnauthorizedLoggedAsManager() throws InvalidUsernameException, InvalidPasswordException {
		es.login("manager", "manager");
		assertThrows(UnauthorizedException.class, () -> es.getUser(1));
	}

	@Test
	public void testGetUserAuthorizedInvalidNullId() throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidUserIdException.class, () -> es.getUser(null));
	}

	@Test
	public void testGetUserAuthorizedInvalidNegativeId() throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidUserIdException.class, () -> es.getUser(-1));
	}

	@Test
	public void testGetUserAuthorizedNotExists()
			throws InvalidUsernameException, InvalidPasswordException, InvalidUserIdException, UnauthorizedException {
		es.login("admin", "admin");
		assertNull(es.getUser(100));
	}

	@Test
	public void testGetUserAuthorizedExists()
			throws InvalidUsernameException, InvalidPasswordException, InvalidUserIdException, UnauthorizedException {
		es.login("admin", "admin");
		assertNotNull(es.getUser(1));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// updateUserRights

	@Test
	public void testUpdateUserRightsUnauthorizedNotLogged() {
		assertThrows(UnauthorizedException.class, () -> es.updateUserRights(1, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsUnauthorizedLoggedAsCashier()
			throws InvalidUsernameException, InvalidPasswordException {
		es.login("cashier", "cashier");
		assertThrows(UnauthorizedException.class, () -> es.updateUserRights(1, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsUnauthorizedLoggedAsManager()
			throws InvalidUsernameException, InvalidPasswordException {
		es.login("manager", "manager");
		assertThrows(UnauthorizedException.class, () -> es.updateUserRights(1, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsAuthorizedInvalidNullId()
			throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidUserIdException.class, () -> es.updateUserRights(null, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsAuthorizedInvalidNegativeId()
			throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidUserIdException.class, () -> es.updateUserRights(-1, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsAuthorizedInvalidRole() throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertThrows(InvalidRoleException.class, () -> es.updateUserRights(1, "TotallyNotValidRole"));
	}

	@Test
	public void testUpdateUserRightsAuthorizedNotExists() throws InvalidUsernameException, InvalidPasswordException,
			InvalidUserIdException, InvalidRoleException, UnauthorizedException {
		es.login("admin", "admin");
		assertFalse(es.updateUserRights(100, "Administrator"));
	}

	@Test
	public void testUpdateUserRightsAuthorizedExists() throws InvalidUsernameException, InvalidPasswordException,
			InvalidUserIdException, InvalidRoleException, UnauthorizedException {
		es.login("admin", "admin");
		assertTrue(es.updateUserRights(2, "ShopManager"));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// login

	@Test
	public void testLoginInvalidNullUsername() {
		assertThrows(InvalidUsernameException.class, () -> es.login(null, "admin"));
	}

	@Test
	public void testLoginInvalidEmptyUsername() {
		assertThrows(InvalidUsernameException.class, () -> es.login("", "admin"));
	}

	@Test
	public void testLoginInvalidNullPassword() {
		assertThrows(InvalidPasswordException.class, () -> es.login("admin", null));
	}

	@Test
	public void testLoginInvalidEmptyPassword() {
		assertThrows(InvalidPasswordException.class, () -> es.login("admin", ""));
	}

	@Test
	public void testLoginCredentialsNotCorrect() throws InvalidUsernameException, InvalidPasswordException {
		assertNull(es.login("nonExistingUser", "definitelyNonExistingPassword"));
	}

	@Test
	public void testLoginCredentialsCorrect() throws InvalidUsernameException, InvalidPasswordException {
		assertNotNull(es.login("admin", "admin"));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// logout

	@Test
	public void testLogoutNoLoggedUser() {
		assertFalse(es.logout());
	}

	@Test
	public void testLogoutLoggedUser() throws InvalidUsernameException, InvalidPasswordException {
		es.login("admin", "admin");
		assertTrue(es.logout());
	}
}
