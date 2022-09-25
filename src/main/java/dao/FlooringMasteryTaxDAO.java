package dao;

import java.io.FileNotFoundException;
import java.util.Collection;

import dto.Tax;

public interface FlooringMasteryTaxDAO {
    
	Collection<Tax> getAllTaxes() throws FileNotFoundException;
    Tax getTax(String stateAbbreviation) throws FileNotFoundException;
}
