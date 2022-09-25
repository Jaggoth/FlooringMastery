package dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.springframework.stereotype.Component;

import dto.Order;

@Component
public class FlooringMasteryOrderDAOFileImpl implements FlooringMasteryOrderDAO{
	
	private Map<Integer, Order> orders = new HashMap<Integer, Order>();
	private final String delimiter = ",";
	private final String dataExportFile;
	private final String customerNamePlaceHolder = "~#~";
	private final String orderFolder;
	//Full path to Orders D:\\Users\\Tudor\\eclipse-workspace\\FlooringMastery\\FileData\\Orders
	
	public FlooringMasteryOrderDAOFileImpl() {
		this.dataExportFile = "FileData\\Backup\\DataExport.txt";
		this.orderFolder = "FileData\\Orders";
	}

	public FlooringMasteryOrderDAOFileImpl(String dataExportFile, String orderFolder) {
		this.dataExportFile = dataExportFile;
		this.orderFolder = orderFolder;
	}
	    
    public Order addOrderToExistingFile(String fileName, int orderNumber, Order order) throws IOException {
        loadOrders(fileName);
        Order newOrder = orders.put(orderNumber, order);
        writeOrders(fileName);
        orders.clear();
        return newOrder;
    }
    
    public Order addOrderToNewFile(String fileName, int orderNumber, Order order) throws IOException {
        Order newOrder = orders.put(orderNumber, order);
        
        writeOrders(fileName);
        orders.clear();
        return newOrder;
    }
    
    public Order getOrder (String fileName, int orderNum) throws FileNotFoundException {
        loadOrders(fileName);
        Order orderToGet = orders.get(orderNum);
        orders.clear();
        return orderToGet;
    }
	
    public Collection<Order> getAllOrdersForADate(String fileName) throws FileNotFoundException {
        loadOrders(fileName);
        Collection<Order> allOrdersForADate = new ArrayList<Order>(orders.values());
        
        orders.clear();
        return allOrdersForADate;
    }
    
    public Collection<Integer> getAllOrderNumsForADate(String fileName) throws FileNotFoundException {
        loadOrders(fileName);
        Collection<Integer> orderNums = new ArrayList<Integer>(orders.keySet());
        
        //Collection<Integer> listOfOrderNums = new ArrayList<Integer>(orderNums);
        orders.clear();
        return orderNums;
    }
    
    public Collection<Order> getAllOrders() throws FileNotFoundException { 
        String [] allOrderFiles = listAllOrderFiles();
        Collection<Order> allOrders = new ArrayList<Order>();
        
        for (String orderFile : allOrderFiles) {
        	Collection<Order> ordersForADate = getAllOrdersForADate(orderFile);
            ordersForADate.forEach(order -> {
                allOrders.add(order);
            });
        }
        orders.clear();
        return allOrders;
    }
    
    public Collection<Integer> getAllOrderNums() throws FileNotFoundException {
        String [] allOrderFiles = listAllOrderFiles();
        Collection<Integer> allOrderNums = new ArrayList<Integer>();
        
        for (String orderFile : allOrderFiles) {
        	Collection<Integer> orderNumsForADate = getAllOrderNumsForADate(orderFile);
        	orderNumsForADate.forEach(orderNum -> {
                allOrderNums.add(orderNum);
            });  
        }

        orders.clear();
        return allOrderNums;
        
    }
    
	public String[] listAllOrderFiles() {
        FilenameFilter filter = (file, fileName) -> {
        	return fileName.contains(".");
        };
		
        String [] orderFiles = new File(orderFolder).list(filter);
        return orderFiles;
	}
        
    public Order editOrder(String fileName, Order updatedOrder) throws IOException {
		loadOrders(fileName);
		
		Order orderToEdit = orders.get(updatedOrder.getOrderNumber());
		
		orderToEdit.setCustomerName(updatedOrder.getCustomerName());
		orderToEdit.setStateAbbr(updatedOrder.getStateAbbr());
		orderToEdit.setTaxRate(updatedOrder.getTaxRate());
		orderToEdit.setProductType(updatedOrder.getProductType());
		orderToEdit.setArea(updatedOrder.getArea());
		orderToEdit.setCostPerSquareFoot(updatedOrder.getCostPerSquareFoot());
		orderToEdit.setLaborCostPerSquareFoot(updatedOrder.getLaborCostPerSquareFoot());
		orderToEdit.setMaterialCost(updatedOrder.getMaterialCost());
		orderToEdit.setLaborCost(updatedOrder.getLaborCost());
		orderToEdit.setTax(updatedOrder.getTax());
		orderToEdit.setTotal(updatedOrder.getTotal());
		
		writeOrders(fileName);
		return orderToEdit;
    }
    
    public Order deleteOrder(String fileName, int orderNumber) throws IOException {
        loadOrders(fileName);
        Order orderToRemove = orders.remove(orderNumber);
        writeOrders(fileName);
        return orderToRemove;
    }
	
    public void deleteOrderFile(String fileName) throws IOException {
        Path pathOfFile = Paths.get(this.orderFolder + "\\" + fileName);
        Files.deleteIfExists(pathOfFile);
    }  

	private void loadOrders(String fileName) throws FileNotFoundException{
        Scanner scanner = new Scanner(new BufferedReader(new FileReader(this.orderFolder+"\\"+fileName)));
        
        String currentLine;
        Order currentOrder;       
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();

            if (currentLine.startsWith("OrderNumber")) {//ignore heading
                continue;
            }
            currentOrder = unmarshallOrder(currentLine);
            orders.put(currentOrder.getOrderNumber(), currentOrder);
        }
        scanner.close();
	}
	
	private void writeOrders(String fileName) throws IOException {
	        PrintWriter out = new PrintWriter(new FileWriter(this.orderFolder+"\\"+fileName));
	        
	        //Header
	        out.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
	       
	        String orderAsText;
	        Collection<Order> orderList = orders.values();
	        for (Order currentOrder : orderList) {
	            orderAsText = marshallOrder(currentOrder);
	            out.println(orderAsText);
	            out.flush();
	        }
	        //Clean up
	        out.close();
	    }
	
	private String marshallOrder(Order order) {
		String orderAsString = order.getOrderNumber() + delimiter;
		orderAsString += order.getCustomerName() + delimiter;
		orderAsString += order.getStateAbbr() + delimiter;
		orderAsString += order.getTaxRate() + delimiter;
		orderAsString += order.getProductType() + delimiter;
		orderAsString += order.getArea() + delimiter;
		orderAsString += order.getCostPerSquareFoot() + delimiter;
		orderAsString += order.getLaborCostPerSquareFoot() + delimiter;
		orderAsString += order.getMaterialCost() + delimiter;
		orderAsString += order.getLaborCost() + delimiter;
		orderAsString += order.getTax() + delimiter;
		orderAsString += order.getTotal() + delimiter;
		
		return orderAsString;
	}
	
	private Order unmarshallOrder(String currentLine) {
        String [] orderParts = currentLine.split(delimiter);
    
        Order fileOrder = new Order();
        
        String customerName = orderParts[1];
        customerName = customerName.replace(customerNamePlaceHolder, ",");
        
        fileOrder.setOrderNumber(Integer.parseInt(orderParts[0]));
        fileOrder.setCustomerName(customerName);
        fileOrder.setStateAbbr(orderParts[2]);
        fileOrder.setTaxRate(new BigDecimal(orderParts[3]));
        fileOrder.setProductType(orderParts[4]);
        fileOrder.setArea(new BigDecimal (orderParts[5]));
        fileOrder.setCostPerSquareFoot(new BigDecimal(orderParts[6]));
        fileOrder.setLaborCostPerSquareFoot(new BigDecimal(orderParts[7]));
        fileOrder.setMaterialCost(new BigDecimal(orderParts[8]));
        fileOrder.setLaborCost(new BigDecimal(orderParts[9]));
        fileOrder.setTax(new BigDecimal(orderParts[10]));
        fileOrder.setTotal(new BigDecimal(orderParts[11]));

        return fileOrder;
	}
	
	//Extension
    private String getDateFromFileName(String fileName) {   
    	//File name pattern "Order_MMDDYY.txt"
        String [] fileNameTokens = fileName.split("_");

        String [] dateTokens = fileNameTokens[1].split("\\.");

        String date = dateTokens[0];

        String mm = date.substring(0, 2);
        String dd = date.substring(2, 4);
        String yyyy = date.substring(4, 8);
        //req format is MM-DD-YYYY
        return mm+"-"+dd+"-"+yyyy;
    }
    
    private Map<String,Collection<Order>> getExportData() throws FileNotFoundException {
        Map<String,Collection<Order>> exportDataMap = new HashMap<>();
        //Get a list of all the orders from all files
        String [] allOrderFiles = this.listAllOrderFiles();
        
        for (String orderFile: allOrderFiles) {
            //Get the order date from the file name
            String dateString = this.getDateFromFileName(orderFile);
            //Get the order list from the file
            Collection<Order> allOrdersForFile = this.getAllOrdersForADate(orderFile);
            //Add to the map the date and orders
            exportDataMap.put(dateString,allOrdersForFile);
        }
        return exportDataMap;
    }
    
    public void writeDataExport() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(dataExportFile));
        out.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total,Date");

        Map<String, Collection<Order>> dataToExport = getExportData();
        String dataToExportAsText;
        
        for (String date : dataToExport.keySet()) {
        	Collection<Order> currentOrderList = dataToExport.get(date);
            for (Order order : currentOrderList) {
            	dataToExportAsText = marshallOrder(order) + "," + date;
                out.println(dataToExportAsText);
                out.flush();
            }
        }
        out.close();
    }
}
