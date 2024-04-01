/*
 * Copyright (C) 2020-2024  Luca Zanconato (<github.com/gherynos>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.nharyes.aurora.transport;

import fi.iki.elonen.NanoHTTPD;
import net.nharyes.aurora.AuroraException;
import net.nharyes.aurora.db.DBUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.Properties;

public class GmailOAuthUtils extends NanoHTTPD {

    protected static final Logger LOGGER = LogManager.getLogger();

    private static final int REDIRECT_PORT = 8080;
    private static final String REDIRECT_URI = "http://localhost:" + REDIRECT_PORT;

    private static final BasicScope SCOPE = new BasicScope("https://mail.google.com");

    private final HttpUrlConnectionExecutor executor;

    private OAuth2InteractiveGrant grant;

    private String state;

    private final DBUtils db;

    private final Properties main;

    private GmailOAuthUtilsUI ui;

    public interface GmailOAuthUtilsUI {

        void openInBrowser(URI uri);

        void authCompleted();

        void authError(Exception ex);
    }

    public GmailOAuthUtils(DBUtils db) {

        super(REDIRECT_PORT);

        this.db = db;
        main = db.getProperties();

        executor = new HttpUrlConnectionExecutor();
    }

    private OAuth2Client getClient() {

        OAuth2ClientCredentials credentials = new BasicOAuth2ClientCredentials(
                main.getProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID),
                main.getProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET));

        return new BasicOAuth2Client(new GoogleAuthorizationProvider(), credentials,
                new LazyUri(new Precoded(REDIRECT_URI)));
    }

    public void authorise(GmailOAuthUtilsUI ui, String clientId, String clientSecret) throws IOException {

        main.setProperty(DBUtils.OAUTH_GMAIL_CLIENT_ID, clientId);
        main.setProperty(DBUtils.OAUTH_GMAIL_CLIENT_SECRET, clientSecret);

        grant = new AuthorizationCodeGrant(getClient(), SCOPE);

        URI authorizationUrl = grant.authorizationUrl();
        state = new TextParameter(org.dmfs.oauth2.client.utils.Parameters.STATE, new XwfueParameterList(
                new LazyUri(new Precoded(authorizationUrl.toString())).query().value())).toString();

        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

        this.ui = ui;
        ui.openInBrowser(authorizationUrl);
    }

    @Override
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition")
    public Response serve(IHTTPSession session) {

        if ("/".equals(session.getUri())) {

            try {

                String url = String.format("%s?state=%s&code=%s", REDIRECT_URI, state, session.getParameters().get("code").get(0));

                OAuth2AccessToken token = grant.withRedirect(
                        new LazyUri(new Precoded(url))).accessToken(executor);

                main.setProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN, token.accessToken().toString());
                main.setProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN, token.refreshToken().toString());
                main.setProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION,
                        Long.toString(token.expirationDate().getTimestamp()));

                db.saveProperties();
                ui.authCompleted();
                stopServer();
                return newFixedLengthResponse("Authorisation successful, this window can be closed now.\n");

            } catch (ProtocolException | ProtocolError | IOException | AuroraException ex) {

                ui.authError(ex);
                stopServer();
                return newFixedLengthResponse("Error during authorisation.\n");
            }
        }

        return newFixedLengthResponse("");
    }

    private void stopServer() {

        new Thread(() -> {

            synchronized (this) {

                try {

                    this.wait(2000);

                } catch (InterruptedException ex) {  // NOPMD

                }

                stop();
                ui = null;  // NOPMD
            }

        }).start();
    }

    public String getAccessToken() throws AuroraException {

        try {

            long exp = Long.parseLong(main.getProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION));
            if (exp <= System.currentTimeMillis()) {

                if (LOGGER.isDebugEnabled()) {

                    LOGGER.debug("Refreshing Gmail access token");
                }

                OAuth2AccessToken token = new OAuthToken(
                        main.getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN),
                        main.getProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN),
                        exp, SCOPE);

                OAuth2AccessToken newToken = new TokenRefreshGrant(getClient(), token).accessToken(executor);

                // save refreshed tokens to DB
                main.setProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN, newToken.accessToken().toString());
                main.setProperty(DBUtils.OAUTH_GMAIL_TOKEN_EXPIRATION,
                        Long.toString(newToken.expirationDate().getTimestamp()));
                if (newToken.hasRefreshToken()) {

                    main.setProperty(DBUtils.OAUTH_GMAIL_REFRESH_TOKEN, newToken.refreshToken().toString());
                }

                db.saveProperties();
            }

            return main.getProperty(DBUtils.OAUTH_GMAIL_ACCESS_TOKEN);

        } catch (ProtocolException | ProtocolError | IOException ex) {

            throw new AuroraException("Unable to get/refresh Gmail access token", ex);
        }
    }
}
