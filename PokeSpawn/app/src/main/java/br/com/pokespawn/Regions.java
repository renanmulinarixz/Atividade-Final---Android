package br.com.pokespawn;

public final class Regions {
    public static final int TOTAL = 1025;
    public static final String[] NAMES = {
        "Todas as regiões", "Kanto", "Johto", "Hoenn", "Sinnoh", "Unova",
        "Kalos", "Alola", "Galar / Hisui", "Paldea"
    };
    private static final int[] ENDS = {1025, 151, 251, 386, 493, 649, 721, 809, 905, 1025};

    private Regions() {}

    public static int first(int region) { return region <= 1 ? 1 : ENDS[region - 1] + 1; }
    public static int last(int region) { return ENDS[region]; }
    public static int size(int region) { return last(region) - first(region) + 1; }
    public static int of(int id) {
        for (int i = 1; i < ENDS.length; i++) if (id <= ENDS[i]) return i;
        return 9;
    }
}
