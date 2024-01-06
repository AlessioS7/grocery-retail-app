package it.polito.ezshop.data;

import it.polito.ezshop.exceptions.*;
import it.polito.ezshop.model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class EZShop implements EZShopInterface {

    private final boolean useDatabase = true;
    private Connection dbConnection = null;
    User loggedUser;
    HashMap<Integer, User> users;
    HashMap<Integer, ProductType> productTypes;
    HashMap<Integer, Order> orders;
    HashMap<Integer, Customer> customers;
    HashMap<Integer, SaleTransaction> closedSaleTransactions;
    HashMap<Integer, SaleTransaction> payedSaleTransactions;
    HashMap<Integer, ReturnTransaction> returnTransactions;
    HashMap<String, Integer> registeredRFIDs;
    SaleTransaction currentlyOpenSaleTransaction;
    AccountBook accountBook;

    public EZShop() {
        super();
        this.users = new HashMap<Integer, User>();
        this.productTypes = new HashMap<Integer, ProductType>();
        this.orders = new HashMap<Integer, Order>();
        this.customers = new HashMap<Integer, Customer>();
        this.closedSaleTransactions = new HashMap<Integer, SaleTransaction>();
        this.payedSaleTransactions = new HashMap<Integer, SaleTransaction>();
        this.returnTransactions = new HashMap<Integer, ReturnTransaction>();
        this.registeredRFIDs = new HashMap<String, Integer>();
        this.accountBook = new AccountBook();

        if (useDatabase) {
            try {
                dbConnection = DriverManager.getConnection("jdbc:sqlite:ezshop.db");
                if (dbConnection != null) {
                    System.out.println("Connected to database");
                    // SQL statement for creating new tables
                    Statement stmt = dbConnection.createStatement();
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS users (id integer PRIMARY KEY, username text NOT NULL, password text NOT NULL, role text NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS productTypes (id integer PRIMARY KEY, quantity integer, location text, note text, description text NOT NULL, barCode text NOT NULL, pricePerUnit real NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS orders (id integer PRIMARY KEY, balanceId integer NOT NULL, productCode text NOT NULL, pricePerUnit real NOT NULL, quantity integer NOT NULL, status text NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS customers (id integer PRIMARY KEY, name text NOT NULL, card text, points integer);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS saleTransactions (ticketNumber integer PRIMARY KEY, discountRate real, price real, status TEXT NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS balanceOperations (id integer PRIMARY KEY, date integer NOT NULL, money real NOT NULL, type text NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS returnTransactions (returnId integer PRIMARY KEY, saleId integer, moneyToReturn real, status text NOT NULL);");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS ticketEntries (barCode text, saleId integer, description text, amount integer, pricePerUnit real, discountRate real, PRIMARY KEY(barCode, saleId));");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS returnedProducts (barCode text, amount integer, returnId integer, PRIMARY KEY(barCode, returnId));");
                    stmt.execute(
                            "CREATE TABLE IF NOT EXISTS registeredRFIDs (RFID text PRIMARY KEY, productId integer NOT NULL);");

                    // SQL statements to retrieve data
                    // users
                    ResultSet rs = stmt.executeQuery("SELECT id, username, password, role FROM users");
                    while (rs.next()) {
                        users.put(rs.getInt("id"), new EZUser(rs.getInt("id"), rs.getString("username"),
                                rs.getString("password"), rs.getString("role")));
                    }
                    // productTypes
                    rs = stmt.executeQuery(
                            "SELECT id, quantity, location, note, description, barCode, pricePerUnit FROM productTypes");
                    while (rs.next()) {
                        ProductType product = new EZProductType(rs.getInt("id"), rs.getString("description"),
                                rs.getString("barCode"), rs.getDouble("pricePerUnit"), rs.getString("note"));
                        product.setQuantity(rs.getInt("quantity"));
                        if (rs.getString("location") != null) {
                            product.setLocation(rs.getString("location"));
                        }
                        productTypes.put(product.getId(), product);
                    }
                    // orders
                    rs = stmt.executeQuery(
                            "SELECT id, balanceId, productCode, pricePerUnit, quantity, status FROM orders");
                    while (rs.next()) {
                        Order order = new EZOrder(rs.getInt("id"), rs.getString("productCode"), rs.getInt("quantity"),
                                rs.getDouble("pricePerUnit"), rs.getString("status"));
                        order.setBalanceId(rs.getInt("balanceId"));
                        orders.put(order.getOrderId(), order);
                    }

                    // customers
                    // customers (id integer PRIMARY KEY, name text NOT NULL, card text, points
                    // integer)
                    rs = stmt.executeQuery("SELECT id, name, card, points FROM customers");
                    while (rs.next()) {
                        Customer c = new EZCustomer(rs.getInt("id"), rs.getString("name"));
                        if (rs.getString("card") != null) {
                            c.setCustomerCard(rs.getString("card"));
                        }
                        if (rs.getInt("points") != 0) {
                            c.setPoints(rs.getInt("points"));
                        }

                        customers.put(c.getId(), c);
                    }

                    // balanceOperations
                    rs = stmt.executeQuery("SELECT id, date, money, type FROM balanceOperations");
                    while (rs.next()) {
                        BalanceOperation bo = new EZBalanceOperation(rs.getInt("id"), rs.getDate("date").toLocalDate(),
                                rs.getDouble("money"), rs.getString("type"));
                        accountBook.addBalanceOperation(bo);
                    }
                    // saleTransactions
                    rs = stmt.executeQuery("SELECT ticketNumber, discountRate, price, status FROM saleTransactions;");
                    while (rs.next()) {
                        SaleTransaction st = new EZSaleTransaction(rs.getInt(1));
                        st.setDiscountRate(rs.getInt(2));
                        st.setPrice(rs.getInt(3));

                        // Adding ticketEntries to the sale transaction
                        // ticketEntries (barCode text, saleId integer, description text, amount
                        // integer, pricePerUnit real, PRIMARY KEY(barCode, saleId))
                        PreparedStatement pstmt = dbConnection.prepareStatement(
                                "SELECT barCode, saleId, description, amount, pricePerUnit, discountRate FROM ticketEntries WHERE saleId = ?;");
                        pstmt.setInt(1, st.getTicketNumber());
                        ResultSet rs2 = pstmt.executeQuery();
                        while (rs2.next()) {
                            TicketEntry te = new EZTicketEntry(rs2.getString(1), rs2.getString(3), rs2.getInt(4),
                                    rs2.getDouble(5));
                            te.setDiscountRate(rs2.getDouble("discountRate"));

                            st.getEntries().add(te);
                        }

                        if (rs.getString("status").equals("CLOSED")) {
                            closedSaleTransactions.put(st.getTicketNumber(), st);
                        } else if (rs.getString("status").equals("PAYED")) {
                            payedSaleTransactions.put(st.getTicketNumber(), st);
                        }
                    }
                    // returnTransactions
                    // returnTransactions (returnId integer PRIMARY KEY, saleId integer,
                    // moneyToReturn real)
                    rs = stmt.executeQuery("SELECT returnId, saleId, moneyToReturn, status FROM returnTransactions;");
                    while (rs.next()) {
                        ReturnTransaction rt = new ReturnTransaction(rs.getInt(1), rs.getInt(2));
                        rt.setMoneyToReturn(rs.getDouble(3));
                        rt.setState(rs.getString(4));
                        // Adding products to the return transaction
                        // returnedProducts (barCode text, amount integer, returnId integer, PRIMARY
                        // KEY(barCode, returnId))
                        PreparedStatement pstmt = dbConnection.prepareStatement(
                                "SELECT barCode, amount, returnId FROM returnedProducts WHERE returnId = ?;");
                        pstmt.setInt(1, rt.getReturnId());
                        ResultSet rs2 = pstmt.executeQuery();
                        while (rs2.next()) {
                            rt.getProducts().put(rs2.getString(1), rs2.getInt(2));
                        }
                        returnTransactions.put(rt.getReturnId(), rt);
                    }

                    // registeredRFIDs
                    rs = stmt.executeQuery("SELECT RFID, productId FROM registeredRFIDs;");
                    while (rs.next()) {
                        registeredRFIDs.put(rs.getString("RFID"), rs.getInt("productId"));
                    }

                }

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    ///////////////////////////////////
    // UTILITY METHODS DEFINED BY US

    public static boolean checkProductBarcodeIsValid(String barCode) {
        if (barCode == null || barCode.equals("")) {
            return false;
        }
        if (barCode.length() < 12 || barCode.length() > 14) {
            return false;
        }
        if (!barCode.matches("[0-9]+")) {
            return false;
        }

        // here save the check digit (N digit of the code)
        int originalCheckDigit = Character.getNumericValue(barCode.charAt(barCode.length() - 1));
        int sum = 0, computedCheckDigit = 0, multipleOfTen = 0;

        // cycle in witch determine if the digit read is in an even or odd position
        for (int i = barCode.length() - 2, j = 0; i >= 0; i--, j++) {
            sum += ((j % 2 == 0 ? 3 : 1) * Character.getNumericValue(barCode.charAt(i)));
        }
        // calculate the nearest equal or higher multiple of ten
        if (sum % 10 == 0) {
            multipleOfTen = sum / 10;
        } else {
            multipleOfTen = sum / 10 + 1;
        }
        // calculate the computedCheckDigitve
        computedCheckDigit = (multipleOfTen * 10) - sum;
        // finally check if the code is valid
        if (computedCheckDigit == originalCheckDigit) {
            return true;
        }
        return false;
    }

    public boolean checkProductBarcodeIsUnique(String barcode) {
        for (ProductType p : productTypes.values()) {
            if (p.getBarCode().equals(barcode)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkProductPositionIsValid(String position) {
        if (position == null || position.equals("")) {
            return false;
        }

        return position.matches("[0-9]+-[a-zA-Z]+-[0-9]+");
    }

    public boolean checkProductPositionIsFree(String position) {
        if (position.equals("")) {
            return true;
        }
        for (ProductType p : productTypes.values()) {
            if (p.getLocation().equals(position)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkCustomerNameIsUnique(String name) {
        for (Customer c : customers.values()) {
            if (c.getCustomerName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkCustomerCardIsUnique(String customerCard) {
        for (Customer c : customers.values()) {
            if (c.getCustomerCard() != null && c.getCustomerCard().equals(customerCard)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkCustomerCardIsNumeric(String customerCard) {
        if (!customerCard.matches("[0-9]+")) {
            return false;
        }
        return true;
    }

    public static boolean checkCreditCardIsValid(String creditCard) {
        if (creditCard == null || creditCard.equals("") || !creditCard.matches("[0-9]+")) {
            return false;
        }

        int nDigits = creditCard.length();

        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--) {

            int d = creditCard.charAt(i) - '0';

            if (isSecond == true)
                d = d * 2;

            // We add two digits to handle
            // cases that make two digits
            // after doubling
            nSum += d / 10;
            nSum += d % 10;

            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    public static boolean checkCreditCardHasEnoughMoney(String creditCard, double amount) {
        try {
            Scanner sc = new Scanner(new FileInputStream("CreditCards.txt"));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("#")) {
                    continue;
                } else {
                    String[] lineContent = line.split(";");
                    if (lineContent[0].equals(creditCard) && Double.parseDouble(lineContent[1]) >= amount) {
                        return true;
                    }
                }
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkCreditCardIsRegistered(String creditCard) {
        try {
            Scanner sc = new Scanner(new FileInputStream("CreditCards.txt"));
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("#")) {
                    continue;
                } else {
                    String[] lineContent = line.split(";");
                    if (lineContent[0].equals(creditCard)) {
                        return true;
                    }
                }
            }
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static double computeTransactionTotal(SaleTransaction st) {
        double total = 0;
        for (TicketEntry te : st.getEntries()) {
            double entryTotalNotDiscounted = te.getAmount() * te.getPricePerUnit();
            total += (entryTotalNotDiscounted - entryTotalNotDiscounted * te.getDiscountRate());
        }
        return total - total * st.getDiscountRate();
    }

    public static boolean checkRFIDIsValid(String RFID) {
        if (RFID == null || RFID.length() != 12 || !RFID.matches("[0-9]+")) {
            return false;
        }
        return true;
    }

    public boolean checkRFIDIsUnique(String RFID) {
        return registeredRFIDs.get(RFID) == null ? true : false;
    }

    ///////////////////////////
    // OFFICIAL METHODS

    @Override
    public void reset() {

        if (useDatabase) {
            try {
                Statement stmt = dbConnection.createStatement();
                stmt.execute("DELETE FROM balanceOperations;");
                stmt.execute("DELETE FROM customers;");
                stmt.execute("DELETE FROM orders;");
                stmt.execute("DELETE FROM productTypes;");
                stmt.execute("DELETE FROM saleTransactions;");
                stmt.execute("DELETE FROM returnTransactions;");
                stmt.execute("DELETE FROM ticketEntries;");
                stmt.execute("DELETE FROM returnedProducts;");
                stmt.execute("DELETE FROM users;");
                stmt.execute("DELETE FROM registeredRFIDs;");
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
        this.users.clear();
        this.productTypes.clear();
        this.orders.clear();
        this.customers.clear();
        this.closedSaleTransactions.clear();
        this.registeredRFIDs.clear();
        this.accountBook = new AccountBook();

    }

    @Override
    public Integer createUser(String username, String password, String role)
            throws InvalidUsernameException, InvalidPasswordException, InvalidRoleException {
        // Checking the correctness of username
        if (username == null || username.equals("")) {
            throw new InvalidUsernameException("username cannot be empty or null.\n");
        }
        // Checking the correctness of password
        if (password == null || password.equals("")) {
            throw new InvalidPasswordException("password cannot be empty or null.\n");
        }
        // Checking the correctness of role
        if (role == null || role.equals("")) {
            throw new InvalidRoleException("role cannot be empty or null.\n");
        }
        if (!role.equals("Administrator") && !role.equals("Cashier") && !role.equals("ShopManager")) {
            throw new InvalidRoleException(
                    "role must be equal to one of the following values: \"Administrator\", \"Cashier\", \"ShopManager\".\n");
        }
        // Checking if exist another user with the same username
        for (User u : users.values()) {
            if (u.getUsername().equals(username)) {
                return -1;
            }
        }
        // Obtaining the new user id
        Integer id;
        try {
            id = Collections.max(users.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }

        // Creating and saving the new user
        User user = new EZUser(id, username, password, role);

        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("INSERT INTO users(id,username,password,role) VALUES(?,?,?,?)");
                pstmt.setInt(1, user.getId());
                pstmt.setString(2, user.getUsername());
                pstmt.setString(3, user.getPassword());
                pstmt.setString(4, user.getRole());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }

        users.put(id, user);
        return id;
    }

    @Override
    public boolean deleteUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        // Checking if we are authorised to delete a user
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator")) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Checking if id is valid
        if (id == null || id <= 0) {
            throw new InvalidUserIdException("id not valid.\n");
        }
        // Checking if the user exists in our list of users
        if (!users.containsKey(id)) {
            return false;
        }
        // Deleting the specified user
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement("DELETE FROM users WHERE id = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        users.remove(id);
        return true;
    }

    @Override
    public List<User> getAllUsers() throws UnauthorizedException {
        // Checking if we are authorised to get the list
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator")) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        return new ArrayList<User>(users.values());
    }

    @Override
    public User getUser(Integer id) throws InvalidUserIdException, UnauthorizedException {
        // Checking if id is valid
        if (id == null || id <= 0) {
            throw new InvalidUserIdException("id not valid.\n");
        }
        // Checking if we are authorised to get the user
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator")) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Checking if the user exists in our list of users
        if (!users.containsKey(id)) {
            return null;
        }
        return users.get(id);
    }

    @Override
    public boolean updateUserRights(Integer id, String role)
            throws InvalidUserIdException, InvalidRoleException, UnauthorizedException {
        // Checking if we are authorised to update rights of a user
        if (loggedUser == null || !loggedUser.getRole().equals("Administrator")) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Checking if id is valid
        if (id == null || id <= 0) {
            throw new InvalidUserIdException("id not valid.\n");
        }
        // Checking the correctness of role
        if (role == null || role.equals("")) {
            throw new InvalidRoleException("role cannot be empty or null.\n");
        }
        if (!role.equals("Administrator") && !role.equals("Cashier") && !role.equals("ShopManager")) {
            throw new InvalidRoleException(
                    "role must be equal to one of the following values: \"Administrator\", \"Cashier\", \"ShopManager\".\n");
        }
        // Checking if the user exists in our list of users
        if (!users.containsKey(id)) {
            return false;
        }
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement("UPDATE users SET role = ? WHERE id = ? ");
                pstmt.setString(1, role);
                pstmt.setInt(2, id);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        users.get(id).setRole(role);
        return true;
    }

    @Override
    public User login(String username, String password) throws InvalidUsernameException, InvalidPasswordException {
        // Checking if username is valid
        if (username == null || username.equals("")) {
            throw new InvalidUsernameException("username cannot be empty or null.\n");
        }
        // Checking if password is valid
        if (password == null || password.equals("")) {
            throw new InvalidPasswordException("password cannot be empty or null.\n");
        }
        // Checking if the credentials are correct
        for (User u : users.values()) {
            if (u.getUsername().equals(username) && u.getPassword().equals(password)) {
                loggedUser = u;
                return loggedUser;
            }
        }

        return null;
    }

    @Override
    public boolean logout() {
        if (loggedUser != null) {
            loggedUser = null;
            return true;
        }

        return false;
    }

    @Override
    public Integer createProductType(String description, String productCode, double pricePerUnit, String note)
            throws InvalidProductDescriptionException, InvalidProductCodeException, InvalidPricePerUnitException,
            UnauthorizedException {
        // Checking if description is valid
        if (description == null || description.equals("")) {
            throw new InvalidProductDescriptionException("description cannot be null or empty.\n");
        }

        // Checking if productCode is valid
        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode isn't a valid barCode\n");
        }

        // Checking if pricePerUnit is greater than 0
        if (pricePerUnit <= 0) {
            throw new InvalidPricePerUnitException("pricePerUnit cannot be less than or equal to 0.\n");
        }
        // Checking if we are authorised to create a productType
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Checking if productCode is unique
        if (!checkProductBarcodeIsUnique(productCode)) {
            return -1;
        }

        // Creating and inserting the product
        Integer id;
        try {
            id = Collections.max(productTypes.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }
        EZProductType product = new EZProductType(id, description, productCode, pricePerUnit, note);
        if (useDatabase) {
            try {
                // productTypes (id integer PRIMARY KEY, quantity integer, location text, note
                // text, description text NOT NULL, barCode text NOT NULL, pricePerUnit real NOT
                // NULL)
                PreparedStatement pstmt = dbConnection.prepareStatement(
                        "INSERT INTO productTypes(id, description, barCode, pricePerUnit, note, quantity, location) VALUES(?, ?, ?, ?, ?, 0, '')");
                pstmt.setInt(1, product.getId());
                pstmt.setString(2, product.getProductDescription());
                pstmt.setString(3, product.getBarCode());
                pstmt.setDouble(4, product.getPricePerUnit());
                pstmt.setString(5, product.getNote());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        productTypes.put(product.getId(), product);
        return product.getId();
    }

    @Override
    public boolean updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote)
            throws InvalidProductIdException, InvalidProductDescriptionException, InvalidProductCodeException,
            InvalidPricePerUnitException, UnauthorizedException {
        // Checking if id is valid
        if (id == null || id <= 0) {
            throw new InvalidProductIdException("id cannot be null, or less than or equal to 0");
        }
        // Checking if newDescription is valid
        if (newDescription == null || newDescription.equals("")) {
            throw new InvalidProductDescriptionException("newDescription cannot be null or empty.\n");
        }
        // Checking if newCode is valid
        if (!checkProductBarcodeIsValid(newCode)) {
            throw new InvalidProductCodeException("newCode isn't a valid barCode\n");
        }

        // Checking if pricePerUnit is greater than 0
        if (newPrice <= 0) {
            throw new InvalidPricePerUnitException("pricePerUnit cannot be less than or equal to 0.\n");
        }
        // Checking if we are authorised to update a productType
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Checking if there is a product with given id
        if (productTypes.containsKey(id)) {
            ProductType p = productTypes.get(id);

            // Checking if newCode is unique
            if (!p.getBarCode().equals(newCode) && !checkProductBarcodeIsUnique(newCode)) {
                return false;
            }

            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection.prepareStatement(
                            "UPDATE productTypes SET description = ?, barCode = ?, pricePerUnit = ?, note = ? WHERE id = ? ");
                    pstmt.setString(1, newDescription);
                    pstmt.setString(2, newCode);
                    pstmt.setDouble(3, newPrice);
                    pstmt.setString(4, newNote);
                    pstmt.setInt(5, id);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            // if successfull retrieve from HashMap and update, else return false
            p.setProductDescription(newDescription);
            p.setBarCode(newCode);
            p.setPricePerUnit(newPrice);
            p.setNote(newNote);
            return true;
        }
        return false;

    }

    @Override
    public boolean deleteProductType(Integer id) throws InvalidProductIdException, UnauthorizedException {
        // Checking if id is valid
        if (id == null || id <= 0) {
            throw new InvalidProductIdException("id cannot be null, or less than or equal to 0");
        }

        // Checking if we are authorised to delete a productType
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        // Delete from DB
        // if successfull, delete from HashMap, else return false
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement("DELETE FROM productTypes WHERE id = ?");
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        if (productTypes.remove(id) == null) {
            return false;
        }
        return true;
    }

    @Override
    public List<ProductType> getAllProductTypes() throws UnauthorizedException {
        // Checking if we are authorised to get the list
        if (loggedUser == null || !(loggedUser.getRole().equals("Administrator")
                || loggedUser.getRole().equals("ShopManager") || loggedUser.getRole().equals("Cashier"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        return new ArrayList<ProductType>(productTypes.values());
    }

    @Override
    public ProductType getProductTypeByBarCode(String barCode)
            throws InvalidProductCodeException, UnauthorizedException {
        // Checking if barCode is valid
        if (!checkProductBarcodeIsValid(barCode)) {
            throw new InvalidProductCodeException("barCode isn't a valid barCode\n");
        }

        // Checking if we are authorised to get a productType by barCode
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        for (Integer id : productTypes.keySet()) {
            if (productTypes.get(id).getBarCode().equals(barCode)) {
                return productTypes.get(id);
            }
        }
        return null;
    }

    @Override
    public List<ProductType> getProductTypesByDescription(String description) throws UnauthorizedException {
        // Checking if we are authorised to get a productType by description
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if description is null
        String subDesc = description == null ? "" : description;

        // Filtering by productDescription contains subDesc
        List<ProductType> pList = productTypes.values().stream()
                .filter(p -> p.getProductDescription().contains(subDesc)).collect(Collectors.toList());
        return pList;
    }

    @Override
    public boolean updateQuantity(Integer productId, int toBeAdded)
            throws InvalidProductIdException, UnauthorizedException {
        // Checking if id is valid
        if (productId == null || productId <= 0) {
            throw new InvalidProductIdException("id cannot be null, or less than or equal to 0");
        }

        // Checking if we are authorised to update the quantity of a productType
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        if (productTypes.containsKey(productId)) {
            ProductType p = productTypes.get(productId);
            if ((p.getQuantity() + toBeAdded >= 0) && !(p.getLocation().equals(""))) {
                if (useDatabase) {
                    try {
                        PreparedStatement pstmt = dbConnection
                                .prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ? ");
                        pstmt.setInt(1, p.getQuantity() + toBeAdded);
                        pstmt.setInt(2, productId);
                        pstmt.executeUpdate();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return false;
                    }
                }
                p.setQuantity(p.getQuantity() + toBeAdded);
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean updatePosition(Integer productId, String newPos)
            throws InvalidProductIdException, InvalidLocationException, UnauthorizedException {
        // Checking if id is valid
        if (productId == null || productId <= 0) {
            throw new InvalidProductIdException("id cannot be null, or less than or equal to 0");
        }

        // Checking if location is valid
        if (newPos != null && !newPos.equals("") && !checkProductPositionIsValid(newPos)) {
            throw new InvalidLocationException(
                    "the new position isn't in the format <aisleNumber>-<rackAlphabeticIdentifier>-<levelNumber>");
        }

        // Checking if we are authorised to update the position of a product
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        if (newPos == null || newPos.equals("")) {
            newPos = "";
        }

        if (productTypes.containsKey(productId) && checkProductPositionIsFree(newPos)) {
            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection
                            .prepareStatement("UPDATE productTypes SET location = ? WHERE id = ? ");
                    pstmt.setString(1, newPos);
                    pstmt.setInt(2, productId);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            productTypes.get(productId).setLocation(newPos);
            return true;
        }
        return false;
    }

    @Override
    public Integer issueOrder(String productCode, int quantity, double pricePerUnit) throws InvalidProductCodeException,
            InvalidQuantityException, InvalidPricePerUnitException, UnauthorizedException {

        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if productCode is valid
        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode isn't a valid barCode\n");
        }

        // if the quantity is not positive
        if (quantity <= 0) {
            throw new InvalidQuantityException("Product quantity cannot be less than or equal to 0.\\n");
        }

        // if the price is not positive
        if (pricePerUnit <= 0.0) {
            throw new InvalidPricePerUnitException("pricePerUnit cannot be less than or equal to 0.\n");
        }

        // if barcode is unique -> product doesn't exists
        if (checkProductBarcodeIsUnique(productCode)) {
            return -1;
        }

        // Creating and inserting the order as ISSUED
        Integer id;
        try {
            id = Collections.max(orders.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }
        EZOrder order = new EZOrder(id, productCode, quantity, pricePerUnit, "ISSUED");
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement(
                        "INSERT INTO orders(id,balanceId,productCode,pricePerUnit,quantity,status) VALUES(?,?,?,?,?,?)");
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, order.getBalanceId());
                pstmt.setString(3, order.getProductCode());
                pstmt.setDouble(4, order.getPricePerUnit());
                pstmt.setInt(5, order.getQuantity());
                pstmt.setString(6, order.getStatus());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        orders.put(order.getOrderId(), order);
        return order.getOrderId();
    }

    @Override
    public Integer payOrderFor(String productCode, int quantity, double pricePerUnit)
            throws InvalidProductCodeException, InvalidQuantityException, InvalidPricePerUnitException,
            UnauthorizedException {

        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if productCode is valid
        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode isn't a valid barCode\n");
        }

        // if the quantity is not positive
        if (quantity <= 0) {
            throw new InvalidQuantityException("Product quantity cannot be less than or equal to 0.\\n");
        }

        // if the price is not positive
        if (pricePerUnit <= 0.0) {
            throw new InvalidPricePerUnitException("pricePerUnit cannot be less than or equal to 0.\n");
        }

        // if barcode is unique -> product doesn't exists
        if (checkProductBarcodeIsUnique(productCode)) {
            return -1;
        }

        // check that we have enough money
        if (accountBook.computeBalance() < quantity * pricePerUnit) {
            return -1;
        }

        // Creating and inserting the order as PAYED
        Integer id;
        try {
            id = Collections.max(orders.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }
        EZOrder order = new EZOrder(id, productCode, quantity, pricePerUnit, "PAYED");
        int balanceId = accountBook.getIdOfNextBalanceOperation();
        order.setBalanceId(balanceId);
        EZBalanceOperation bo = new EZBalanceOperation(balanceId, LocalDate.now(), quantity * pricePerUnit, "DEBIT");

        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection.prepareStatement(
                        "INSERT INTO orders(id,balanceId,productCode,pricePerUnit,quantity,status) VALUES(?,?,?,?,?,?)");
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, order.getBalanceId());
                pstmt.setString(3, order.getProductCode());
                pstmt.setDouble(4, order.getPricePerUnit());
                pstmt.setInt(5, order.getQuantity());
                pstmt.setString(6, order.getStatus());
                pstmt.executeUpdate();

                pstmt = dbConnection
                        .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                pstmt.setInt(1, bo.getBalanceId());
                pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                pstmt.setDouble(3, bo.getMoney());
                pstmt.setString(4, bo.getType());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        accountBook.addBalanceOperation(bo);
        orders.put(order.getOrderId(), order);
        return order.getOrderId();
    }

    @Override
    public boolean payOrder(Integer orderId) throws InvalidOrderIdException, UnauthorizedException {
        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException("orderId can't be null or <= 0");
        }
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        if (orders.containsKey(orderId)) {
            Order order = orders.get(orderId);
            // check that we have enough money
            if (accountBook.computeBalance() < order.getQuantity() * order.getPricePerUnit()) {
                return false;
            }
            if (order.getStatus().equals("PAYED")) {
                // do nothing and return
                return false;
            }
            if (order.getStatus().equals("ISSUED") || order.getStatus().equals("ORDERED")) {
                int balanceId = accountBook.getIdOfNextBalanceOperation();
                order.setBalanceId(balanceId);
                EZBalanceOperation bo = new EZBalanceOperation(balanceId, LocalDate.now(),
                        order.getQuantity() * order.getPricePerUnit(), "DEBIT");

                if (useDatabase) {
                    try {
                        PreparedStatement pstmt = dbConnection
                                .prepareStatement("UPDATE orders SET status = ?, balanceId = ? WHERE id = ?");
                        pstmt.setString(1, "PAYED");
                        pstmt.setInt(2, order.getBalanceId());
                        pstmt.setInt(3, order.getOrderId());
                        pstmt.executeUpdate();

                        pstmt = dbConnection
                                .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                        pstmt.setInt(1, bo.getBalanceId());
                        pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                        pstmt.setDouble(3, bo.getMoney());
                        pstmt.setString(4, bo.getType());
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        return false;
                    }
                }
                order.setStatus("PAYED");
                accountBook.addBalanceOperation(bo);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean recordOrderArrival(Integer orderId)
            throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException {
        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException("orderId can't be null or <= 0");
        }
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        if (orders.containsKey(orderId)) {
            Order order = orders.get(orderId);
            if (order.getStatus().equals("COMPLETED")) {
                // do nothing and return
                return false;
            }
            if (order.getStatus().equals("PAYED")) {
                try {
                    ProductType product = getProductTypeByBarCode(order.getProductCode());
                    if (product != null && !product.getLocation().equals("")) {
                        if (useDatabase) {
                            try {
                                PreparedStatement pstmt = dbConnection
                                        .prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ? ");
                                pstmt.setInt(1, product.getQuantity() + order.getQuantity());
                                pstmt.setInt(2, product.getId());
                                pstmt.executeUpdate();

                                pstmt = dbConnection
                                        .prepareStatement("UPDATE orders SET status = 'COMPLETED' WHERE id = ?");
                                pstmt.setInt(1, order.getOrderId());
                                pstmt.executeUpdate();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return false;
                            }
                        }
                        product.setQuantity(product.getQuantity() + order.getQuantity());
                        order.setStatus("COMPLETED");
                        return true;
                    } else {
                        throw new InvalidLocationException("The product has no location assigned\n");
                    }
                } catch (InvalidProductCodeException ipce) {
                    // don't propagate this exception
                }
            }
        }
        return false;
    }

    @Override
    public boolean recordOrderArrivalRFID(Integer orderId, String RFIDfrom)
            throws InvalidOrderIdException, UnauthorizedException, InvalidLocationException, InvalidRFIDException {
        if (orderId == null || orderId <= 0) {
            throw new InvalidOrderIdException("orderId can't be null or <= 0");
        }
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        if (!checkRFIDIsValid(RFIDfrom)) {
            throw new InvalidRFIDException("Given RFID is invalid\n");
        }
        if (orders.containsKey(orderId)) {
            Order order = orders.get(orderId);
            if (order.getStatus().equals("COMPLETED")) {
                // do nothing and return
                return false;
            }
            if (order.getStatus().equals("PAYED")) {
            	try {
                    ProductType product = getProductTypeByBarCode(order.getProductCode());
                    if (product != null && !product.getLocation().equals("")) {
                        Long RFIDfromAsLong;
                        try {
                            RFIDfromAsLong = Long.parseLong(RFIDfrom);
                        } catch (NumberFormatException nfe) {
                            throw new InvalidRFIDException("Given RFID is not numeric\n");
                        }

                        for(int i = 0; i < order.getQuantity(); i++) {
                            if(registeredRFIDs.get(String.format("%012d",RFIDfromAsLong+i)) != null) {
                                throw new InvalidRFIDException("Given RFID is already registered\n");
                            }
                            System.out.println(String.format("%012d",RFIDfromAsLong+i));
                        }

                        if (useDatabase) {
                            try {
                                PreparedStatement pstmt = dbConnection
                                        .prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ? ");
                                pstmt.setInt(1, product.getQuantity() + order.getQuantity());
                                pstmt.setInt(2, product.getId());
                                pstmt.executeUpdate();

                                pstmt = dbConnection
                                        .prepareStatement("UPDATE orders SET status = 'COMPLETED' WHERE id = ?");
                                pstmt.setInt(1, order.getOrderId());
                                pstmt.executeUpdate();
                                for (int i = 0; i < order.getQuantity(); i++) {
                                    pstmt = dbConnection.prepareStatement(
                                            "INSERT INTO registeredRFIDs(RFID,productId) VALUES(?,?)");
                                    pstmt.setString(1, String.format("%012d",RFIDfromAsLong+i));
                                    pstmt.setInt(2, product.getId());
                                    pstmt.executeUpdate();
                                }
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return false;
                            }
                        }for (int i = 0; i < order.getQuantity(); i++) {
                            registeredRFIDs.put(String.format("%012d",RFIDfromAsLong+i), product.getId());
                        }
                        product.setQuantity(product.getQuantity() + order.getQuantity());
                        order.setStatus("COMPLETED");
                        return true;
                    } else {
                        throw new InvalidLocationException("The product has no location assigned\n");
                    }
                } catch (InvalidProductCodeException ipce) {
                    // don't propagate this exception
                }
            }
        }
        return false;
    }

    @Override
    public List<Order> getAllOrders() throws UnauthorizedException {
        // Checking if we are authorised to get the list
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }
        return new ArrayList<Order>(orders.values());
    }

    @Override
    public Integer defineCustomer(String customerName) throws InvalidCustomerNameException, UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }
        // Checking the validity of name
        if (customerName == null || customerName.equals("")) {
            throw new InvalidCustomerNameException("The customer name cannot be empty or null.\n");
        }
        // Checking if the customer name is unique
        if (!checkCustomerNameIsUnique(customerName)) {
            return -1;
        }
        // Creating and inserting the customer
        Integer id;
        try {
            id = Collections.max(customers.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }

        // Creating and saving the new customer
        Customer customer = new EZCustomer(id, customerName);
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("INSERT INTO customers(id,name,card) VALUES(?,?,?)");
                pstmt.setInt(1, customer.getId());
                pstmt.setString(2, customer.getCustomerName());
                pstmt.setString(3, customer.getCustomerCard());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        customers.put(id, customer);
        return id;
    }

    @Override
    public boolean modifyCustomer(Integer id, String newCustomerName, String newCustomerCard)
            throws InvalidCustomerNameException, InvalidCustomerCardException, InvalidCustomerIdException,
            UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }
        // Checking the validity of name
        if (newCustomerName == null || newCustomerName.equals("")) {
            throw new InvalidCustomerNameException("The customer name cannot be empty or null.\n");
        }
        // Checking the validity of the card code
        if ((newCustomerCard != null) && (!newCustomerCard.equals("")
                && ((newCustomerCard.length() != 10) || !checkCustomerCardIsNumeric(newCustomerCard)))) {
            throw new InvalidCustomerCardException("The card code must be of 10 digits.\n");
        }
        // Checking the validity of the id
        if (id == null || id <= 0) {
            throw new InvalidCustomerIdException("Invalid id.\n");
        }

        if (customers.containsKey(id)) {
            Customer customer = customers.get(id);

            // Checking if the customer name is unique
            if (customer.getCustomerName() != null && !customer.getCustomerName().equals(newCustomerName)
                    && !checkCustomerNameIsUnique(newCustomerName)) {
                return false;
            }
            // Checking if the customer card is unique
            if ((customer.getCustomerCard() == null
                    || (customer.getCustomerCard() != null && !customer.getCustomerCard().equals(newCustomerCard)))
                    && !checkCustomerCardIsUnique(newCustomerCard)) {
                return false;
            }
            Integer newPoints;
            if (newCustomerCard == null || (customer.getCustomerCard() != null && newCustomerCard != null
                    && customer.getCustomerCard().equals(newCustomerCard))) {
                // this allows to use a unique sql query even if we don't want to update the
                // customerCard
                newCustomerCard = customer.getCustomerCard();
                newPoints = customer.getPoints();
            } else {
                if (newCustomerCard.equals("")) {
                    // reset the customerCard
                    newCustomerCard = null;
                } else {
                    // do nothing, keep the newCustomerCard passed as parameter
                }
                newPoints = 0;
            }

            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection
                            .prepareStatement("UPDATE customers SET name = ?, card = ?, points=? WHERE id = ? ");
                    pstmt.setString(1, newCustomerName);
                    pstmt.setString(2, newCustomerCard);
                    pstmt.setInt(3, newPoints);
                    pstmt.setInt(4, customer.getId());
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            customer.setCustomerName(newCustomerName);
            customer.setCustomerCard(newCustomerCard);
            customer.setPoints(newPoints);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        // Checking the validity of the id
        if (id == null || id <= 0) {
            throw new InvalidCustomerIdException("Invalid id.\n");
        }
        if (customers.containsKey(id)) {
            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection.prepareStatement("DELETE FROM customers WHERE id = ?");
                    pstmt.setInt(1, id);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            customers.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public Customer getCustomer(Integer id) throws InvalidCustomerIdException, UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        // Checking the validity of the id
        if (id == null || id <= 0)
            throw new InvalidCustomerIdException("Invalid id.\n");

        return customers.get(id);
    }

    @Override
    public List<Customer> getAllCustomers() throws UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        return new ArrayList<Customer>(customers.values());
    }

    @Override
    public String createCard() throws UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        String cardCode;
        boolean flag = true;

        // Create a random code of 10 digits and verify it's a new assignable card
        // (doesn't have
        // the same code of the already assigned cards)
        do {
            String digits = "0123456789";
            Random rnd = new Random();
            StringBuilder sb = new StringBuilder(10);

            for (int i = 0; i < 10; i++)
                sb.append(digits.charAt(rnd.nextInt(digits.length())));

            cardCode = sb.toString();

            // Checking if the new generated card (cardCode) is already assigned to some
            // customer
            if (checkCustomerCardIsUnique(cardCode)) {
                flag = false;
            }

        } while (flag);

        return cardCode;
    }

    @Override
    public boolean attachCardToCustomer(String customerCard, Integer customerId)
            throws InvalidCustomerIdException, InvalidCustomerCardException, UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }
        // Checking the validity of the customerId
        if (customerId == null || customerId <= 0) {
            throw new InvalidCustomerIdException("Invalid id.\n");
        }
        // Checking the validity of the customerCard
        if (customerCard == null || customerCard.equals("") || customerCard.length() != 10
                || !checkCustomerCardIsNumeric(customerCard)) {
            throw new InvalidCustomerCardException("Invalid customer card.\n");
        }
        // Checking if exist a customer associated to customerId
        if (customers.get(customerId) == null) {
            return false;
        }
        // Checking if customerCard is already assigned to some other customer
        /*
         * for (Customer c : customers.values()) { if (c.getId() != customerId &&
         * c.getCustomerCard() != null && c.getCustomerCard().equals(customerCard)) {
         * return false; } }
         */
        if (!checkCustomerCardIsUnique(customerCard)) {
            return false;
        }

        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("UPDATE customers SET card = ?, points=0 WHERE id = ? ");
                pstmt.setString(1, customerCard);
                pstmt.setInt(2, customerId);
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        customers.get(customerId).setCustomerCard(customerCard);
        return true;
    }

    @Override
    public boolean modifyPointsOnCard(String customerCard, int pointsToBeAdded)
            throws InvalidCustomerCardException, UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null || (!loggedUser.getRole().equals("Administrator")
                && !loggedUser.getRole().equals("Cashier") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        // Checking the validity of the customerCard
        if (customerCard == null || customerCard.equals("") || customerCard.length() != 10
                || !checkCustomerCardIsNumeric(customerCard)) {
            throw new InvalidCustomerCardException("Invalid customer card.\n");
        }

        Customer customer = null;

        // Retrieving the customer assigned to customerCard
        for (Customer c : customers.values()) {
            if (c.getCustomerCard() != null && c.getCustomerCard().equals(customerCard)) {
                customer = c;
                break;
            }
        }

        // Checking if there is no customer assigned to customerCard
        if (customer == null)
            return false;

        if (customer.getPoints() + pointsToBeAdded >= 0) {
            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection
                            .prepareStatement("UPDATE customers SET points = ? WHERE id = ? ");
                    pstmt.setInt(1, customer.getPoints() + pointsToBeAdded);
                    pstmt.setInt(2, customer.getId());
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            customer.setPoints(customer.getPoints() + pointsToBeAdded);
            return true;
        }
        return false;
    }

    /**
     * Use case 6 - Manage sale transaction
     * 
     * @return integer id of the transaction, to be used for subsequent references
     *         to the transaction.
     */
    @Override
    public Integer startSaleTransaction() throws UnauthorizedException {
        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        // Creating and inserting the new transaction
        Integer id;
        Collection<Integer> allSaleTransactionIntegers = new ArrayList<Integer>();
        allSaleTransactionIntegers.addAll(closedSaleTransactions.keySet());
        allSaleTransactionIntegers.addAll(payedSaleTransactions.keySet());

        try {
            id = Collections.max(allSaleTransactionIntegers) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }

        SaleTransaction st = new EZSaleTransaction(id);
        currentlyOpenSaleTransaction = st;
        return id;
    }

    @Override
    public boolean addProductToSale(Integer transactionId, String productCode, int amount)
            throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException,
            UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode is not valid.\n");
        }

        if (amount < 0) {
            throw new InvalidQuantityException("Amount should not be negative");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }

        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            try {
                ProductType product = getProductTypeByBarCode(productCode);
                if (product != null) {
                    if (product.getQuantity() >= amount) {
                        product.setQuantity(product.getQuantity() - amount);
                        for (TicketEntry entry : currentlyOpenSaleTransaction.getEntries()) {
                            if (entry.getBarCode().equals(productCode)) {
                                entry.setAmount(entry.getAmount() + amount);
                                return true;
                            }
                        }
                        TicketEntry te = new EZTicketEntry(productCode, product.getProductDescription(), amount,
                                product.getPricePerUnit());
                        currentlyOpenSaleTransaction.getEntries().add(te);
                        currentlyOpenSaleTransaction.setPrice(computeTransactionTotal(currentlyOpenSaleTransaction));
                        return true;
                    }
                }
            } catch (InvalidProductCodeException ipce) {
                // don't propagate this exception
            }
        }
        return false;
    }

    @Override
    public boolean addProductToSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException,
            InvalidRFIDException, InvalidQuantityException, UnauthorizedException {
        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }

        if (!checkRFIDIsValid(RFID)) {
            throw new InvalidRFIDException("Invalid RFID.\n");
        }
        try {
            Integer productId = registeredRFIDs.get(RFID);
            
            if(productId == null) {
            	return false;
            }

            return addProductToSale(transactionId, productTypes.get(productId).getBarCode(), 1);
        } catch (InvalidProductCodeException e) {
            throw new InvalidRFIDException("Product not found\n");
        }
    }

    @Override
    public boolean deleteProductFromSale(Integer transactionId, String productCode, int amount)
            throws InvalidTransactionIdException, InvalidProductCodeException, InvalidQuantityException,
            UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode is not valid.\n");
        }

        if (amount < 0) {
            throw new InvalidQuantityException("Amount should not be negative");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }
        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            try {
                ProductType product = getProductTypeByBarCode(productCode);
                if (product != null) {
                    List<TicketEntry> entries = currentlyOpenSaleTransaction.getEntries();

                    for (TicketEntry entry : entries) {
                        if (entry.getBarCode().equals(productCode)) {
                            if (entry.getAmount() >= amount) {
                                entry.setAmount(entry.getAmount() - amount);
                                if (entry.getAmount() == 0) {
                                    entries.remove(entry);
                                }
                                product.setQuantity(product.getQuantity() + amount);
                                currentlyOpenSaleTransaction
                                        .setPrice(computeTransactionTotal(currentlyOpenSaleTransaction));
                                return true;
                            }
                        }
                    }
                }
            } catch (InvalidProductCodeException ipce) {
                // don't propagate this exception
            }
        }
        return false;
    }

    @Override
    public boolean deleteProductFromSaleRFID(Integer transactionId, String RFID) throws InvalidTransactionIdException,
            InvalidRFIDException, InvalidQuantityException, UnauthorizedException {
    	
    	Integer productId;
    	
        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }

        if (!checkRFIDIsValid(RFID)) {
            throw new InvalidRFIDException("Invalid RFID.\n");
        }
        try {
        	productId = registeredRFIDs.get(RFID);
        	
            if(productId == null) {
            	return false;
            }
            
            return deleteProductFromSale(transactionId, productTypes.get(productId).getBarCode(), 1);
        } catch (InvalidProductCodeException e) {
            throw new InvalidRFIDException("Product not found\n");
        }
    }

    @Override
    public boolean applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate)
            throws InvalidTransactionIdException, InvalidProductCodeException, InvalidDiscountRateException,
            UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        // Checking if productCode is valid
        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("productCode isn't a valid barCode\n");
        }

        if (discountRate < 0.0 || discountRate >= 1.0) {
            throw new InvalidDiscountRateException("Discount should be between 0 % and 100 %");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }
        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            for (TicketEntry entry : currentlyOpenSaleTransaction.getEntries()) {
                if (entry.getBarCode().equals(productCode)) {
                    entry.setDiscountRate(discountRate);
                    currentlyOpenSaleTransaction.setPrice(computeTransactionTotal(currentlyOpenSaleTransaction));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean applyDiscountRateToSale(Integer transactionId, double discountRate)
            throws InvalidTransactionIdException, InvalidDiscountRateException, UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (discountRate < 0.0 || discountRate >= 1.0) {
            throw new InvalidDiscountRateException("Discount should be between 0 % and 100 %");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }

        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            currentlyOpenSaleTransaction.setDiscountRate(discountRate);
            currentlyOpenSaleTransaction.setPrice(computeTransactionTotal(currentlyOpenSaleTransaction));
            return true;
        }
        if (closedSaleTransactions.containsKey(transactionId)) {
            closedSaleTransactions.get(transactionId).setDiscountRate(discountRate);
            closedSaleTransactions.get(transactionId)
                    .setPrice(computeTransactionTotal(closedSaleTransactions.get(transactionId)));
            return true;
        }
        return false;
    }

    @Override
    public int computePointsForSale(Integer transactionId) throws InvalidTransactionIdException, UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }
        SaleTransaction st;
        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            st = currentlyOpenSaleTransaction;
        } else if (closedSaleTransactions.containsKey(transactionId)) {
            st = closedSaleTransactions.get(transactionId);
        } else if (payedSaleTransactions.containsKey(transactionId)) {
            st = payedSaleTransactions.get(transactionId);
        } else {
            return -1;
        }

        return Math.floorDiv((int) st.getPrice(), 10);
    }

    @Override
    public boolean endSaleTransaction(Integer transactionId)
            throws InvalidTransactionIdException, UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("transactionId can't be null or <= 0");
        }

        if (currentlyOpenSaleTransaction != null && currentlyOpenSaleTransaction.getTicketNumber() == transactionId) {
            currentlyOpenSaleTransaction.setPrice(computeTransactionTotal(currentlyOpenSaleTransaction));
            if (useDatabase) {

                try {
                    PreparedStatement pstmt = dbConnection.prepareStatement(
                            "INSERT INTO saleTransactions(ticketNumber,discountRate,price,status) VALUES(?,?,?,?)");
                    pstmt.setInt(1, currentlyOpenSaleTransaction.getTicketNumber());
                    pstmt.setDouble(2, currentlyOpenSaleTransaction.getDiscountRate());
                    pstmt.setDouble(3, currentlyOpenSaleTransaction.getPrice());
                    pstmt.setString(4, "CLOSED");
                    pstmt.executeUpdate();

                    for (TicketEntry te : currentlyOpenSaleTransaction.getEntries()) {
                        pstmt = dbConnection.prepareStatement(
                                "INSERT INTO ticketEntries(barCode,saleId,description,amount,pricePerUnit,discountRate) VALUES(?,?,?,?,?,?)");
                        pstmt.setString(1, te.getBarCode());
                        pstmt.setInt(2, currentlyOpenSaleTransaction.getTicketNumber());
                        pstmt.setString(3, te.getProductDescription());
                        pstmt.setInt(4, te.getAmount());
                        pstmt.setDouble(5, te.getPricePerUnit());
                        pstmt.setDouble(6, te.getDiscountRate());
                        pstmt.executeUpdate();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            closedSaleTransactions.put(transactionId, currentlyOpenSaleTransaction);
            currentlyOpenSaleTransaction = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteSaleTransaction(Integer transactionId)
            throws InvalidTransactionIdException, UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("Internal error: The requested transaction does not exist");
        }

        if (closedSaleTransactions.containsKey(transactionId)) {

            if (useDatabase) {
                try {
                    PreparedStatement pstmt = dbConnection
                            .prepareStatement("DELETE FROM saleTransactions WHERE ticketNumber = ?");
                    pstmt.setInt(1, transactionId);
                    pstmt.executeUpdate();
                    pstmt = dbConnection.prepareStatement("DELETE FROM ticketEntries WHERE saleId = ?");
                    pstmt.setInt(1, transactionId);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
            for (TicketEntry te : closedSaleTransactions.get(transactionId).getEntries()) {
                for (ProductType p : productTypes.values()) {
                    if (te.getBarCode().equals(p.getBarCode())) {
                        p.setQuantity(p.getQuantity() + te.getAmount());
                    }
                }
            }
            closedSaleTransactions.remove(transactionId);
            return true;
        }
        return false;
    }

    @Override
    public SaleTransaction getSaleTransaction(Integer transactionId)
            throws InvalidTransactionIdException, UnauthorizedException {

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("Internal error: The requested transaction does not exist");
        }
        if (closedSaleTransactions.containsKey(transactionId)) {
            return closedSaleTransactions.get(transactionId);
        }
        if (payedSaleTransactions.containsKey(transactionId)) {
            return payedSaleTransactions.get(transactionId);
        }
        return null;
    }

    @Override
    public Integer startReturnTransaction(Integer transactionId)
            throws /* InvalidTicketNumberException, */InvalidTransactionIdException, UnauthorizedException {
        // Checking if we are authorised to execute this operation
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if transactionId is valid
        if (transactionId == null || transactionId <= 0) {
            throw new InvalidTransactionIdException("Invalid id.\n");
        }

        // Checking if exists a payed saleTransaction with id = transactionId
        if (!payedSaleTransactions.containsKey(transactionId)) {
            return -1;
        }

        // Obtaining the id of the new transaction
        int id;
        try {
            id = Collections.max(returnTransactions.keySet()) + 1;
        } catch (NoSuchElementException nsee) {
            id = 1;
        }

        returnTransactions.put(id, new ReturnTransaction(id, transactionId));
        return id;
    }

    @Override
    public boolean returnProduct(Integer returnId, String productCode, int amount) throws InvalidTransactionIdException,
            InvalidProductCodeException, InvalidQuantityException, UnauthorizedException {
        // Checking if we are authorised to execute this operation
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if returnId is valid
        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Invalid id.\n");
        }

        // Checking if productCode is valid
        if (!checkProductBarcodeIsValid(productCode)) {
            throw new InvalidProductCodeException("Invalid bar code.\n");
        }

        // Checking if amount is valid
        if (amount <= 0) {
            throw new InvalidQuantityException("Invalid quantity.\n");
        }

        // Retrieving the specified return transaction
        ReturnTransaction rt = returnTransactions.get(returnId);
        if (rt == null || !rt.getState().equals("active")) {
            return false;
        }

        // Checking if prodCode actually exists in the specified saleTransaction
        int prodAmount = -1;
        double money = 0.0;
        for (TicketEntry t : payedSaleTransactions.get(rt.getSaleId()).getEntries()) {
            if (t.getBarCode().equals(productCode)) {
                prodAmount = t.getAmount();
                money = t.getPricePerUnit();
                if (t.getDiscountRate() != 0.0) {
                    money -= money * t.getDiscountRate();
                }
                break;
            }
        }

        if (prodAmount == -1 || prodAmount < amount) {
            return false;
        }

        // add product to return transaction and update moneyToReturn
        rt.getProducts().put(productCode, amount);
        rt.setMoneyToReturn(rt.getMoneyToReturn() + (money * amount));
        return true;
    }

    @Override
    public boolean returnProductRFID(Integer returnId, String RFID)
            throws InvalidTransactionIdException, InvalidRFIDException, UnauthorizedException {
    	Integer productId;
    	
        // Checking if we are authorised to execute this operation
        if (loggedUser == null || !(loggedUser.getRole().equals("Cashier")
                || loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if returnId is valid
        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Invalid id.\n");
        }

        if (!checkRFIDIsValid(RFID)) {
            throw new InvalidRFIDException("Invalid RFID.\n");
        }

        try {
        	productId = registeredRFIDs.get(RFID);
        	
            if(productId == null) {
            	return false;
            }
            
            return returnProduct(returnId, productTypes.get(productId).getBarCode(), 1);
        } catch (InvalidProductCodeException | InvalidQuantityException e) {
            throw new InvalidRFIDException("Product not found\n");
        }
    }

    @Override
    public boolean endReturnTransaction(Integer returnId, boolean commit)
            throws InvalidTransactionIdException, UnauthorizedException {
        // Checking if we are authorised to execute this operation
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if returnId is valid
        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Invalid id.\n");
        }

        // Retrieving the specified return transaction
        ReturnTransaction rt = returnTransactions.get(returnId);
        if (rt == null || !rt.getState().equals("active")) {
            return false;
        }

        SaleTransaction st = payedSaleTransactions.get(rt.getSaleId());

        if (commit) {
            HashMap<String, Integer> productsToReturned = rt.getProducts();

            // Updating the products' quantities in productTypes
            for (String barCode : productsToReturned.keySet()) {
                for (ProductType p : productTypes.values()) {
                    if (p.getBarCode().equals(barCode)) {
                        int newQuantity = p.getQuantity() + productsToReturned.get(barCode);
                        p.setQuantity(newQuantity);

                        if (useDatabase) {
                            try {
                                // productTypes (id integer PRIMARY KEY, quantity integer, location text, note
                                // text, description text NOT NULL, barCode text NOT NULL, pricePerUnit real NOT
                                // NULL)
                                PreparedStatement pstmt = dbConnection
                                        .prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ?");
                                pstmt.setInt(1, newQuantity);
                                pstmt.setInt(2, p.getId());
                                pstmt.executeUpdate();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return false;
                            }
                        }

                        break;
                    }
                }

                // Updating the products' quantities in the sale transaction
                for (TicketEntry te : st.getEntries()) {
                    if (te.getBarCode().equals(barCode)) {
                        int newQuantity = te.getAmount() - productsToReturned.get(barCode);
                        te.setAmount(newQuantity);

                        if (useDatabase) {
                            try {
                                // ticketEntries (barCode text PRIMARY KEY, saleId integer, description text,
                                // ticketEntries integer, pricePerUnit double, FOREIGN KEY(saleId) REFERENCES
                                // saleTransactions(id))
                                PreparedStatement pstmt = dbConnection
                                        .prepareStatement("UPDATE ticketEntries SET amount = ? WHERE barCode = ?");
                                pstmt.setInt(1, newQuantity);
                                pstmt.setString(2, barCode);
                                pstmt.executeUpdate();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                                return false;
                            }
                        }

                        break;
                    }
                }
            }

            st.setPrice(computeTransactionTotal(st));
            double newPrice = st.getPrice();

            // Setting the returnTransaction as committed
            rt.setState("committed");

            if (useDatabase) {
                try {
                    // saleTransactions (id integer PRIMARY KEY, ticketNumber integer NOT NULL,
                    // discountRate real, price real, status TEXT NOT NULL);
                    PreparedStatement pstmt = dbConnection
                            .prepareStatement("UPDATE saleTransactions SET price = ? WHERE ticketNumber = ?");
                    pstmt.setDouble(1, newPrice);
                    pstmt.setInt(2, rt.getSaleId());
                    pstmt.executeUpdate();

                    // returnTransactions (returnId integer PRIMARY KEY, saleId integer,
                    // moneyToReturn real)
                    pstmt = dbConnection.prepareStatement(
                            "INSERT INTO returnTransactions(returnId, saleId, moneyToReturn, status) VALUES(?, ?, ?, ?)");
                    pstmt.setInt(1, rt.getReturnId());
                    pstmt.setInt(2, rt.getSaleId());
                    pstmt.setDouble(3, rt.getMoneyToReturn());
                    pstmt.setString(4, rt.getState());
                    pstmt.executeUpdate();

                    // returnedProducts (barCode text, amount integer, returnId integer, PRIMARY
                    // KEY(barCode, returnId))
                    for (String barCode : rt.getProducts().keySet()) {
                        pstmt = dbConnection.prepareStatement(
                                "INSERT INTO returnedProducts(barCode, amount, returnId) VALUES(?, ?, ?)");
                        pstmt.setString(1, barCode);
                        pstmt.setInt(2, rt.getProducts().get(barCode));
                        pstmt.setInt(3, rt.getReturnId());
                        pstmt.executeUpdate();
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return false;
                }
            }
        } else {
            returnTransactions.remove(returnId);
        }

        return true;
    }

    @Override
    public boolean deleteReturnTransaction(Integer returnId)
            throws InvalidTransactionIdException, UnauthorizedException {
        // Checking if we are authorised to execute this operation
        if (loggedUser == null
                || !(loggedUser.getRole().equals("Administrator") || loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("You do not have the rights to perform this operation.\n");
        }

        // Checking if returnId is valid
        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Invalid id.\n");
        }

        // Retrieving the specified return transaction
        ReturnTransaction rt = returnTransactions.get(returnId);
        if (rt == null || rt.getState().equals("active") || rt.getState().equals(("payed"))) {
            return false;
        }

        SaleTransaction st = payedSaleTransactions.get(rt.getSaleId());

        HashMap<String, Integer> productsToReturned = rt.getProducts();

        // Updating the products' quantities in productTypes
        for (String barCode : productsToReturned.keySet()) {
            for (ProductType p : productTypes.values()) {
                if (p.getBarCode().equals(barCode)) {
                    int newQuantity = p.getQuantity() - productsToReturned.get(barCode);
                    p.setQuantity(newQuantity);

                    if (useDatabase) {
                        try {
                            // productTypes (id integer PRIMARY KEY, quantity integer, location text, note
                            // text, description text NOT NULL, barCode text NOT NULL, pricePerUnit real NOT
                            // NULL)
                            PreparedStatement pstmt = dbConnection
                                    .prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ?");
                            pstmt.setInt(1, newQuantity);
                            pstmt.setInt(2, p.getId());
                            pstmt.executeUpdate();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            return false;
                        }
                    }

                    break;
                }
            }

            // Updating the products' quantities in the sale transaction
            for (TicketEntry te : st.getEntries()) {
                if (te.getBarCode().equals(barCode)) {
                    int newQuantity = te.getAmount() + productsToReturned.get(barCode);
                    te.setAmount(newQuantity);

                    if (useDatabase) {
                        try {
                            // ticketEntries (barCode text PRIMARY KEY, saleId integer, description text,
                            // ticketEntries integer, pricePerUnit double, FOREIGN KEY(saleId) REFERENCES
                            // saleTransactions(id))
                            PreparedStatement pstmt = dbConnection
                                    .prepareStatement("UPDATE ticketEntries SET amount = ? WHERE barCode = ?");
                            pstmt.setInt(1, newQuantity);
                            pstmt.setString(2, barCode);
                            pstmt.executeUpdate();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            return false;
                        }
                    }

                    break;
                }
            }
        }

        st.setPrice(computeTransactionTotal(st));
        double newPrice = st.getPrice();

        if (useDatabase) {
            try {
                // saleTransactions (id integer PRIMARY KEY, ticketNumber integer NOT NULL,
                // discountRate real, price real, status TEXT NOT NULL);
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("UPDATE saleTransactions SET price = ? WHERE ticketNumber = ?");
                pstmt.setDouble(1, newPrice);
                pstmt.setInt(2, rt.getSaleId());
                pstmt.executeUpdate();

                // returnTransactions (returnId integer PRIMARY KEY, saleId integer,
                // moneyToReturn real)
                pstmt = dbConnection.prepareStatement("DELETE FROM returnTransactions WHERE returnId = ?");
                pstmt.setInt(1, returnId);
                pstmt.executeUpdate();

                // returnedProducts (barCode text, amount integer, returnId integer, PRIMARY
                // KEY(barCode, returnId))
                for (String barCode : rt.getProducts().keySet()) {
                    pstmt = dbConnection.prepareStatement("DELETE FROM returnedProducts WHERE barCode = ?");
                    pstmt.setString(1, barCode);
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }

        return true;
    }

    @Override
    public double receiveCashPayment(Integer ticketNumber, double cash)
            throws InvalidTransactionIdException, InvalidPaymentException, UnauthorizedException {

        if (ticketNumber == null || ticketNumber <= 0) {
            throw new InvalidTransactionIdException("ticketNumber can't be null or <= 0\n");
        }

        if (cash <= 0.0) {
            throw new InvalidPaymentException("The payment value must be positive\n");
        }

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (!closedSaleTransactions.containsKey(ticketNumber)
                || cash < closedSaleTransactions.get(ticketNumber).getPrice()) {
            return -1;
        }

        BalanceOperation bo = new EZBalanceOperation(accountBook.getIdOfNextBalanceOperation(), LocalDate.now(),
                closedSaleTransactions.get(ticketNumber).getPrice(), "CREDIT");
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("UPDATE saleTransactions SET status = ? WHERE ticketNumber = ?");
                pstmt.setString(1, "PAYED");
                pstmt.setInt(2, ticketNumber);
                pstmt.executeUpdate();

                pstmt = dbConnection
                        .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                pstmt.setInt(1, bo.getBalanceId());
                pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                pstmt.setDouble(3, bo.getMoney());
                pstmt.setString(4, bo.getType());
                pstmt.executeUpdate();

                for (ProductType product : productTypes.values()) {
                    pstmt = dbConnection.prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ?");
                    pstmt.setInt(1, product.getQuantity());
                    pstmt.setInt(2, product.getId());
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return -1;
            }
        }
        SaleTransaction st = closedSaleTransactions.remove(ticketNumber);
        payedSaleTransactions.put(st.getTicketNumber(), st);
        accountBook.addBalanceOperation(bo);

        return cash - st.getPrice();
    }

    @Override
    public boolean receiveCreditCardPayment(Integer ticketNumber, String creditCard)
            throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {

        if (ticketNumber == null || ticketNumber <= 0) {
            throw new InvalidTransactionIdException("ticketNumber can't be null or <= 0\n");
        }

        if (!checkCreditCardIsValid(creditCard)) {
            throw new InvalidCreditCardException("The Credit Card is not valid");
        }
        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (!closedSaleTransactions.containsKey(ticketNumber) || !checkCreditCardIsRegistered(creditCard)
                || !checkCreditCardHasEnoughMoney(creditCard, closedSaleTransactions.get(ticketNumber).getPrice())) {
            return false;
        }

        BalanceOperation bo = new EZBalanceOperation(accountBook.getIdOfNextBalanceOperation(), LocalDate.now(),
                closedSaleTransactions.get(ticketNumber).getPrice(), "CREDIT");
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("UPDATE saleTransactions SET status = ? WHERE ticketNumber = ?");
                pstmt.setString(1, "PAYED");
                pstmt.setInt(2, ticketNumber);
                pstmt.executeUpdate();

                pstmt = dbConnection
                        .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                pstmt.setInt(1, bo.getBalanceId());
                pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                pstmt.setDouble(3, bo.getMoney());
                pstmt.setString(4, bo.getType());
                pstmt.executeUpdate();

                for (ProductType product : productTypes.values()) {
                    pstmt = dbConnection.prepareStatement("UPDATE productTypes SET quantity = ? WHERE id = ?");
                    pstmt.setInt(1, product.getQuantity());
                    pstmt.setInt(2, product.getId());
                    pstmt.executeUpdate();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        SaleTransaction st = closedSaleTransactions.remove(ticketNumber);
        payedSaleTransactions.put(st.getTicketNumber(), st);
        accountBook.addBalanceOperation(bo);
        return true;
    }

    @Override
    public double returnCashPayment(Integer returnId) throws InvalidTransactionIdException, UnauthorizedException {

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Internal error: The requested transaction does not exist\n");
        }

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (returnTransactions.containsKey(returnId)) {
            ReturnTransaction rt = returnTransactions.get(returnId);
            if (rt.getState().equals("committed")) {
                // cannot use recordBalanceUpdate since Cashiers are not allowed to use it
                // recordBalanceUpdate(-rt.getMoneyToReturn());
                double toReturn = rt.getMoneyToReturn();
                rt.setState("payed");

                EZBalanceOperation bo = new EZBalanceOperation(accountBook.getIdOfNextBalanceOperation(),
                        LocalDate.now(), toReturn, "DEBIT");

                if (useDatabase) {
                    try {
                        PreparedStatement pstmt = dbConnection
                                .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                        pstmt.setInt(1, bo.getBalanceId());
                        pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                        pstmt.setDouble(3, bo.getMoney());
                        pstmt.setString(4, bo.getType());
                        pstmt.executeUpdate();

                        pstmt = dbConnection
                                .prepareStatement("UPDATE returnTransactions SET status=? WHERE returnId=?");
                        pstmt.setString(1, rt.getState());
                        pstmt.setInt(2, rt.getReturnId());
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        return -1;
                    }
                }
                accountBook.addBalanceOperation(bo);
                return toReturn;
            }
        }
        return -1;
    }

    @Override
    public double returnCreditCardPayment(Integer returnId, String creditCard)
            throws InvalidTransactionIdException, InvalidCreditCardException, UnauthorizedException {

        if (returnId == null || returnId <= 0) {
            throw new InvalidTransactionIdException("Internal error: The requested transaction does not exist\n");
        }

        if (loggedUser == null || (!loggedUser.getRole().equals("Cashier")
                && !loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager"))) {
            throw new UnauthorizedException("The logged user must be an administrator, cashier or shop manager.\n");
        }

        if (!checkCreditCardIsValid(creditCard)) {
            throw new InvalidCreditCardException("The Credit Card is not valid");
        }

        /*
         * if (!checkCreditCardIsRegistered(creditCard)) { throw new
         * InvalidCreditCardException("The Credit Card is not registered"); }
         */
        if (returnTransactions.containsKey(returnId)) {
            ReturnTransaction rt = returnTransactions.get(returnId);
            if (rt.getState().equals("committed")) {
                // cannot use recordBalanceUpdate since Cashiers are not allowed to use it
                // recordBalanceUpdate(-rt.getMoneyToReturn());
                double toReturn = rt.getMoneyToReturn();
                rt.setState("payed");

                EZBalanceOperation bo = new EZBalanceOperation(accountBook.getIdOfNextBalanceOperation(),
                        LocalDate.now(), toReturn, "DEBIT");

                if (useDatabase) {
                    try {
                        PreparedStatement pstmt = dbConnection
                                .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                        pstmt.setInt(1, bo.getBalanceId());
                        pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                        pstmt.setDouble(3, bo.getMoney());
                        pstmt.setString(4, bo.getType());
                        pstmt.executeUpdate();

                        pstmt = dbConnection
                                .prepareStatement("UPDATE returnTransactions SET status=? WHERE returnId=?");
                        pstmt.setString(1, rt.getState());
                        pstmt.setInt(2, rt.getReturnId());
                        pstmt.executeUpdate();
                    } catch (SQLException e) {
                        System.out.println(e.getMessage());
                        return -1;
                    }
                }
                accountBook.addBalanceOperation(bo);
                return toReturn;
            }
        }
        return -1;
    }

    @Override
    public boolean recordBalanceUpdate(double toBeAdded) throws UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null
                || (!loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator or shop manager.\n");

        // Checking what the balance would be with the new balance operation
        if (accountBook.computeBalance() + toBeAdded < 0)
            return false;

        int id = accountBook.getIdOfNextBalanceOperation();
        String type;
        if (toBeAdded >= 0)
            type = "CREDIT";
        else
            type = "DEBIT";

        EZBalanceOperation bo = new EZBalanceOperation(id, LocalDate.now(), Math.abs(toBeAdded), type);
        if (useDatabase) {
            try {
                PreparedStatement pstmt = dbConnection
                        .prepareStatement("INSERT INTO balanceOperations(id,date,money,type) VALUES(?,?,?,?)");
                pstmt.setInt(1, bo.getBalanceId());
                pstmt.setDate(2, java.sql.Date.valueOf(bo.getDate()));
                pstmt.setDouble(3, bo.getMoney());
                pstmt.setString(4, bo.getType());
                pstmt.executeUpdate();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        accountBook.addBalanceOperation(bo);
        return true;
    }

    @Override
    public List<BalanceOperation> getCreditsAndDebits(LocalDate from, LocalDate to) throws UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null
                || (!loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator or shop manager.\n");

        return accountBook.getOperationBetween(from, to);
    }

    @Override
    public double computeBalance() throws UnauthorizedException {
        // Checking if the logged user has the rights to perform the operation
        if (loggedUser == null
                || (!loggedUser.getRole().equals("Administrator") && !loggedUser.getRole().equals("ShopManager")))
            throw new UnauthorizedException("The logged user must be an administrator or shop manager.\n");

        return accountBook.computeBalance();
    }
}
