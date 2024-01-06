# Requirements Document

Authors: Alessio Santangelo, Andrea Cencio, Damiano Bonaccorsi, Lorenzo Chiola

Date: 20 April 2021

Version: 1.2.1

## Contents

- [Essential description](#essential-description)
- [Stakeholders](#stakeholders)
- [Context Diagram and interfaces](#context-diagram-and-interfaces)
  - [Context Diagram](#context-diagram)
  - [Interfaces](#interfaces)
- [Stories and personas](#stories-and-personas)
- [Functional and non functional requirements](#functional-and-non-functional-requirements)
  - [Functional Requirements](#functional-requirements)
  - [Access right, actor vs function](#access-right-actor-vs-function)
  - [Non functional requirements](#non-functional-requirements)
- [Use case diagram and use cases](#use-case-diagram-and-use-cases)
  - [Use case diagram](#use-case-diagram)
  - [Use cases](#use-cases)
- [Glossary](#glossary)
- [System design](#system-design)
- [Deployment diagram](#deployment-diagram)

## Essential description

Small shops require a simple application to support the owner or manager. A small shop (ex a food shop) occupies 50-200 square meters, sells 500-2000 different item types, has one or a few cash registers.
EZShop is a software application to:

- manage sales
- manage inventory
- manage customers
- support accounting

## Stakeholders

| Stakeholder name  | Description |
| :---------------- | :---------- |
|   Administrator   | Supervises the application (fixing bugs and inserting new features if needed). He also manages users' accounts |
|   User            | Uses the application |
|   Manager         | He has access to all the functionalities of the application |
|   Owner           | Funds the development and operation of the application, could also be the manager. He can use the application to obtain accounting informations |
|   Cashier         | Handles sales (interacts with EZShop with low privileges) |
|   Barcode reader     | Scans products' barcodes |
|   Receipt printer   | Prints sales' receipts |
|   Credit card reader  | Reads credit cards' data to be used by Credit card service |
|   Credit card service  | Allows credit card payments via Internet |

## Context Diagram and interfaces

### Context Diagram

```plantuml
top to bottom direction
actor Administrator as a
actor User as u
actor Manager as m
actor Cashier as c
actor Owner as o
actor BarcodeReader as br
actor ReceiptPrinter as rp
actor CreditCardReader as ccr
actor CreditCardService as ccs
m -up-|> u
o -up-|> u
c -up-|> u
a -> (EZShop)
u -> (EZShop)
br -> (EZShop)
rp -> (EZShop)
ccr -> (EZShop)
ccs -> (EZShop)
```

### Interfaces

| Actor         | Logical Interface | Physical Interface  |
| :------------ | :---------------- | :------------------ |
| Administrator | Development tools, GUI | Screen, keyboard and mouse on PC |
| Manager, Owner, Cashier | GUI  | Screen, keyboard and mouse on PC |
| Receipt printer, Credit card reader | First party drivers | USB |
| Barcode reader | HID keyboard emulation | USB |
| Credit card service | REST API: <https://stripe.com/docs/api> | Internet Link |

## Stories and personas

Greg is 42, he owns a little grocery store in the city center. The activity has only another employee (a cashier) and, for this reason, Greg is very busy managing all the administrative and managerial part of his shop. He would also want to cut some expenses in order to increase his incomes but he doesn’t have time to properly analyze which could be the useless expenses.

Amanda is a 33 y.o. cashier of a medium size food shop. Due to the size of the supermarket, she is not only a cashier but can assume a management role. Most of her work is about interacting with clients, but she often needs to manage the inventory. For this reason, she would like to know in real-time the inventory of the products to always keep the shelves full.

Damiano is a high-school student, he works the evening shift at the liquor store. Due to his schedule, he handles sales at the busiest hours of the day (4~7 PM) and he cannot keep up with the line of customers with a traditional cash register. He currently annotates a mark on paper for each of the most common items he sells. The owner actually suspects that he may be stealing some wares, but he cannot easily see discrepancies between the income and the stock at the end of the night without keeping track of every transaction.

Lillo is 35, he's the manager of this minimarket called TheMiniShop. He is a perfectionist and, for this reason, he hates when some product isn't correctly registered in the inventory or when he is not able to properly manage the accounting of his shop due to the wrong data given by cashiers. He also think adding other payment methods to the shop would give a lot of benefits.

## Functional and non functional requirements

### Functional Requirements

| ID        | Description  |
| :-------- | :----------- |
| FR1       | Handle sales |
|  FR1.1    | Credit card payment |
|  FR1.2    | Cash payment |
| FR2       | Manage inventory |
|  FR2.1    | Add product info to the inventory |
|  FR2.2    | Remove product info from the inventory |
|  FR2.3    | Edit name, quantity or price of item in inventory |
|  FR2.4    | Order products from suppliers |
| FR3       | Manage customers |
|  FR3.1    | Add customer |
|  FR3.2    | Remove customer |
|  FR3.3    | Print list of all the registered customer |
| FR4       | Manage accounting |
|  FR4.1    | Compute balance over a specified time period |
|  FR4.2    | Compute expenses over a specified time period |
|  FR4.3    | Compute income over a specified time period |
| FR5       | User authentication |
| FR6       | Manage users |
|  FR6.1    | Define a new user and its privileges |
|  FR6.2    | Delete a user |

### Access right, actor vs function

| Function                  | Administrator | Manager | Owner | Cashier |
| :------------------------ | :-----------: | :-----: | :---: | :-----: |
| FR1 - Handle sales        | no | no | no | yes |
| FR2 - Manage inventory    | no | yes | no | no |
| FR3 - Manage customers    | no | yes | no | no |
| FR4 - Manage accounting   | no | yes | yes | no |
| FR5 - User authentication | no | yes | yes | yes |
| FR6 - Manage users        | yes | no | no | no |

### Non Functional Requirements

| ID       | Type            | Description            | Refers to |
| :------- | :-------------- | :--------------------- | :--------:|
| NFR1     | Usability       | Application should be used with a training time averaging 1 hour in total | All FR |
| NFR2     | Performance     | All functions should complete in < 0.5 sec  | All FR |
| NFR3     | Correctness     | All the accounting computations must have no errors, while errors on sales are bound to humans’ mistakes | FR2 - FR4 |
| NFR5     | Privacy         | The application stores only the essential customers’ data required for the application itself | FR3 |
| NFR6     | Security        | The application has to be accessed by authorized users only, by means of a login system | All FR |
| NFR7     | Portability     | The application should be compatible with every machine running Microsoft Windows 7 or following | All FR |

## Use case diagram and use cases

### Use case diagram

```plantuml
top to bottom direction
actor Administrator as a
actor User as u
actor Manager as m
actor Owner as o
actor Cashier as c
actor BarcodeReader as br
actor ReceiptPrinter as rp
actor CreditCardReader as ccr
actor CreditCardService as ccs

m -up-|> u
o -up-|> u
c -up-|> u
c --> (Handle sales)
(Handle sales) .> (Update stock amount) :<<include>>
ccs <--> (Handle sales)
ccr --> (Handle sales)
br --> (Handle sales)
(Handle sales) --> rp
br --> (Manage inventory)
m --> (Manage inventory)
m --> (Manage customers)
m --> (Manage accounting)
o --> (Manage accounting)
u --> (Authentication)
a --> (Manage users)
```

### Use Cases

#### Use case 1, UC1 - Handle sales

| Actors Involved   | Cashier, Barcode system, Credit card reader, Credit card service, Receipt printer |
| :------------- | :------------- |
|  Precondition     | Cashier logged in |  
|  Post condition     | Inventory updated, Sales history updated, Receipt printed |
|  Nominal Scenario     | Scan products, compute total and handle payment |
|  Variants     | Payment refused; some product not recognised|

##### Scenario 1.1

| Scenario 1.1   | Credit card payment |
| :------------- | :------------- |
| Precondition   | Internet access, Cashier logged in |
| Post condition | Payment accepted, Inventory updated, Sales history updated, Receipt printed |
| Step# | Description |
| 1     | Start sale transaction |  
| 2     | Use barcode reader to retrieve the product’s code |
| 3     | Retrieve product information from the internal database |
|       | Repeat 2 and 3 for all products in the cart |
| 4     | Compute total |
| 5     | Read credit card's data |
| 6     | Send payment informations to the API and wait for response |
| 7     | Check if the payment is successful |
| 8     | Subtract quantity of product from stock if payment was successful |
| 9     | Save sale information (date, quantity of products sold and total price) in the sales history |
| 10    | Print receipt |
| 11    | Close transaction |

##### Scenario 1.2

| Scenario 1.2   | Credit card payment |
| :------------- | :------------- |
| Precondition   | Internet access, Cashier logged in |
| Post condition | Payment refused |
| Step# | Description |
| 1     | Start sale transaction |  
| 2     | Use barcode reader to retrieve the product’s code |
| 3     | Retrieve product information from the internal database |
|       | Repeat 2 and 3 for all products in the cart |
| 4     | Compute total |
| 5     | Read credit card's data |
| 6     | Send payment informations to the API and wait for response |
| 7     | Payment refused |
| 8     | Try again or return to sale screen |

##### Scenario 1.3

| Scenario 1.3   | Cash payment |
| :------------- | :----------- |
| Precondition   | Cashier logged in |
| Post condition | Payment accepted, Inventory updated, Sales history updated, Receipt printed |
| Step# | Description |
| 1     | Start sales transaction |
| 2     | Use barcode system to retrieve the product’s code |
| 3     | Retrieve product information from the internal database |
|       | Repeat 2 and 3 for all products in the cart |
| 4     | Compute total |
| 5     | Select card payment |
| 6     | The cashier inputs the received cash amount to EZShop |
| 7     | EZShop communicates the change to the cashier |
| 8     | Payment completed |
| 9     | Subtract quantity of product from stock |
| 10    | Save sales information (quantity of products sold and total price) in the sales history |
| 11    | Print receipt |
| 12    | Close transaction |

#### Use case 2, UC2 - Manage inventory

| Actors Involved  | Manager |
| :--------------- | :------ |
| Precondition     | Manager logged in |
| Post condition   | Inventory updated |
| Nominal Scenario | Insert/delete/modify products info to/from the inventory |
| Variants         | Some barcodes not recognised; Product to be added already present |

##### Scenario 2.1

| Scenario 2.1   | Add product info to the inventory |
| :------------- | :------------- |
| Precondition   | Product not already in inventory; Logged in as manager |
| Post condition | Product info inserted |
| Step#          | Description |
| 1              | Start adding procedure |
| 2              | Scan barcode or insert product's code |
| 3              | Insert product information (at least price and barcode) |
| 4              | Confirm and conclude adding procedure |

##### Scenario 2.2

| Scenario 2.2   | Delete product info from the inventory |
| :------------- | :------------- |
| Precondition   | Barcode must be valid; Logged in as manager |
| Post condition | Product info deleted |
| Step#          | Description |
| 1              | Start searching procedure |
| 2              | Scan barcode or insert product's code |
| 3              | Click on Delete button |
| 4              | Ask for deletion confirmation |
| 5              | Finally delete the product info |

##### Scenario 2.3

| Scenario 2.3   | Edit quantity/info of product in inventory |
| :------------- | :------------- |
| Precondition   | Barcode must be valid; Logged in as manager |
| Post condition | Product info updated |
| Step#          | Description  |
| 1              | Start searching procedure |
| 2              | Scan barcode or insert product's code |
| 3              | Click on Edit button |
| 4              | Enter new quantity/info |
| 5              | Ask for edit confirmation |
| 6              | Finally update the product info |

#### Use case 3, UC3 - Manage customers

| Actors Involved  | Manager |
| :--------------- | :------------ |
| Precondition     | Logged in as manager |  
| Post condition   | Customer informations updated |
| Nominal Scenario | Insert/delete/update/print list of customers|
| Variants         | - |

##### Scenario 3.1

| Scenario 3.1   | Insert new customer |
| :------------- | :------------- |
| Precondition   | Knowing customer informations, Logged in as manager |
| Post condition | Customer informations registered |
| Step#          | Description |
| 1              | Start adding procedure |
| 2              | Insert customers information |
| 3              | Confirm and conclude adding procedure |  

##### Scenario 3.2

| Scenario 3.1   | Edit customer info |
| :------------- | :------------- |
| Precondition   | Knowing customer informations, Logged in as manager |
| Post condition | Customer not found |
| Step#          | Description |
| 1              | Start searching procedure |
| 2              | Insert search parameters |
| 3              | Customer not found |
| 4              | Add new customer or return to customers screen |  

#### Use case 4, UC4 - Manage accounting

| Actors Involved  | Manager or Owner |
| :--------------- | :------------- |
| Precondition     | Logged in as Manager or Owner |  
| Post condition   | Accounting informations retrieved |
| Nominal Scenario | Compute and display some accounting information |
| Variants         | - |

##### Scenario 4.1

| Scenario 4.1   | Compute balance based on sales and orders history |
| :------------- | :-------------|
| Precondition   | Having a proper sales and orders history, Logged in as Manager or Owner |
| Post condition | Balance displayed |
| Step#          | Description |
| 1              | Start computing balance procedure |
| 2              | Ask the time period over which the balance has to be computed |  
| 3              | Compute results |
| 4              | Display results |

#### Use case 5, UC5 - User authentication

| Actors Involved  | User |
| :--------------- | :------------- |
| Precondition     | Having an account (Either Manager, Owner or Cashier) |  
| Post condition   | Logged in |
| Nominal Scenario | Log in |
| Variants         | Wrong credentials |

##### Scenario 5.1

| Scenario 5.1   | Logging in |
| :------------- | :-------------|
| Precondition   | Having an account (Either Manager, Owner or Cashier) |  
| Post condition | Logged in |
| Step#          | Description |
| 1              | Open the app |  
| 2              | Insert user and password |
| 3              | Click the Login Button |  

#### Use case 6, UC6 - Manage users

| Actors Involved  | Administrator |
| :--------------- | ------------- |
| Precondition     | - |  
| Post condition   | Update users informations |
| Nominal Scenario | Create, delete or modify credentials and privileges about users |
| Variants         | - |

##### Scenario 6.1

| Scenario 6.1   | Create a User |
| :------------- | :-------------|
| Precondition   | - |  
| Post condition | User account created |
| Step#          | Description |
| 1              | Open the development tools |  
| 2              | Create new user account |
| 3              | Define permissions for user account |
| 4              | Add user account to internal users' database |

## Glossary

```plantuml
class EZShop
class User {
 account_name
 account_pwd
 email
}
class Administrator
class Manager
class Owner
class Cashier
note "If there is a single person that has\ndifferent roles (i.e. Cashier and Manager)\nit has to log-in with different accounts\ndepending on the current role" as N1
N1 .. User
note "He doesn’t access the application through\nthe log-in page but he directly interacts\nwith the code of the application" as N2
N2 .. Administrator
note "Keeping track of all sales and orders\nis necessary for accounting" as N3
N3 .. Sale
N3 .. Order


class Product {
 ID
 name
 price_per_unit
 quantity_in_stock
}
class Customer {
  name
  surname
  fidelity_card_number
  telephone_number
}
class Sale{
 number
 date
 total_price
}
class Order{
  number
  date
  total_cost
  supplier
}

Manager -up-|> User
Owner -up-|> User
Cashier -up-|> User
EZShop -- Administrator
EZShop -- "*" User
EZShop -- "*" Sale
EZShop -- "*" Order
Product "*" -- "*" Sale
Product "*" -- "*" Order
EZShop -- "*" Product
EZShop -- "*" Customer
```

<!-- - Inventory: Quantity of goods in stock. Different from the verb “to inventory”, which indicates the action of checking that the inventory actually reflects the quantities of goods in stock and/or on display. -->

## System Design

Not really meaningful in this case. Only software components are needed. <!-- maybe add supported platforms? -->

## Deployment Diagram

```plantuml
artifact "EZShop Application" as ezshop
node "PC" as pc
pc -- ezshop
```
