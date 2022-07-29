package de.ronnywalter.eve.service;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.ronnywalter.eve.exception.EveCharacterNotFoundException;
import de.ronnywalter.eve.model.EveCharacter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final CharacterService characterService;
    private final OAuth20Service oAuth20Service;

    public String getApiToken(int characterId) throws EveCharacterNotFoundException {
        EveCharacter character = characterService.getEveCharacter(characterId);
        if(character == null) {
            throw new EveCharacterNotFoundException("Character " + characterId + " not found.");
        }
        return refreshAndSaveToken(character);
    }

    private String refreshAndSaveToken(EveCharacter character) {

        if(character != null) {
            LocalDateTime expiryDate = character.getExpiryDate();
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(expiryDate)) {
                try {
                    OAuth2AccessToken accessToken = oAuth20Service.refreshAccessToken(character.getRefreshToken());
                    DecodedJWT decodedJWT = JWT.decode(accessToken.getAccessToken());
                    JwkProvider provider = new UrlJwkProvider(URI.create("https://login.eveonline.com/oauth/jwks").toURL());
                    Jwk jwk = provider.get(decodedJWT.getKeyId());
                    Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                    algorithm.verify(decodedJWT);

                    character.setApiToken(accessToken.getAccessToken());
                    character.setRefreshToken(accessToken.getRefreshToken());
                    character.setExpiryDate(LocalDateTime.now().plusSeconds(accessToken.getExpiresIn()));
                    characterService.saveCharacter(character);
                } catch (IOException e) {
                    log.error("Error refreshing Token: " + e.getMessage(), e);
                } catch (InterruptedException e) {
                    log.error("Error refreshing Token: " + e.getMessage(), e);
                } catch (ExecutionException e) {
                    log.error("Error refreshing Token: " + e.getMessage(), e);
                } catch (InvalidPublicKeyException e) {
                    log.error("Error refreshing Token: " + e.getMessage(), e);
                } catch (JwkException e) {
                    log.error("Error refreshing Token: " + e.getMessage(), e);
                }
            }

            return character.getApiToken();
        } else {
            return null;
        }
    }



/*    private String refreshAndSaveToken(EveCharacter character) {
        if(character != null) {
            LocalDateTime expiryDate = character.getExpiryDate();
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(expiryDate)) {
                log.debug("get new AccessToken for char " + character.getId());
                try {
                    StringBuilder builder = new StringBuilder();
                    builder.append("grant_type=");
                    builder.append(URLEncoder.encode("refresh_token", "UTF-8"));
                    builder.append("&client_id=");
                    builder.append(URLEncoder.encode(character.getClientId(), "UTF-8"));
                    builder.append("&refresh_token=");
                    builder.append(URLEncoder.encode(character.getRefreshToken(), "UTF-8"));


                    URL obj = new URL("https://login.eveonline.com/oauth/token");
                    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                    // add request header
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    con.setRequestProperty("Host", "login.eveonline.com");
                    con.setConnectTimeout(10000);
                    con.setReadTimeout(10000);

                    // Send post request
                    con.setDoOutput(true);
                    try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                        wr.writeBytes(builder.toString());
                        wr.flush();
                    }

                    StringBuilder response;
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        String inputLine;
                        response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                    }
                    log.debug("Response: " + response.toString());
                    // read json
                    JsonParser jsonParser = JsonParserFactory.getJsonParser();
                    Map<String, Object> result = jsonParser.parseMap(response.toString());

                    result.keySet().forEach(k -> {
                        log.debug(k + ": " + result.get(k));
                    });

                    character.setApiToken(result.get("access_token").toString());
                    character.setRefreshToken(result.get("refresh_token").toString());

                    Integer seconds = Integer.parseInt(result.get("expires_in").toString());
                    LocalDateTime newExpiryTime = LocalDateTime.now().plusSeconds(seconds);
                    character.setExpiryDate(newExpiryTime);

                    characterService.saveCharacter(character);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return character.getApiToken();
        }
        return null;
    }

 */
}
