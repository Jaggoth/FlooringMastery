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

import dto.Tax;

@Component
public class FlooringMasteryTaxDAOFileImpl implements FlooringMasteryTaxDAO {

    private Map<String, Tax> taxes = new HashMap<>();
    private final String delimiter = ",";
    private final String taxesFile;
    //Full path to Orders D:\\Users\\Tudor\\eclipse-workspace\\FlooringMastery\\FileData\\Data\\Taxes
    
	public FlooringMasteryTaxDAOFileImpl() {
		this.taxesFile = "FileData\\Data\\Taxes";
	}
    
	public FlooringMasteryTaxDAOFileImpl(String taxesFile) {
		this.taxesFile = taxesFile;
	}
    
	@Override
	public Collection<Tax> getAllTaxes() throws FileNotFoundException {
        loadTaxes();
        return new ArrayList<Tax>(taxes.values());
	}

	@Override
	public Tax getTax(String stateAbbreviation) throws FileNotFoundException {
	    loadTaxes();
        return taxes.get(stateAbbreviation);
	}
	
	private void loadTaxes() throws FileNotFoundException {
		Scanner scanner = new Scanner(new BufferedReader(new FileReader(taxesFile)));

        String currentLine;
        Tax currentTax;
        while (scanner.hasNextLine()) {
            currentLine = scanner.nextLine();

            if (currentLine.startsWith("State")) {//ignore heading
                continue;
            }
            currentTax = unmarshallTax(currentLine);
            taxes.put(currentTax.getStateAbbr(), currentTax);
        }
        scanner.close();
	}

	private Tax unmarshallTax(String currentLine) {
        String[] taxTokens = currentLine.split(delimiter);
        
        Tax taxFromFile = new Tax();
        taxFromFile.setStateAbbr(taxTokens[0]);
        taxFromFile.setStateName(taxTokens[1]);
        taxFromFile.setTaxRate(new BigDecimal(taxTokens[2]));
        
        return taxFromFile;
	}
}
