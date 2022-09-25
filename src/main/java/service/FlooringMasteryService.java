package service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

import dto.Order;
import dto.Product;
import dto.Tax;

public interface FlooringMasteryService {
//============================== 1. DISPLAY ORDERS ======================================
void checkOrderFileExists (String orderfileName) throws FlooringMasteryOrderException;
String createOrderFileNameFromDate(LocalDate date);
Collection<Order> getAllOrders(String fileWithDate) throws FileNotFoundException;
Collection<Order> getOrderList(LocalDate wantedOrderDate) throws FileNotFoundException, FlooringMasteryOrderException;
//============================== 2. ADD AN ORDER ========================================
//------------------------------ a. Order Date ------------------------------------------
LocalDate checkDateIsInFuture(LocalDate orderDate) throws FlooringMasteryOrderException;
//------------------------------ b. Customer Name ---------------------------------------
String getCustomerNamePlaceHolder(String customerNameInput);
void validateCustomerName(String customerNameInput) throws FlooringMasteryOrderException;
//------------------------------ c. State -----------------------------------------------
void checkStateAgainstTaxFile(String stateAbbreviationInput) throws FlooringMasteryOrderException, FileNotFoundException;
//------------------------------ d. Product Type ----------------------------------------
Collection<Product> getAllProducts() throws FileNotFoundException;
void checkProductTypeAgainstProductsFile(String productTypeInput) throws FlooringMasteryOrderException, FileNotFoundException;
Product getProduct(String productType) throws FileNotFoundException;
//------------------------------ e. Area ------------------------------------------------
void checkAreaOverMinOrder(BigDecimal areaInput) throws FlooringMasteryOrderException;
//------------------------------ f. Calcs -----------------------------------------------
BigDecimal calculateMaterialCost(BigDecimal area,BigDecimal costPerSquareFoot);
BigDecimal calculateTax(BigDecimal materialCost, BigDecimal laborCost,BigDecimal taxRate);
BigDecimal calculateTotal(BigDecimal materialCost, BigDecimal laborCost, BigDecimal tax);
BigDecimal calculateLaborCost(BigDecimal area,BigDecimal laborCostPerSquareFoot);  
//------------------------------ g. Tax -------------------------------------------------
Tax getTax(String stateAbbreviationInput) throws FileNotFoundException;
//------------------------------ h. New Order -------------------------------------------
int generateNewOrderNum() throws FileNotFoundException;
Order createNewOrder(String verifyOrder, LocalDate orderDateInput, int orderNumber, String customerNameInput, String stateAbbreviationInput, BigDecimal taxRate, String productTypeInput,BigDecimal areaInput, BigDecimal CostPerSquareFoot, BigDecimal laborCostPerSquareFoot, BigDecimal materialCost, BigDecimal laborCost, BigDecimal tax, BigDecimal total) throws IOException;
//============================== 3. EDIT AN ORDER =======================================
String checkEdit(String updatedInfo);
BigDecimal checkEditBigDecimal (String updatedInfo);
Order updateOrderCustomerName(String updatedCustomerName, Order orderToEdit);
Order updateOrderState(String updatedState, Order orderToEdit);
Order updateOrderProductType(String updatedProductType, Order orderToEdit);
Order updateOrderArea(BigDecimal updatedArea, Order orderToEdit);
Order updateOrderCalculations(Order editedOrder) throws FileNotFoundException;
Order editOrder(String toBeEdited, String orderFile, Order updatedOrder) throws IOException;
//============================== 4. REMOVE AN ORDER =====================================
Order removeOrderIfConfirmed(String removeConfirmation, String orderFile, int orderNumber) throws IOException;
int checkOrderNumExists(String orderFileName, int orderNumberInput) throws FileNotFoundException, FlooringMasteryOrderException;
Order getOrder(String fileName, int orderNum) throws FileNotFoundException;
//============================== 5. EXPORT ALL DATA =====================================
void exportAllData() throws IOException;
}
