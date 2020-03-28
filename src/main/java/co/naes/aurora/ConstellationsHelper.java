package co.naes.aurora;

import java.io.CharArrayWriter;
import java.util.Random;

public final class ConstellationsHelper {

    public static final String[] LIST = {
            "Andromeda",
            "Antlia",
            "Apus",
            "Aquarius",
            "Aquila",
            "Ara",
            "Aries",
            "Auriga",
            "Bootes",
            "Caelum",
            "Camelopardus",
            "Cancer",
            "Canes-Venatici",
            "Canis-Major",
            "Canis-Minor",
            "Capricornus",
            "Carina",
            "Cassiopeia",
            "Centaurus",
            "Cephus",
            "Cetus",
            "Chamaeleon",
            "Circinus",
            "Columba",
            "Coma-Berenices",
            "Corona-Australis",
            "Corona-Borealis",
            "Corvus",
            "Crater",
            "Crux",
            "Cygnus",
            "Delphinus",
            "Dorado",
            "Draco",
            "Equuleus",
            "Eridanus",
            "Fornax",
            "Gemini",
            "Grus",
            "Hercules",
            "Horologium",
            "Hydra",
            "Hydrus",
            "Indus",
            "Lacerta",
            "Leo",
            "Leo-Minor",
            "Lepus",
            "Libra",
            "Lupus",
            "Lynx",
            "Lyra",
            "Mensa",
            "Microscopium",
            "Monoceros",
            "Musca",
            "Norma",
            "Octans",
            "Ophiuchus",
            "Orion",
            "Pavo",
            "Pegasus",
            "Perseus",
            "Phoenix",
            "Pictor",
            "Pisces",
            "Piscis-Austrinis",
            "Puppis",
            "Pyxis",
            "Reticulum",
            "Sagitta",
            "Sagittarius",
            "Scorpius",
            "Sculptor",
            "Scutum",
            "Serpens",
            "Sextans",
            "Taurus",
            "Telescopium",
            "Triangulum",
            "Triangulum-Australe",
            "Tucana",
            "Ursa-Major",
            "Ursa-Minor",
            "Vela",
            "Virgo",
            "Volans",
            "Vulpecula"
    };

    public static final String SEPARATOR = "#";

    private ConstellationsHelper() { }

    public static char[] getRandom(int num) {

        Random r = new Random();
        CharArrayWriter cw = new CharArrayWriter();

        for (int i = 0; i < num - 1; i++) {

            cw.append(LIST[r.nextInt(LIST.length)]).append(SEPARATOR);
        }
        cw.append(LIST[r.nextInt(LIST.length)]);

        return cw.toCharArray();
    }
}
