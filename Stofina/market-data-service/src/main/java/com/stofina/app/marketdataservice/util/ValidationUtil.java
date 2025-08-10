package com.stofina.app.marketdataservice.util;
import com.stofina. app.marketdataservice.constant.Constants;

import java.math.BigDecimal;
import java.util.List;

public class ValidationUtil {

    //Sembol boş, null ya da maksimum uzunluktan büyükse geçersizdir.
    public static boolean isValidSymbol(String symbol) {
        return symbol != null
                && !symbol.trim().isEmpty()
                && symbol.trim().length() <= Constants.Validation.MAX_SYMBOL_LENGTH;
    }

    //Fiyat negatif, null ya da çok yüksekse geçersizdir.
    public static boolean isValidPrice(BigDecimal price) {
        return price != null
                && price.compareTo(Constants.Validation.MIN_PRICE) >= 0
                && price.compareTo(Constants.Validation.MAX_PRICE) <= 0;
    }

    //Sembol listesi boşsa ya da fazla sayıda sembol varsa hata fırlatır.
    public static void validateSymbolList(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException(Constants.ErrorMessages.SYMBOLS_LIST_EMPTY);
        }

        if (symbols.size() > Constants.Validation.MAX_SYMBOLS_PER_REQUEST) {
            throw new IllegalArgumentException(Constants.ErrorMessages.MAX_SYMBOLS_EXCEEDED);
        }
    }
}
