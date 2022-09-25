package view;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UserIO {

    void print(String msgToPrint);

    String readString(String msgToPrint);

    int readInt(String intToRead);

    BigDecimal readBigDecimal(String bigDecimalToRead);
    
    LocalDate readDate(String localDateToRead);
}
