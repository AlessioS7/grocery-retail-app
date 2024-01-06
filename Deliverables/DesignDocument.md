# Design Document

Authors: Alessio Santangelo, Andrea Cencio, Damiano Bonaccorsi, Lorenzo Chiola

Date: 19 May 2021

Version: 1.4

## Contents

- [High level design](#package-diagram)
- [Low level design](#class-diagram)
- [Verification traceability matrix](#verification-traceability-matrix)
- [Verification sequence diagrams](#verification-sequence-diagrams)

## High level design

```plantuml
@startuml
package "EZShopGUI"
note "MVC & Facade" as n1
package "EZShop.data"
package "EZShop.exceptions" 
package "EZShop.model"
EZShopGUI -down-> EZShop.data
note left of EZShopGUI : View
EZShop.data -down-> EZShop.model
note left of EZShop.data : Controller + Facade
EZShop.data -down-> EZShop.exceptions
note left of EZShop.model : Model
@enduml
```

## Low level design

```plantuml
@startuml
skinparam nodesep 12

note "No getters and setters" as n1

package EZShop.data {
  interface EZShopInterface {
+reset() : void
+createUser(String username, String password, String role) : Integer
+deleteUser(Integer id) : boolean
+getAllUsers() : List<User>
+getUser(Integer id) : User
+updateUserRights(Integer id, String role) : boolean
+login(String username, String password) : User
+logout() : boolean
+createProductType(String description, String productCode, double pricePerUnit, String note) : Integer
+updateProduct(Integer id, String newDescription, String newCode, double newPrice, String newNote) : boolean
+deleteProductType(Integer id) : boolean
+getAllProductTypes() : List<ProductType>
+getProductTypeByBarCode(String barCode) : ProductType
+getProductTypesByDescription(String description) : List<ProductType>
+updateQuantity(Integer productId, int toBeAdded) : boolean
+updatePosition(Integer productId, String newPos) : boolean
+issueOrder(String productCode, int quantity, double pricePerUnit) : Integer
+payOrderFor(String productCode, int quantity, double pricePerUnit) : Integer
+payOrder(Integer orderId) : boolean
+recordOrderArrival(Integer orderId) : boolean
+getAllOrders() : List<Order>
+defineCustomer(String customerName) : Integer
+modifyCustomer(Integer id, String newCustomerName, String newCustomerCard) : boolean
+deleteCustomer(Integer id) : boolean
+getCustomer(Integer id) : Customer
+getAllCustomers() : List<Customer>
+createCard() : String
+attachCardToCustomer(String customerCard, Integer customerId) : boolean
+modifyPointsOnCard(String customerCard, int pointsToBeAdded) : boolean
+startSaleTransaction() : Integer
+addProductToSale(Integer transactionId, String productCode, int amount) : boolean
+deleteProductFromSale(Integer transactionId, String productCode, int amount) : boolean
+applyDiscountRateToProduct(Integer transactionId, String productCode, double discountRate) : boolean
+applyDiscountRateToSale(Integer transactionId, double discountRate) : boolean
+computePointsForSale(Integer transactionId) : int
+endSaleTransaction(Integer transactionId) : boolean
+deleteSaleTransaction(Integer transactionId) : boolean
+getSaleTransaction(Integer transactionId) : SaleTransaction
+startReturnTransaction(Integer transactionId) : Integer
+returnProduct(Integer returnId, String productCode, int amount) : boolean
+endReturnTransaction(Integer returnId, boolean commit) : boolean
+deleteReturnTransaction(Integer returnId) : boolean
+receiveCashPayment(Integer transactionId, double cash) : double
+receiveCreditCardPayment(Integer transactionId, String creditCard) : boolean
+returnCashPayment(Integer returnId) : double
+returnCreditCardPayment(Integer returnId, String creditCard) : double
+recordBalanceUpdate(double toBeAdded) : boolean
+getCreditsAndDebits(LocalDate from, LocalDate to) : List<BalanceOperation>
+computeBalance() : double
+recordOrderArrivalRFID() : boolean
+addProductToSaleRFID() : boolean
+deleteProductFromSaleRFID() : boolean
+returnProductRFID() : boolean
}
class EZShop implements EZShopInterface  {
    -loggedUser: User
    -users: HashMap<Integer, User>
    -productTypes: HashMap<Integer, ProductType>
    -orders: HashMap<Integer, Order>
    -customers: HashMap<Integer, Customer>
    -closedSaleTransactions: HashMap<Integer, SaleTransaction>
    -payedSaleTransactions: HashMap<Integer, SaleTransaction>
    -returnTransactions: HashMap<Integer, ReturnTransaction>
    -currentlyOpenSaleTransaction: ReturnTransaction
    -accountBook: AccountBook
    -registeredRFIDs : HashMap<String, Integer>
    +checkProductBarcodeIsValid(String barcode) : boolean
    +checkProductBarcodeIsUnique(String barcode) : boolean
    +checkProductPositionIsValid(String position) : boolean
    +checkProductPositionIsFree(String position) : boolean
    +checkCustomerNameIsUnique(String name) : boolean
    +checkCustomerCardIsUnique(String customerCard) : boolean
    +checkCreditCardIsValid(String creditCard) : boolean
    +checkCreditCardHasEnoughMoney(String creditCard, double amount) : boolean
    +checkCreditCardIsRegistered(String creditCard) : boolean
    -computeTransactionTotal(SaleTransaction st) : double
}
note right of EZShop : Facade
note left of EZShop: Notice we did not need to create\na class for RFIDs since\nwe manage them just\nwith a persistent HashMap<String, Integer>
}
package EZShop.model {
  class User {
    -id: Integer
    -username: String
    -password: String
    -role: String
}
class ProductType {
    -id: Integer
    -description: String
    -productCode: String
    -pricePerUnit: double
    -note: String
    -quantity: Integer
    -position: String
}
class Order {
    -orderId: Integer
    -balanceId : Integer
    -productCode: String
    -quantity: Integer
    -pricePerUnit: double
    -state: String
}
class Customer {
    -id: Integer
    -name: String
    -card: String
    -points : Integer
    +modifyPointsOnCard(int pointsToBeAdded) : boolean
}
class SaleTransaction {
    -ticketNumber: Integer
    -entries: List<TicketEntry>
    -discountRate: double
    -price: double
    -status: String
}
class ReturnTransaction {
    -returnId: Integer
    -saleId: Integer
    -products: HashMap<String, Integer>
    -moneyToReturn: double
    -state: String
}
class AccountBook {
    -operations: List<BalanceOperation>
    -double: currentBalance
    +addBalanceOperation(BalanceOperation): boolean
    +computeBalance() : double
    +getOperationBetween() : List<BalanceOperation>
    +getIdOfNextBalanceOperation: int
}
class BalanceOperation {
    -id: Integer
    -date: LocalDate
    -type: String
    -amount: double
}

class TicketEntry {
    -barCode: String 
    -productDescription: String
    -amount: int 
    -pricePerUnit: double
    -discountRate: double
}

note "Classes in .model\nare persistent" as n2
}
EZShop -left- "1..*" User
EZShop -right- "*" ProductType
EZShop -- "*" Order
EZShop -- "*" Customer
EZShop -- "*" SaleTransaction
EZShop -- "*" ReturnTransaction
Order "*" -- "*" ProductType
SaleTransaction -- "*" TicketEntry
ReturnTransaction -- "*" ProductType
TicketEntry -- "*" ProductType
EZShop -- AccountBook
AccountBook -- "*" BalanceOperation
' Hidden link to better visualize stuff, not real relationships
' Card -[hidden]down- User


@enduml
```

## Verification traceability matrix

<!--for each functional requirement from the requirement document, list which classes concur to implement it>-->

| FR ID | EZShop | User | ProductType | Order | Customer | SaleTransaction | ReturnTransaction | AccountBook | BalanceOperation | TicketEntry |
| :---: | :----: | :--: | :---------: | :---: | :------: | :-------------: | :---------------: | :---------: | :--------------: | :--------------: |
| FR1   | X      | X    |             |       |          |                 |                   |             |                  | <!-- FR1 Manage users and rights (users are Administrator, ShopManager, Cashier) -->
| FR3   | X      | X    | X           |       |          |                 |                   |             |                  |                  | <!-- FR3 Manage product catalog -->
| FR4   | X      | X    | X           | X     |          |                 |                   | X           | X                |                  | <!-- FR4 Manage inventory -->
| FR5   | X      | X    |             |       | X        |                 |                   |             |                  |                  | <!-- FR5 Manage customers and cards -->
| FR6   | X      | X    | X           |       | X        | X               | X                 | X           | X                | X                | <!-- FR6 Manage a sale transaction -->
| FR7   | X      | X    |             |       | X        | X               | X                 | X           | X                |                  | <!-- FR7 Manage payment -->
| FR8   | X      |      |             | X     | X        |                 |                   | X           | X                |                  | <!-- FR8 Accounting-->

## Verification sequence diagrams

### Use case 1, UC1 - Manage products

#### Scenario 1-1: Create product type X

```plantuml
@startuml
GUI -> EZShop : createProductType()
activate EZShop
EZShop -> EZShop : checkProductBarcodeIsUnique()
alt unique
  EZShop -> ProductType : new ProductType()
  activate ProductType
  ProductType -> EZShop : ProductType X
  deactivate ProductType
  EZShop -> EZShop : productTypes.put(X)
  EZShop -> GUI : X.id
else not unique
  EZShop -> GUI : -1
end
deactivate EZShop

GUI -> EZShop : updatePosition(X.id)
activate EZShop
EZShop -> EZShop : checkProductPositionIsValid()
EZShop -> EZShop : checkProductPositionIsFree()
alt position valid and free
  EZShop -> ProductType : X.setPosition()
  EZShop -> GUI : true
else position invalid or not free
  EZShop -> GUI : false
end
deactivate EZShop
@enduml
```

#### Scenario 1-3: Modify product type price per unit

```plantuml
@startuml
GUI -> EZShop : getProductTypeByBarCode()
activate EZShop
EZShop -> EZShop : X = productTypes.find()
alt product found
  EZShop -> GUI : X
  deactivate EZShop
  GUI -> EZShop : updateProduct(X.id)
  activate EZShop
  EZShop -> ProductType : X.setPrice()
  EZShop -> GUI : updateResult
  deactivate EZShop
else product not found
  EZShop -> GUI : null
  deactivate EZShop
end
@enduml
```

### Use case 2, UC2 - Manage users and rights

#### Scenario 2-3: Modify user rights of username X

```plantuml
@startuml
GUI -> EZShop : updateUserRights()
activate EZShop
EZShop -> EZShop : X = users.find()
alt user found
  EZShop -> User : X.setRole(newRole)
  EZShop -> GUI : true
else user not found
  EZShop -> GUI : false
end

deactivate EZShop
@enduml
```

### Use case 3, UC3 - Manage inventory and orders

#### Scenarios 3-1, 3-2: Order of product type X issued (1) and paid (2)

```plantuml
@startuml
GUI -> EZShop : issueOrder(productCode, ...)
activate EZShop
EZShop -> EZShop : X = productTypes.find(productCode)
  EZShop -> Order : new Order(productCode)
  activate Order
  Order -> EZShop : Order Y
  deactivate Order
  EZShop -> Order : Order.send(Y)
  activate Order
  Order -> Order : Y.state = "sent"
  Order -> EZShop : ProductType X
  deactivate Order
EZShop -> GUI : success
deactivate EZShop

GUI -> GUI : Want to pay now? (Yes)

GUI -> EZShop : payOrderFor(productCode, ...)
activate EZShop
EZShop -> EZShop : X = orders.find(productCode)
'EZShop -> EZShop : paySupplier(X.quantity * X.pricePerUnit)
  EZShop -> Order : X.paySupplier()
  activate Order
    Order -> BalanceOperation : new BalanceOperation()
    activate BalanceOperation
    BalanceOperation -> BalanceOperation : amount = X.quantity * X.pricePerUnit
    BalanceOperation -> Order : BalanceOperation Y
    deactivate BalanceOperation
    
    Order -> AccountBook : addBalanceOperation(Y)
    activate AccountBook
    AccountBook -> AccountBook : operations.add(Y)
    AccountBook -> Order : done
    deactivate AccountBook
  Order -> Order : X.state = "paid"
  Order -> EZShop : success
  deactivate Order
EZShop -> GUI : success
deactivate EZShop
@enduml
```

### Use case 4, UC4 - Manage Customers and  Cards

#### Scenarios 4-1, 4-2: Create customer record (1) and Attach Loyalty card (2)

```plantuml
@startuml
GUI -> EZShop : defineCustomer(name)
activate EZShop
EZShop -> EZShop : checkCustomerNameIsUnique(name)
alt name is unique
  EZShop -> EZShop : X = new Customer(name)
  activate Customer
  Customer -> Customer : X.name = name
  Customer -> EZShop : X
  deactivate Customer
  EZShop -> EZShop : customers.put(X.id, X)
  EZShop -> GUI : X.id
  deactivate EZShop
  GUI -> GUI : Assign a new card? (Yes)
  GUI -> EZShop : createCard()
  activate EZShop
  EZShop -> EZShop : C = new Card()
  EZShop -> GUI : C.card
  deactivate EZShop
  GUI -> EZShop : attachCardToCustomer(C.card, X.id)
  activate EZShop
  EZShop -> EZShop : X = getCustomer(X.id)
  EZShop -> EZShop : checkCustomerCardIsUnique(C.card)
  EZShop -> Customer : X.setCard(C.card)
  EZShop -> GUI : true
else name not unique
  EZShop -> GUI : -1
end
deactivate EZShop

@enduml
```

### Use case 5, UC5 - Authenticate, authorize

#### Scenario 5-1: Login

```plantuml
@startuml
GUI -> EZShop : login()
activate EZShop
EZShop -> EZShop : X = users.find()
alt user found
  EZShop -> User : X.checkPasswordMatches()
  activate User
  User -> EZShop : true/false
  deactivate User
  alt password matches
    EZShop -> GUI : X
    else wrong password
    EZShop -> GUI : null
  end
  else user not found
  EZShop -> GUI : null
end
deactivate EZShop
@enduml
```

### Use case 6, UC6 - Manage sale transaction

#### Scenario 6-2: Sale of product type X with product discount

```plantuml
@startuml
GUI -> EZShop : startSaleTransaction()
activate EZShop
  EZShop -> SaleTransaction: new SaleTransaction()
  activate SaleTransaction
  SaleTransaction -> EZShop : T
  deactivate SaleTransaction
  EZShop -> EZShop : saleTransactions.put(T)
EZShop -> GUI : T.id

loop Until there are products
  GUI -> EZShop : addProductToSale(transactionId, productCode, amount)
    EZShop -> EZShop : T = saleTransactions.get(transactionId)
    EZShop -> SaleTransaction : T.addProduct(productCode, amount)
    activate SaleTransaction
      SaleTransaction -> SaleTransaction : T.productsQuantity.put(productCode, amount)
    SaleTransaction -> EZShop : done
    deactivate SaleTransaction
  EZShop -> GUI : success
end

loop Until there are discounts to apply
  GUI -> EZShop : applyDiscountRateToProduct(transactionId, productCode, discountRate)
    EZShop -> EZShop : T = saleTransactions.get(transactionId)
    EZShop -> SaleTransaction : T.ApplyDiscountRateToProduct(productCode, discountRate)
    activate SaleTransaction
      SaleTransaction -> SaleTransaction : T.productsDiscounts.put(productCode, discountRate)
    SaleTransaction -> EZShop : done
    deactivate SaleTransaction
  EZShop -> GUI : success
end
deactivate EZShop

GUI -> EZShop : receiveCashPayment(), receiveCreditCardPayment() : see Use case 7
activate EZShop
EZShop -> GUI : amount, success
deactivate EZShop

GUI -> EZShop : endSaleTransaction()
activate EZShop
EZShop -> GUI : success
deactivate EZShop
@enduml
```

### Use case 7, UC7 - Manage payment

#### Scenario 7-1: Manage payment by valid credit card

```plantuml
@startuml
GUI -> EZShop : receiveCreditCardPayment(transactionId, creditCard)
activate EZShop
  EZShop -> EZShop : T = saleTransactions.get(transactionId)
  EZShop -> SaleTransaction : T.computeTotal()
  SaleTransaction -> EZShop : amount
  
  EZShop -> EZShop : validateCreditCard(creditCard)
  alt creditCard number is valid
    EZShop -> EZShop : coordinate transaction(creditCard)
    alt Bank transaction successful
      EZShop -> BalanceOperation : new BalanceOperation(date, type="Credit Card", amount=amount)
      activate BalanceOperation
        BalanceOperation -> EZShop : BalanceOperation op
      deactivate BalanceOperation

      EZShop -> AccountBook : addBalanceOperation(op)
      activate AccountBook
        AccountBook -> AccountBook : operations.add(op)
        AccountBook -> EZShop
      deactivate AccountBook
      EZShop -> GUI : true
    else transaction refused
      EZShop -> GUI : false
    end
  else user not found
    EZShop -> GUI : false
  end
deactivate EZShop
@enduml
```

### Use case 8, UC8 - Manage return transaction

#### Scenario 8-2: Return transaction of product type X completed, cash

```plantuml
@startuml
GUI -> EZShop : startReturnTransaction(saleId)
activate EZShop
EZShop -> GUI : R.id
GUI -> EZShop : returnProduct(R.id, prodId, amount)
EZShop -> ProductType : updateQuantity(prodId, amount)
EZShop -> ReturnTransaction : endReturnTransaction(R.id)
EZShop -> ReturnTransaction : returnCashPayment(R.id)
activate ReturnTransaction
ReturnTransaction -> ReturnTransaction : R.computeTotal()
ReturnTransaction -> AccountBook : addBalanceOperation()
ReturnTransaction -> EZShop : change
deactivate ReturnTransaction
EZShop -> GUI : change

  ' alt SaleTransaction T exists
  '     EZShop -> AccountBook : addBalanceOperation(op)
  '     activate AccountBook
  '     AccountBook -> AccountBook : operations.add(op)
  '     AccountBook -> EZShop
  '     deactivate AccountBook
  '     EZShop -> ProductType : updateQuantity()
  '     activate ProductType
  '     ProductType -> EZShop : success
  '     deactivate ProductType
  '     EZShop -> GUI : amount
  ' else SaleTransaction not found
  '     EZShop -> GUI : 0.0
  ' end
deactivate EZShop
@enduml
```

### Use case 9, UC9 - Accounting

#### Scenario 9-1: List credits and debits

```plantuml
@startuml
GUI -> EZShop : getCreditsAndDebits()
activate EZShop
EZShop -> AccountBook: X = accountBook.getOperationsBetween()
alt list found 
  EZShop -> GUI : X
else list not found
  EZShop -> GUI : error
end
deactivate EZShop
@enduml
```
