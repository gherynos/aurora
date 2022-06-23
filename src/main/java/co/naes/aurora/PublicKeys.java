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

package co.naes.aurora;

public class PublicKeys {

    private final byte[] publicKey;

    private byte[] publicSignKey;

    private final Identifier identifier;

    public PublicKeys(byte[] publicKey, Identifier identifier) {

        this.publicKey = publicKey.clone();
        this.identifier = identifier;
    }

    public PublicKeys(byte[] publicKey, byte[] publicSignKey, Identifier identifier) {

        this.publicKey = publicKey.clone();
        this.publicSignKey = publicSignKey.clone();
        this.identifier = identifier;
    }

    public byte[] getPublicKey() {

        return publicKey.clone();
    }

    public byte[] getPublicSignKey() {

        return publicSignKey.clone();
    }

    public Identifier getIdentifier() {

        return identifier;
    }

    @Override
    public String toString() {

        return identifier.toString();
    }
}
