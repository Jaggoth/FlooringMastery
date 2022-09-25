package dao;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import dto.Order;

public interface FlooringMasteryOrderDAO {
	Order addOrderToExistingFile(String orderFile, int orderNumber, Order order) throws IOException;
	Order addOrderToNewFile(String orderFile, int orderNumber, Order order) throws IOException;
	Order getOrder (String orderFile, int orderNum) throws FileNotFoundException;
	Collection<Order> getAllOrdersForADate(String orderFile) throws FileNotFoundException;
	Collection<Integer> getAllOrderNumsForADate(String orderFile) throws FileNotFoundException;
	Collection<Order> getAllOrders() throws FileNotFoundException;
	Collection<Integer> getAllOrderNums() throws FileNotFoundException;
	String[] listAllOrderFiles();
	Order editOrder(String orderFile, Order updatedOrder) throws IOException;
	Order deleteOrder(String orderFile, int orderNumber) throws IOException;
	void deleteOrderFile(String fileName) throws IOException;
	void writeDataExport() throws IOException;
}
