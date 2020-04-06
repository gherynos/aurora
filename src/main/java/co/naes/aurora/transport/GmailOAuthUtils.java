package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.db.DBUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.GmailScopes;

import java.io.IOException;
import java.util.Collections;

public class GmailOAuthUtils {

    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;

    private GoogleAuthorizationCodeFlow flow;

    public GmailOAuthUtils() {

        httpTransport = new NetHttpTransport();
        jsonFactory = new JacksonFactory();
    }

    public String getAuthorisationUrl(String clientId, String clientSecret) {

        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory,
                clientId, clientSecret,
                Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM)
        ).build();

        DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID, clientId);
        DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET, clientSecret);

        return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
    }

    public void authorise(String code) throws AuroraException {

        if (flow == null) {

            throw new AuroraException("Please get the authorisation URL first.");
        }

        try {

            GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
            Credential credential = flow.createAndStoreCredential(response, null);

            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN, credential.getAccessToken());
            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN, credential.getRefreshToken());
            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION,
                    credential.getExpirationTimeMilliseconds().toString());

        } catch (IOException ex) {

            throw new AuroraException("Unable to authorise access to Gmail", ex);
        }
    }

    public String getAccessToken() throws AuroraException {

        try {

            GoogleCredential credential = new GoogleCredential.Builder()
                    .setClientSecrets(
                            DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID),
                            DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET)
                    )
                    .setJsonFactory(jsonFactory).setTransport(httpTransport).build()
                    .setRefreshToken(DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN))
                    .setAccessToken(DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN))
                    .setExpirationTimeMilliseconds(
                            Long.parseLong(DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION)));

            if (credential.getExpirationTimeMilliseconds() <= System.currentTimeMillis()) {

                credential.refreshToken();
            }

            return credential.getAccessToken();

        } catch (IOException ex) {

            throw new AuroraException("Unable to get/refresh Gmail access token", ex);
        }
    }
}
