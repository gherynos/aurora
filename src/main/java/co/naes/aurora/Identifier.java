package co.naes.aurora;

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
