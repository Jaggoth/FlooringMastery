package controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Collection;

import org.springframework.stereotype.Component;

import dto.Order;
import dto.Product;
import dto.Tax;
import service.FlooringMasteryOrderException;
import service.FlooringMasteryService;
import view.FlooringMasteryView;
import view.UserIO;
import view.UserIOConsoleImpl;

@Component
public class FlooringMasteryController {

    private FlooringMasteryView view;
    private FlooringMasteryService service;
    
    public FlooringMasteryController(FlooringMasteryView view, FlooringMasteryService service) {
        this.view = view;
        this.service = service;
    }
    
    public void run() {
        boolean keepGoing = true;
        int menuSelection = 0;
        while (keepGoing) {
            menuSelection = view.printMenuSelection();
            //menuSelection = 1;
            switch (menuSelection) {
                    case 1:
                        listOrders();
                        break;
                    case 2:
                        createOrder();
                        break;
                    case 3:
                        editOrder();
                        break;
                    case 4:
                        removeOrder();
                        break;
                    case 5:
                        exportAllData();
                        break;
                    case 6:
                        keepGoing = false;
                        break;
                    default:
                        unknownCommand();
            }
        }
        exitMessage();
    }
    
    private void exitMessage() {
    	view.displayExitHeader();
	}

	private void unknownCommand() {
	    view.displayUnknownCommandHeader();
	}

	private void exportAllData() { 
	    view.displayExportAllDataHeader();
	    try {
	        service.exportAllData();
	    } catch (IOException e) {
	        view.displayErrorMessage(e.getMessage());
	    } 
	}

	private void removeOrder() {
		view.displayRemoveOrderHeader();
	    boolean hasErrors = false;
	    do {
	        //Prompt user for the date & order number
	        LocalDate orderDateInput = view.getOrderDateRemoveOrder();
	        int orderNumberInput = view.getOrderNumberRemoveOrder();
	        try {
	            String orderFileName = service.createOrderFileNameFromDate(orderDateInput);
	            service.checkOrderFileExists(orderFileName);

	            //If the file exists, check if the order with the specified order number exists in the file
	            int orderNumToRemove = service.checkOrderNumExists(orderFileName, orderNumberInput);
	            //If it doesn't exist, an exception is thrown. If it is, then remove the order.
	            Order orderToRemove = service.getOrder(orderFileName, orderNumToRemove);

	            //Display the order information
	            view.displayOrderInformation(orderDateInput, orderToRemove);

	            //Prompt the user if they are sure they want to remove the order
	            String removeConfirmation = view.getRemoveConfirmation();

	            //If they are sure, remove the order, if not return to menu. removedOrder will be null if no order to be removed.
	            Order removedOrder = service.removeOrderIfConfirmed(removeConfirmation, orderFileName, orderNumberInput);

	            view.displayRemoveSuccessBanner(removedOrder);
	            hasErrors = false;
	        } catch (FlooringMasteryOrderException | IOException e) {
	            hasErrors = true;
	            view.displayErrorMessage(e.getMessage());
	        }
	    } while (hasErrors);
	}

	private void editOrder() {
		view.displayEditOrderHeader();
	    boolean hasErrors = false;
	    do {
	        LocalDate orderDateInput = view.getOrderDateEditOrder();
	        int orderNumberInput = view.getOrderNumberEditOrder();
	        try {
	            String orderFileName = service.createOrderFileNameFromDate(orderDateInput);
	            service.checkOrderFileExists(orderFileName);

	            int orderNumToEdit = service.checkOrderNumExists(orderFileName, orderNumberInput);
	            Order orderToEdit = service.getOrder(orderFileName, orderNumToEdit);
	            
	            String updatedCustomerName = view.displayAndGetEditCustomerName(orderToEdit);
	            updatedCustomerName = service.checkEdit(updatedCustomerName);
	            
	            Order updatedOrder = service.updateOrderCustomerName(updatedCustomerName, orderToEdit);
	            
	            String updatedStateAbbreviation = view.displayAndGetEditState(orderToEdit);
	            updatedStateAbbreviation = service.checkEdit(updatedStateAbbreviation);
	            if (updatedStateAbbreviation!=null){
	                service.checkStateAgainstTaxFile(updatedStateAbbreviation);
	            }
	            updatedOrder = service.updateOrderState(updatedStateAbbreviation, orderToEdit);
	            
	            String updatedProductType = view.displayAndGetEditProductType(orderToEdit);
	            updatedProductType = service.checkEdit(updatedProductType);
	            if (updatedProductType!=null) {
	                service.checkProductTypeAgainstProductsFile(updatedProductType);
	            }
	            updatedOrder = service.updateOrderProductType(updatedProductType, orderToEdit);
	            
	            String updatedAreaString = view.displayAndGetEditArea(orderToEdit);
	            BigDecimal updatedArea = service.checkEditBigDecimal(updatedAreaString);
	            if (updatedArea!=null) {
	                service.checkAreaOverMinOrder(updatedArea);
	            }
	            updatedOrder = service.updateOrderArea(updatedArea, orderToEdit);
	            
	            updatedOrder = service.updateOrderCalculations(updatedOrder);
	            
	            view.displayEditedOrderSummary(orderDateInput,orderToEdit);
	            
	            String toBeEdited = view.getSaveConfirmation();
	            
	            Order editedOrder = service.editOrder(toBeEdited,orderFileName,updatedOrder);
	            
	            view.displayEditSucessHeader(editedOrder);
	            
	            hasErrors = false;
	               } catch (FlooringMasteryOrderException | IOException e) {
	                   hasErrors = true;
	                   view.displayErrorMessage(e.getMessage());
	               }
	           } while(hasErrors);
		
	}

	private void createOrder() {
	    view.displayCreateOrderHeader();
	    boolean hasErrors = false;
		do {
	        try {
	            LocalDate orderDateInput = view.getOrderDateCreateOrder();
	            service.checkDateIsInFuture(orderDateInput);

	            String customerNameInput = view.getCustomerName();
	            service.validateCustomerName(customerNameInput);
	            String customerNameInputPh = service.getCustomerNamePlaceHolder(customerNameInput);
	            
	            String stateAbbreviationInput = view.getStateAbbreviation();
	            service.checkStateAgainstTaxFile(stateAbbreviationInput);
	            
	            Collection<Product> availableProducts = service.getAllProducts();
	            String productTypeInput = view.displayAvailableProductsAndGetSelection(availableProducts);
	            service.checkProductTypeAgainstProductsFile(productTypeInput);
	            Product productSelected = service.getProduct(productTypeInput);
	            
	            BigDecimal areaInput = view.getArea();
	            service.checkAreaOverMinOrder(areaInput);
	            
	            BigDecimal materialCost = service.calculateMaterialCost(areaInput, productSelected.getCostPerSquareFoot());
	            
	            BigDecimal laborCost = service.calculateLaborCost(areaInput, productSelected.getLaborCostPerSquareFoot());
	            
	            Tax taxObj = service.getTax(stateAbbreviationInput);
	            BigDecimal tax = service.calculateTax(materialCost, laborCost, taxObj.getTaxRate());
	            
	            BigDecimal total = service.calculateTotal(materialCost, laborCost, tax);
	            
	            int orderNumber = service.generateNewOrderNum();
	            
	            String verifyOrder = view.displayNewOrderSummary(orderDateInput, orderNumber, customerNameInput, stateAbbreviationInput,
	                    productTypeInput, areaInput, materialCost, laborCost, tax, total);
	               
	            service.createNewOrder(verifyOrder, orderDateInput, orderNumber, customerNameInputPh, stateAbbreviationInput, 
	                    tax, productTypeInput, areaInput, materialCost, laborCost, materialCost, laborCost, tax, total);

	            view.displayCreateSuccessHeader(verifyOrder);
	            
	            hasErrors = false;
	        } catch (FlooringMasteryOrderException | IOException e) {
		        hasErrors = true;
	            view.displayErrorMessage(e.getMessage());
	        }

	    } while(hasErrors);
	}

	private void listOrders() {
        view.displayListOrdersHeader();
        boolean hasErrors = false;
        Collection<Order> orderList = null;
        do {
            try {
                LocalDate wantedOrderDate = view.getOrderDateListOrders();
                orderList = service.getOrderList(wantedOrderDate);
                view.displayOrderListHeader(wantedOrderDate);
                hasErrors = false;
            } catch (DateTimeException | FileNotFoundException | FlooringMasteryOrderException e) {
                hasErrors = true;
                view.displayErrorMessage(e.getMessage());
            }
        } while(hasErrors);
        view.displayOrderList(orderList);
    }
}
