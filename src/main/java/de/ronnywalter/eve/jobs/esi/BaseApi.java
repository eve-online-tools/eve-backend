package de.ronnywalter.eve.jobs.esi;

import lombok.Getter;
import net.evetech.esi.client.ApiClient;

import java.util.List;
import java.util.Map;

@Getter
public class BaseApi {

    protected static final String X_PAGES = "X-Pages";
    protected static final String DATASOURCE = "tranquility";
    protected static final String LANGUAGE = "en-us";
    protected static final int MAX_RETRIES = 3;

    private final ApiClient apiClient = new ApiClient();

    public static Integer getXPages(Map<String, List<String>> responseHeaders) {
        // Get header
        String header = getHeader(responseHeaders, X_PAGES);
        if (header == null) {
            return null;
        }
        // Convert
        Integer xPages;
        try {
            xPages = Integer.valueOf(header);
        } catch (NumberFormatException ex) {
            xPages = null;
        }
        return xPages;
    }

    public static String getHeader(Map<String, List<String>> responseHeaders, String header) {
        if (responseHeaders == null) {
            return null;
        }
        // Search the headers case insensitive (headers should be evaluated as
        // case insensitive, but, the Swagger implementation uses HashMap that
        // is case sensitive)
        for (Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(header)) {
                List<String> headersList = entry.getValue();
                if (headersList != null && !headersList.isEmpty()) { // Better
                    // safe
                    // than
                    // sorry
                    return headersList.get(0);
                }
                break; // Header found, but, was null or empty
            }
        }
        return null;
    }

}
