package com.github.z201.utils;

import lombok.Data;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

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

    public static String stripXSS(String value) {
        if (value != null) {
            // NOTE: It's highly recommended to use the ESAPI library and uncomment the following line to
            // avoid encoded attacks.
            // value = ESAPI.encoder().canonicalize(value);
            // Avoid null characters
            value = value.replaceAll("", "");
            // Avoid anything between script tags
            Pattern scriptPattern = compile("<script>(.*?)</script>", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid anything in a src="..." type of e­xpression
            scriptPattern = compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            scriptPattern = compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome </script> tag
            scriptPattern = compile("</script>", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Remove any lonesome <script ...> tag
            scriptPattern = compile("<script(.*?)>", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid eval(...) e­xpressions
            scriptPattern = compile("eval\\((.*?)\\)", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid e­xpression(...) e­xpressions
            scriptPattern = compile("e­xpression\\((.*?)\\)", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid javascript:... e­xpressions
            scriptPattern = compile("javascript:", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid vbscript:... e­xpressions
            scriptPattern = compile("vbscript:", CASE_INSENSITIVE);
            value = scriptPattern.matcher(value).replaceAll("");
            // Avoid onload= e­xpressions
            scriptPattern = compile("onload(.*?)=", CASE_INSENSITIVE | MULTILINE | DOTALL);
            value = scriptPattern.matcher(value).replaceAll("");
        }
        return value;

    }
}
