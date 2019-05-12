package com.github.z201.utils;

import lombok.Data;

/**
 * @author z201.coding@gmail.com
 **/
@Data
public class KeyUtils {

    public static String hideKey(String oldKey) {
        int length = oldKey.length();
        int beforeLength = 10;
        int afterLength = 10;
        String replaceSymbol = "*******************";
        StringBuffer sb = new StringBuffer();
        sb.append(oldKey.substring(0, beforeLength));
        sb.append(replaceSymbol);
        sb.append(oldKey.substring(length - afterLength, length));
        return sb.toString();
    }
}
