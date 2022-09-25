package service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.springframework.stereotype.Component;

import dao.FlooringMasteryOrderDAO;
import dao.FlooringMasteryProductDAO;
import dao.FlooringMasteryTaxDAO;
import dto.Order;
import dto.Product;
import dto.Tax;

@Component
public class FlooringMasteryServiceImpl implements FlooringMasteryService{
	
    private FlooringMasteryOrderDAO orderDao;
    private FlooringMasteryProductDAO productDao;
    private FlooringMasteryTaxDAO taxDao;
    
    private final String customerCommaPlaceHolder = "~#~";

    public FlooringMasteryServiceImpl(FlooringMasteryOrderDAO orderDao, FlooringMasteryProductDAO productDao, FlooringMasteryTaxDAO taxDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxDao = taxDao;
    }
	
	//============================== 1. DISPLAY ORDERS ======================================
    public String createOrderFileNameFromDate(LocalDate date){
        //Convert YYYY-MM-DD to MMDDYYYY
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
        String dateFormatted = date.format(formatter);
        
        return "Orders_"+dateFormatted+".txt";
    }
    
    public void checkOrderFileExists (String orderFileName) throws FlooringMasteryOrderException {
        String [] orderFiles = orderDao.listAllOrderFiles();
        String orderfile = null;
        for (String orderFile : orderFiles){
            if (orderFileName.equals(orderFile)) {//compare the order file names to the file name given
                orderfile = orderFileName;
                if (orderfile!=null) {//if it is found, set name to the orderfile name
                    break;
                }
            }
        }
        if (orderfile == null){
            throw new FlooringMasteryOrderException (
            "ERROR: no orders exist for that date.");
        }
    }
    
    public Collection<Order> getAllOrders (String fileWithDate) throws FileNotFoundException {  
        //Lists all the orders from the file specified.
        return orderDao.getAllOrdersForADate(fileWithDate);
    }

    public Collection<Order> getOrderList (LocalDate wantedOrderDate) throws FlooringMasteryOrderException, FileNotFoundException {
        String fileWithDate = createOrderFileNameFromDate(wantedOrderDate);
        checkOrderFileExists(fileWithDate);
        return getAllOrders(fileWithDate);
        
    }
    
	//============================== 2. ADD AN ORDER ========================================
	//------------------------------ a. Order Date ------------------------------------------
    public LocalDate checkDateIsInFuture(LocalDate orderDate) throws FlooringMasteryOrderException{
        LocalDate dateNow = LocalDate.now();
        if (orderDate.compareTo(dateNow)<0){
            throw new FlooringMasteryOrderException (
            "ERROR: Date must be in the future.");
        }
        return orderDate;
    }
    
	//------------------------------ b. Customer Name ---------------------------------------
    public void validateCustomerName(String customerNameInput)throws FlooringMasteryOrderException {
        if (customerNameInput.isBlank()  || customerNameInput.isEmpty()) {
            throw new FlooringMasteryOrderException (
                    "ERROR: customer name cannot be blank.");
                }
    }
     
    public String getCustomerNamePlaceHolder(String customerNameInput) {
        //Replace commas for something else to not interfere with the delimiter
        return customerNameInput.replace(",", customerCommaPlaceHolder);
    }
    
	//------------------------------ c. State -----------------------------------------------
    public void checkStateAgainstTaxFile(String stateAbbreviationInput) throws FlooringMasteryOrderException, FileNotFoundException {
        Collection<Tax> taxesList = taxDao.getAllTaxes();
        String stateAbbreviation = null;
       for (Tax tax:taxesList) {
           if (tax.getStateAbbr().equalsIgnoreCase(stateAbbreviationInput)) {
               stateAbbreviation = tax.getStateAbbr();
               if (stateAbbreviation!=null){
                   break;
               }
           }
       }
       if (stateAbbreviation == null) {
           throw new FlooringMasteryOrderException (
           "ERROR: we cannot sell to " + stateAbbreviation + ".");
       }
    }
    
	//------------------------------ d. Product Type ----------------------------------------
    public Collection<Product> getAllProducts() throws FileNotFoundException {
        return productDao.getAllProducts();
    }
    
    public void checkProductTypeAgainstProductsFile (String productTypeInput) throws FlooringMasteryOrderException, FileNotFoundException {
    	Collection<Product> productList = productDao.getAllProducts();
        String productType = null;
        
        for (Product product:productList) {
            if (product.getProductType().equalsIgnoreCase(productTypeInput)) {
                productType = product.getProductType();
            }
        }
        if (productType == null) {
            throw new FlooringMasteryOrderException (
                    "ERROR: " + productTypeInput + " is not in the product list.");
        }
    }
     
    public Product getProduct (String productType) throws FileNotFoundException {
        return productDao.getProduct(productType);
    }
    
	//------------------------------ e. Area ------------------------------------------------
    public void checkAreaOverMinOrder (BigDecimal areaInput) throws FlooringMasteryOrderException {
        if (areaInput.compareTo(new BigDecimal("100"))<0) {
            throw new FlooringMasteryOrderException (
            "ERROR: the area is below the minimum order");
        }
     }
    
	//------------------------------ f. Calcs -----------------------------------------------
    public BigDecimal calculateMaterialCost(BigDecimal area, BigDecimal costPerSquareFoot) {
        return area.multiply(costPerSquareFoot).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateLaborCost(BigDecimal area, BigDecimal laborCostPerSquareFoot) {
        return area.multiply(laborCostPerSquareFoot).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(BigDecimal materialCost, BigDecimal laborCost, BigDecimal taxRate) {
        return (materialCost.add(laborCost)).multiply(taxRate.divide(new BigDecimal("100"))).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(BigDecimal materialCost, BigDecimal laborCost, BigDecimal tax) {
        return materialCost.add(laborCost).add(tax).setScale(2, RoundingMode.HALF_UP);
    }
    
	//------------------------------ g. Tax -------------------------------------------------
    public Tax getTax(String stateAbbreviationInput) throws FileNotFoundException {
        return taxDao.getTax(stateAbbreviationInput);
    }
    
	//------------------------------ h. New Order -------------------------------------------
    public int generateNewOrderNum() throws FileNotFoundException {
    	Collection<Integer> orderNums = orderDao.getAllOrderNums();
        int maxOrderNum = Collections.max(orderNums);
        return maxOrderNum + 1;
    }
 
    public Order createNewOrder(String verifyOrder, LocalDate orderDateInput, int orderNumber, String customerNameInput, String stateAbbreviationInput, BigDecimal taxRate, String productTypeInput,
                    BigDecimal areaInput, BigDecimal CostPerSquareFoot, BigDecimal laborCostPerSquareFoot, BigDecimal materialCost, BigDecimal laborCost, BigDecimal tax, BigDecimal total) 
                    		throws IOException {
       Order newOrder;
       if (verifyOrder.equalsIgnoreCase("Y")){
           newOrder = new Order();
           newOrder.setOrderNumber(orderNumber);
           newOrder.setCustomerName(customerNameInput);
           newOrder.setStateAbbr(stateAbbreviationInput);
           newOrder.setTaxRate(taxRate);
           newOrder.setProductType(productTypeInput);
           newOrder.setArea(areaInput);
           newOrder.setCostPerSquareFoot(CostPerSquareFoot);
           newOrder.setLaborCostPerSquareFoot(laborCostPerSquareFoot);
           newOrder.setMaterialCost(materialCost);
           newOrder.setLaborCost(laborCost);
           newOrder.setTax(tax);
           newOrder.setTotal(total);

           String newOrderFileName = createOrderFileNameFromDate(orderDateInput);
           String [] orderFiles = orderDao.listAllOrderFiles();
           String fileExists = null;

           for (String orderFile : orderFiles){
               if (newOrderFileName.equals(orderFile)) {
                fileExists = newOrderFileName;
                Order orderCreated = orderDao.addOrderToExistingFile(fileExists, orderNumber, newOrder);
                return orderCreated;          
                }
            }

	        if (fileExists == null){
	            Order orderCreated = orderDao.addOrderToNewFile(newOrderFileName, orderNumber, newOrder);
	            return orderCreated;
	        }
        }
        return null;
    }
   
	//============================== 3. EDIT AN ORDER =======================================
    public String checkEdit (String updatedInfo) {
        if (updatedInfo == null 
                || updatedInfo.trim().length()==0
                || updatedInfo.isEmpty()
                || updatedInfo.isBlank()) {
            return null;
        } else {
            return updatedInfo;
        }
    }
    
    public BigDecimal checkEditBigDecimal (String updatedInfo) {
        if (updatedInfo == null 
                || updatedInfo.trim().length()==0
                || updatedInfo.isEmpty()
                || updatedInfo.isBlank()) {
            return null;
        } else {
            return new BigDecimal(updatedInfo);
        }
    }
    
    public Order updateOrderCustomerName(String updatedCustomerName, Order orderToEdit) {
        if (updatedCustomerName == null) {
            return orderToEdit;
        } else {
            orderToEdit.setCustomerName(updatedCustomerName);
            return orderToEdit;
        }
    }
    
    public Order updateOrderState(String updatedState, Order orderToEdit){
        if (updatedState == null) {
            return orderToEdit;
        } else {
            orderToEdit.setStateAbbr(updatedState);
            return orderToEdit;
        }
    }
    
    public Order updateOrderProductType(String updatedProductType, Order orderToEdit) {
        if (updatedProductType == null) {
            return orderToEdit;
        } else {
            orderToEdit.setProductType(updatedProductType);
            return orderToEdit;
        }
    }
    
    public Order updateOrderArea(BigDecimal updatedArea, Order orderToEdit){
        if (updatedArea == null) {
            return orderToEdit;
        } else {
            orderToEdit.setArea(updatedArea);
            return orderToEdit;
        }
    }
    
    public Order updateOrderCalculations(Order editedOrder) throws FileNotFoundException {
        BigDecimal updatedTaxRate = null;
        BigDecimal updatedCostPerSquareFoot = null;
        BigDecimal updatedLaborCostPerSquareFoot = null;

        String updatedStateAbbreviation = editedOrder.getStateAbbr();
        if (updatedStateAbbreviation != null) {
            Tax updatedTaxObj = taxDao.getTax(updatedStateAbbreviation);
            updatedTaxRate = updatedTaxObj.getTaxRate();
        }
        
        String updatedProductType = editedOrder.getProductType();
        if (updatedProductType != null) {
            Product updatedProduct = productDao.getProduct(updatedProductType);
            updatedCostPerSquareFoot = updatedProduct.getCostPerSquareFoot();
            updatedLaborCostPerSquareFoot = updatedProduct.getLaborCostPerSquareFoot();
            BigDecimal updatedArea = editedOrder.getArea();

            BigDecimal updatedMaterialCost = this.calculateMaterialCost(updatedArea, updatedCostPerSquareFoot);
            BigDecimal updatedLaborCost = this.calculateLaborCost(updatedArea, updatedLaborCostPerSquareFoot);
            BigDecimal updatedTax = this.calculateTax(updatedMaterialCost, updatedLaborCost, updatedTaxRate);
            BigDecimal updatedTotal = this.calculateTotal(updatedMaterialCost, updatedLaborCost, updatedTax);
            editedOrder.setMaterialCost(updatedMaterialCost);
            editedOrder.setLaborCost(updatedLaborCost);
            editedOrder.setTax(updatedTax);
            editedOrder.setTotal(updatedTotal);
        }
       return editedOrder;
    }
    
    public Order editOrder(String toBeEdited, String orderFile, Order updatedOrder) throws IOException {
        if (toBeEdited.equalsIgnoreCase("Y")) {
            Order editedOrder = orderDao.editOrder(orderFile, updatedOrder);
            return editedOrder;
        } 
        return null;
    }
    
	//============================== 4. REMOVE AN ORDER =====================================
    public Order removeOrderIfConfirmed(String removeConfirmation, String orderFile, int orderNumber) throws IOException {
        if (removeConfirmation.equalsIgnoreCase("Y")) {
            Order removedOrder = orderDao.deleteOrder(orderFile, orderNumber);
            removeFileIfEmpty(orderFile);
            return removedOrder;
        }
        return null;
    }
    
    private void removeFileIfEmpty (String orderFile) throws IOException {
        ArrayList<Order> orderList = (ArrayList<Order>) this.getAllOrders(orderFile);
        if (orderList.isEmpty()) {
            orderDao.deleteOrderFile(orderFile);
        }
    }
    
    public int checkOrderNumExists(String orderFileName, int orderNumberInput) throws FileNotFoundException, FlooringMasteryOrderException {
    	Collection<Integer> orderNums = orderDao.getAllOrderNumsForADate(orderFileName);
        
        int orderNumFound = 0;
        for (Integer orderNum : orderNums){
            if (orderNumberInput == orderNum) {
                orderNumFound=orderNum;
                return orderNumFound;
            }
        }
        if (orderNumFound == 0) {
            throw new FlooringMasteryOrderException (
            "ERROR: no orders exist for that order number.");
    }
        return 0;
    }
    
    public Order getOrder(String fileName, int orderNum) throws FileNotFoundException  {
        return orderDao.getOrder(fileName, orderNum);
    }
    
	//============================== 5. EXPORT ALL DATA =====================================
    public void exportAllData() throws IOException  {
        orderDao.writeDataExport();
    }
}
