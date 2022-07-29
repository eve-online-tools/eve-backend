package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.jobs.AbstractJob;
import de.ronnywalter.eve.util.DateFormatThreadSafe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.ApiClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
public abstract class BaseEsiApiJob extends AbstractJob {

    protected static final String HEADER_X_PAGES = "X-Pages";
    protected static final String HEADER_EXPIRES = "Expires";
    protected static final String HEADER_LAST_MODIFIED = "Last-Modified";
    protected static final String HEADER_ERROR_LIMIT_REMAIN = "X-Esi-Error-Limit-Remain";
    protected static final String HEADER_ERROR_LIMIT_RESET = "X-Esi-Error-Limit-Reset";
    protected static final String HEADER_LAST_ETAG = "Etag";

    protected static final String DATASOURCE = "tranquility";
    protected static final String LANGUAGE = "en-us";
    protected static final int MAX_RETRIES = 150;

    private static final DateFormatThreadSafe EXPIRE_DATE1 = new DateFormatThreadSafe("EEE, dd MMM yyyy HH:mm:ss zzz"); //Tue, 04 Oct 2016 18:21:28 GMT
    private static final DateFormatThreadSafe EXPIRE_DATE2 = new DateFormatThreadSafe("dd MMM yyyy HH:mm:ss zzz");

    private final ApiClient apiClient = new ApiClient();

    private static Integer errorLimit = 100;
    private static Integer errorReset = 60;

    public BaseEsiApiJob(String name) {
        super(name);
    }

    @PostConstruct
    private void setUserAgent() {
        apiClient.setUserAgent("Eve Tool Suite; contact: 'kreetix' or mail@ronnywalter.de");
    }

    public static Integer getXPages(Map<String, List<String>> responseHeaders) {
        // Get header
        String header = getHeader(responseHeaders, HEADER_X_PAGES);
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

    public static Instant getExpiryDate(Map<String, List<String>> responseHeaders) {
        // Get header
        String header = getHeader(responseHeaders, HEADER_EXPIRES);
        if (header == null) {
            return null;
        }
        // Convert
        try {
            return EXPIRE_DATE1.parse(header).toInstant();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException e) {
            int a = 0;
        }
        try {
            return EXPIRE_DATE2.parse(header).toInstant();
        } catch (ParseException ex) {
            return Instant.now();
        }
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

    public static Integer getIntHeader(Map<String, List<String>> responseHeaders, String header) {
        String headerValue = getHeader(responseHeaders, header);
        if(headerValue != null && headerValue.length() > 0) {
            return Integer.parseInt(headerValue);
        } else {
            return null;
        }
    }

    protected synchronized static void checkErrors() {
        if (errorLimit != null && errorLimit < 10) { //Error limit reached
            try {
                long wait = errorReset * 1000 + 1000;
                log.warn("Error limit reached! waiting: " + wait + "ms");
                if (wait > 0) { //Negative values throws an Exception
                    Thread.sleep(wait); //Wait until the error window is reset
                }
                //Reset
                BaseEsiApiJob.errorReset = 60;
                BaseEsiApiJob.errorLimit = 100;  //No errors in this timeframe (yet)
            } catch (InterruptedException ex) {
                //No problem
            }
        }
    }

    protected void handleHeaders(HttpStatusCodeException httpStatusCodeException) {
        //setExpires(apiException.getResponseHeaders());
        setErrorLimit(httpStatusCodeException.getResponseHeaders()); //Always save error limit header
    }

    protected void handleHeaders(ResponseEntity<?> responseEntity) {
        //setExpires(apiResponse.getHeaders());
        setErrorLimit(responseEntity.getHeaders()); //Always save error limit header
    }

    private void setErrorLimit(Map<String, List<String>> responseHeaders) {
        if (responseHeaders != null) {
            setErrorLimit(getIntHeader(responseHeaders, "x-esi-error-limit-remain"));
            setErrorReset(getIntHeader(responseHeaders, "x-esi-error-limit-reset"));
        }
    }

    private synchronized static void setErrorLimit(Integer errorLimit) {
        BaseEsiApiJob.errorLimit = errorLimit;
        log.debug("error-limit: " + BaseEsiApiJob.errorLimit);
    }

    private synchronized static void setErrorReset(Integer errorReset) {
        BaseEsiApiJob.errorReset = errorReset;
        log.debug("error-reset: " + BaseEsiApiJob.errorReset);
    }
}
