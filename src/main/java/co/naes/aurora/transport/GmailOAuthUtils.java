package co.naes.aurora.transport;

import co.naes.aurora.AuroraException;
import co.naes.aurora.db.DBUtils;
import org.dmfs.httpessentials.exceptions.ProtocolError;
import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.httpessentials.httpurlconnection.HttpUrlConnectionExecutor;
import org.dmfs.oauth2.client.BasicOAuth2Client;
import org.dmfs.oauth2.client.BasicOAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Client;
import org.dmfs.oauth2.client.OAuth2ClientCredentials;
import org.dmfs.oauth2.client.OAuth2InteractiveGrant;
import org.dmfs.oauth2.client.grants.AuthorizationCodeGrant;
import org.dmfs.oauth2.client.grants.TokenRefreshGrant;
import org.dmfs.oauth2.client.scope.BasicScope;
import org.dmfs.oauth2.providers.GoogleAuthorizationProvider;
import org.dmfs.rfc3986.encoding.Precoded;
import org.dmfs.rfc3986.parameters.adapters.TextParameter;
import org.dmfs.rfc3986.parameters.adapters.XwfueParameterList;
import org.dmfs.rfc3986.uris.LazyUri;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import static org.dmfs.oauth2.client.utils.Parameters.STATE;

public class GmailOAuthUtils {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";

    private static final BasicScope SCOPE = new BasicScope("https://mail.google.com");

    private final HttpUrlConnectionExecutor executor;

    private OAuth2InteractiveGrant grant;

    private String state;

    public GmailOAuthUtils() {

        executor = new HttpUrlConnectionExecutor();
    }

    private OAuth2Client getClient() {

        OAuth2ClientCredentials credentials = new BasicOAuth2ClientCredentials(
                DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID),
                DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET));

        return new BasicOAuth2Client(new GoogleAuthorizationProvider(), credentials,
                new LazyUri(new Precoded(REDIRECT_URI)));
    }

    public String getAuthorisationUrl(String clientId, String clientSecret) {

        DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID, clientId);
        DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET, clientSecret);

        grant = new AuthorizationCodeGrant(getClient(), SCOPE);

        URI authorizationUrl = grant.authorizationUrl();
        state = new TextParameter(STATE, new XwfueParameterList(
                new LazyUri(new Precoded(authorizationUrl.toString())).query().value())).toString();

        return authorizationUrl.toString();
    }

    public void authorise(String code) throws AuroraException {

        if (grant == null) {

            throw new AuroraException("Please get the authorisation URL first.");
        }

        try {

            String url = String.format("http://localhost?state=%s&code=%s", state, code);

            OAuth2AccessToken token = grant.withRedirect(
                    new LazyUri(new Precoded(url))).accessToken(executor);

            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN, token.accessToken().toString());
            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN, token.refreshToken().toString());
            DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION,
                    Long.toString(token.expirationDate().getTimestamp()));

            DBUtils.saveProperties();

        } catch (ProtocolException | ProtocolError | IOException ex) {

            throw new AuroraException("Unable to authorise access to Gmail", ex);
        }
    }

    public String getAccessToken() throws AuroraException {

        try {

            long exp = Long.parseLong(DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION));
            if (exp <= System.currentTimeMillis()) {

                logger.finer("Refreshing Gmail access token");

                OAuth2AccessToken token = new OAuthToken(
                        DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN),
                        DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN),
                        exp, SCOPE);

                OAuth2AccessToken newToken = new TokenRefreshGrant(getClient(), token).accessToken(executor);

                // save refreshed tokens to DB
                DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN,
                        newToken.accessToken().toString());
                DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION,
                        Long.toString(newToken.expirationDate().getTimestamp()));
                if (newToken.hasRefreshToken()) {
                    DBUtils.getProperties().setProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN,
                            newToken.refreshToken().toString());
                }

                DBUtils.saveProperties();
            }

            return DBUtils.getProperties().getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN);

        } catch (ProtocolException | ProtocolError | IOException ex) {

            throw new AuroraException("Unable to get/refresh Gmail access token", ex);
        }
    }
}
