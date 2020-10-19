package co.naes.aurora.transport;

import org.dmfs.httpessentials.exceptions.ProtocolException;
import org.dmfs.jems.optional.Optional;
import org.dmfs.oauth2.client.OAuth2AccessToken;
import org.dmfs.oauth2.client.OAuth2Scope;
import org.dmfs.rfc5545.DateTime;

public class OAuthToken implements OAuth2AccessToken  {

    private final CharSequence at;

    private final CharSequence rt;

    private final DateTime ed;

    private final OAuth2Scope s;

    /* default */ OAuthToken(String accessToken, String refreshToken, long exp, OAuth2Scope scope) {

        at = accessToken;
        rt = refreshToken;
        ed = new DateTime(exp);
        s = scope;
    }

    @Override
    public CharSequence accessToken() throws ProtocolException {

        return at;
    }

    @Override
    public CharSequence tokenType() throws ProtocolException {
        return null;
    }

    @Override
    public boolean hasRefreshToken() {

        return rt != null;
    }

    @Override
    public CharSequence refreshToken() throws ProtocolException {

        return rt;
    }

    @Override
    public DateTime expirationDate() throws ProtocolException {

        return ed;
    }

    @Override
    public OAuth2Scope scope() throws ProtocolException {

        return s;
    }

    @Override
    public Optional<CharSequence> extraParameter(String s) {

        return null;
    }
}
