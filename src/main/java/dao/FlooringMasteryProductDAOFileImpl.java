package dao;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.springframework.stereotype.Component;

import dto.Product;

@Component
public class FlooringMasteryProductDAOFileImpl implements FlooringMasteryProductDAO {
	
    private Map<String, Product> products = new HashMap<>();
    private final String delimiter = ",";
    private final String productsFile;
    //Full path to Orders D:\\Users\\Tudor\\eclipse-workspace\\FlooringMastery\\FileData\\Data\\Products
    
    public FlooringMasteryProductDAOFileImpl() {
        this.productsFile = "FileData\\Data\\Products";
    }
    
    public FlooringMasteryProductDAOFileImpl(String filePath) {
        this.productsFile = filePath;
    }  
    
	@Override
	public Product getProduct(String productType) throws FileNotFoundException {
        loadProducts();
        return products.get(productType);
	}
    
	@Override
	public Collection<Product> getAllProducts() throws FileNotFoundException {
        loadProducts();
        return new ArrayList<Product>(products.values());
	}

	private void loadProducts() throws FileNotFoundException {
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(productsFile)));

        String currentLine;
        Product currentProduct;
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();

            if (currentLine.startsWith("ProductType")) {//ignore heading
                continue;
            }
            currentProduct = unmarshallProduct(currentLine);
            products.put(currentProduct.getProductType(), currentProduct);
        }
        scanner.close();
	}

	private Product unmarshallProduct(String currentLine) {
        String [] productTokens = currentLine.split(delimiter);
        
        Product productFromFile = new Product();
        productFromFile.setProductType(productTokens[0]);
        productFromFile.setCostPerSquareFoot(new BigDecimal(productTokens[1]));
        productFromFile.setLaborCostPerSquareFoot(new BigDecimal(productTokens[2]));

        return productFromFile;
	}
	
	

}
