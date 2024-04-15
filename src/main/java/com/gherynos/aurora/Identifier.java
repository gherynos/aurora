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

package com.gherynos.aurora;

import java.util.Objects;

public class Identifier {

    private final String name;

    private final String email;

    public Identifier(String name, String email) {

        this.name = name.replaceAll("\\|", "");
        this.email = email.replaceAll("\\|", "");
    }

    public Identifier(String serialised) {

        String[] sp = serialised.split("\\|");
        name = sp[0];
        email = sp[1];
    }

    public String serialise() {

        return String.format("%s|%s", name, email);
    }

    public String getName() {

        return name;
    }

    public String getEmail() {

        return email;
    }

    @Override
    public String toString() {

        return String.format("%s <%s>", name, email);
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {

            return true;
        }

        if (o == null || getClass() != o.getClass()) {

            return false;
        }

        Identifier that = (Identifier) o;
        return name.equals(that.name) && email.equals(that.email);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, email);
    }
}
