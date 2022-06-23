/*
 * Copyright (C) 2020  Luca Zanconato (<github.com/gherynos>)
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
