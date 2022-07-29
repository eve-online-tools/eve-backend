package de.ronnywalter.eve.jobs.esi;

import de.ronnywalter.eve.exception.ApiException;
import de.ronnywalter.eve.jobs.ThreadWorker;
import de.ronnywalter.eve.model.JobData;
import lombok.extern.slf4j.Slf4j;
import net.evetech.esi.client.api.CharacterApi;
import net.evetech.esi.client.api.CorporationApi;
import net.evetech.esi.client.model.GetCharactersCharacterIdOk;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
public abstract class EsiApiJob extends BaseEsiApiJob {

    private final CharacterApi characterApi = new CharacterApi(getApiClient());
    private final CorporationApi corporationApi = new CorporationApi(getApiClient());

    public EsiApiJob(String name) {
        super(name);
    }

    protected <T> T update(Update<T> update) throws HttpClientErrorException {
        return update(update, 0, true);
    }

    protected <T> T update(Update<T> update, boolean updateNextExecutionTime) throws HttpClientErrorException {
        return update(update, 0, updateNextExecutionTime);
    }

    private <T> T update(Update<T> update, int retry, boolean updateNextExecutionTime) throws HttpClientErrorException {
        checkErrors();
        try {
            T result = update.update();
            if(result instanceof ResponseEntity) {
                //jobData.setNextExecutionTime(getExpiryDate(((ResponseEntity<?>) result).getHeaders()).plusSeconds(60));
                handleHeaders((ResponseEntity<?>) result);
            }
            return result;
        } catch (HttpClientErrorException ex) {
            handleHeaders(ex);
            log.error("Exception occured: " + ex.getMessage());

            if(ex.getRawStatusCode() == 403) {
                log.warn("Got a forbidden from api: " + ex.getMessage());
                return (T) new ResponseEntity<T>(null, ex.getResponseHeaders(), ex.getStatusCode());
            } else if(ex.getRawStatusCode() == 404) {
                return null;
            }

            if (retry < MAX_RETRIES) {
                retry++;
                return update(update, retry, updateNextExecutionTime);
            } else {
                throw ex;
            }
        } catch (HttpServerErrorException ex) {
            handleHeaders(ex);
            if(ex.getRawStatusCode() == 504 && ex.getMessage().toLowerCase().contains("timeout contacting tranquility")) {
                // Server downtime, we will sleep an try it again.
                log.warn("Server downtime? " + ex.getMessage());
                try {
                    log.info("Sleeping for 60 seconds");
                    Thread.sleep(60000); //Wait a minute
                } catch (InterruptedException ex1) {
                    //No problem
                }
                log.info("Retrying " + retry + " of " + MAX_RETRIES);
                return update(update, retry, updateNextExecutionTime);
            } else {
                log.warn("Exception occured: " + ex.getMessage());
                if (retry < MAX_RETRIES) {
                    retry++;
                    log.info("Retrying " + retry + " of " + MAX_RETRIES);
                    return update(update, retry, updateNextExecutionTime);
                } else {
                    throw ex;
                }
            }
        } catch (RestClientException ex) {
            log.warn(ex.getMessage());
            if (retry < MAX_RETRIES) {
                retry++;
                return update(update, retry, updateNextExecutionTime);
            } else {
                throw ex;
            }
        }
    }

    protected interface Update<T> {
        public T update() throws HttpClientErrorException;
    }

    protected <K> List<K> updatePages(JobData jobData, int maxRetries, EsiPagesHandler<K> handler) throws ApiException {
        List<K> values = new ArrayList<>();
        EsiPageUpdater<K> pageUpdater = new EsiPageUpdater<>(handler, 1, " 1 of ?");
        List<K> returnValue = updateApi(pageUpdater);
        if (returnValue != null) {
            values.addAll(returnValue);

            Integer pages = getXPages(pageUpdater.getResponse().getHeaders()); //Get pages header
            Instant expires = getExpiryDate(pageUpdater.getResponse().getHeaders());
            jobData.setNextExecutionTime(expires.plusSeconds(60));
            int count = 2;
            if (pages != null && pages > 1) { //More than one page
                List<EsiPageUpdater<K>> updaters = new ArrayList<>();
                for (int i = 2; i <= pages; i++) { //Get the remaining pages (we already got page 1 so we start at page 2
                    updaters.add(new EsiPageUpdater<>(handler, i, count + " of " + pages));
                    count++;
                }
                log.debug("Starting " + updaters.size() + " pages threads");
                try {
                    List<Future<List<K>>> futures = startSubThreads(updaters);
                    for (Future<List<K>> future : futures) {
                        if (future.isDone()) {
                            returnValue = future.get(); //Get data from ESI
                            if (returnValue != null) {
                                values.addAll(returnValue);
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    ThreadWorker.throwExecutionException(HttpStatusCodeException.class, ex);
                }
            }
        }
        return values;
    }

    public interface EsiPagesHandler<K> {
        public ResponseEntity<List<K>> get(Integer page) throws HttpStatusCodeException;
    }

    public class EsiPageUpdater<T> implements Callable<List<T>>, Updater<ResponseEntity<List<T>>, HttpStatusCodeException> {

        private final EsiPagesHandler<T> handler;
        private final int page;
        private final String status;
        private final int maxRetries;
        private ResponseEntity<List<T>> response;

        public EsiPageUpdater(EsiPagesHandler<T> handler, int page, String status) {
            this.handler = handler;
            this.page = page;
            this.status = status;
            this.maxRetries = MAX_RETRIES;
        }

        @Override
        public ResponseEntity<List<T>> update() throws HttpStatusCodeException {
            response = handler.get(page);
            return response;
        }

        @Override
        public List<T> call() throws Exception {
            return updateApi(this, maxRetries);
        }

        public ResponseEntity<List<T>> getResponse() {
            return response;
        }

        @Override
        public String getStatus() {
            return status;
        }

        @Override
        public int getMaxRetries() {
            return maxRetries;
        }
    }

    protected interface Updater<R, E extends Throwable> {
        public R update() throws E;
        public String getStatus();
        public int getMaxRetries();
    }

    private <R> R updateApi(Updater<ResponseEntity<R>, HttpStatusCodeException> updater) throws ApiException {
        return updateApi(updater, 0);
    }

    private <R> R updateApi(Updater<ResponseEntity<R>, HttpStatusCodeException> updater, int retries) throws ApiException {
        checkErrors(); //Update timeframe as needed
        //checkCancelled();
        try {
            ResponseEntity<R> apiResponse = updater.update();
            if (apiResponse == null) {
                return null;
            }
            handleHeaders(apiResponse);
            log.debug(updater.getStatus() + " updated");
            return apiResponse.getBody();
        } catch (HttpClientErrorException ex) {
            handleHeaders(ex);
            log.warn("Error occured. Status-code: " + ex.getRawStatusCode() + ", message: " + ex.getMessage());
            if (ex.getRawStatusCode() == 401 && ex.getMessage().toLowerCase().contains("error") && ex.getMessage().toLowerCase().contains("authorization not provided")) {
                throw new ApiException("not authorized", ex, ex.getRawStatusCode(), ex.getResponseHeaders());
            } else if (ex.getRawStatusCode() == 403) {
                log.warn("Access denied: " + ex.getMessage());
                return null;
            } else if ((ex.getRawStatusCode() >= 500 && ex.getRawStatusCode() < 600 //CCP error, Lets try again in a sec
                    || ex.getRawStatusCode() == 0) //Other error, Lets try again in a sec
                    && ex.getRawStatusCode() != 503 //Don't retry when it may be downtime
                    && (ex.getRawStatusCode() != 502 || (ex.getMessage().toLowerCase().contains("no reply within 10 seconds") || ex.getMessage().toLowerCase().startsWith("<html>"))) //Don't retry when it may be downtime, unless it's "no reply within 10 seconds" or html body
                    && retries < updater.getMaxRetries()) { //Retries
                retries++;
                try {
                    Thread.sleep(1000); //Wait a sec
                } catch (InterruptedException ex1) {
                    //No problem
                }
                log.info(updater.getStatus(), "Retrying " + retries + " of " + updater.getMaxRetries() + ":");
                return updateApi(updater, retries);
            } else {
                throw new ApiException(ex);
            }
        } catch (HttpServerErrorException ex) {
            handleHeaders(ex);
            if(ex.getRawStatusCode() == 504 && ex.getMessage().toLowerCase().contains("timeout contacting tranquility")) {
                // Server downtime, we will sleep an try it again.
                log.info("Server downtime? " + ex.getMessage());
                try {
                    log.info("Sleeping for 60 seconds");
                    Thread.sleep(60000); //Wait a minute
                } catch (InterruptedException ex1) {
                    //No problem
                }
                log.info(updater.getStatus(), "Retrying " + retries + " of " + updater.getMaxRetries() + ":");
                return updateApi(updater, retries);
            } else {
                log.warn(ex.getMessage());
                retries++;
                try {
                    Thread.sleep(1000); //Wait a sec
                } catch (InterruptedException ex1) {
                    //No problem
                }
                log.info(updater.getStatus(), "Retrying " + retries + " of " + updater.getMaxRetries() + ":");
                return updateApi(updater, retries);
            }
        } catch (RestClientException ex) {
            log.warn(ex.getMessage());
            retries++;
            try {
                Thread.sleep(1000); //Wait a sec
            } catch (InterruptedException ex1) {
                //No problem
            }
            log.info(updater.getStatus(), "Retrying " + retries + " of " + updater.getMaxRetries() + ":");
            return updateApi(updater, retries);
        }
    }

    protected final <K> List<Future<K>> startSubThreads(Collection<? extends Callable<K>> updaters) throws InterruptedException {
        return ThreadWorker.startReturn(updaters);
    }

    protected final <K> List<Future<K>> startSubThreads(Collection<? extends Callable<K>> updaters, boolean updateProgress) throws InterruptedException {
        return ThreadWorker.startReturn(updaters, updateProgress);
    }

    protected String resolveCharacterName(int characterId) {
        ResponseEntity<GetCharactersCharacterIdOk> response = update(new Update<ResponseEntity<GetCharactersCharacterIdOk>>() {
            @Override
            public ResponseEntity<GetCharactersCharacterIdOk> update() throws HttpClientErrorException {
                return characterApi.getCharactersCharacterIdWithHttpInfo(characterId, DATASOURCE, null);
            }
        });
        if(response != null && response.getBody() != null) {
            return response.getBody().getName();
        } else {
            return null;
        }
    }
}
