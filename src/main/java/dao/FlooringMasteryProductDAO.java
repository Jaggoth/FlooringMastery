package dao;

import java.io.FileNotFoundException;
import java.util.Collection;

import dto.Product;

public interface FlooringMasteryProductDAO {
	
    Collection<Product> getAllProducts() throws FileNotFoundException;
    
    Product getProduct (String productType) throws FileNotFoundException;
}
