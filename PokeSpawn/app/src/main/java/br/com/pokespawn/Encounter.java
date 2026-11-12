package br.com.pokespawn;

import java.util.Random;

// Only the currently visible encounter can be captured, once, before its deadline.
public final class Encounter {
    public static final long INTERVAL_MS = 15_000;
    private int pokemonId;
    private long deadline;
    private boolean captured;

    public void show(int id, long now) {
        pokemonId = id;
        deadline = now + INTERVAL_MS;
        captured = false;
    }

    public boolean capture(long now) {
        if (!canCapture(now)) return false;
        captured = true;
        return true;
    }

    public boolean canCapture(long now) { return pokemonId > 0 && !captured && now < deadline; }
    public boolean expired(long now) { return pokemonId == 0 || now >= deadline; }
    public int seconds(long now) { return (int) Math.max(0, (deadline - now + 999) / 1000); }
    public int id() { return pokemonId; }
    public long deadline() { return deadline; }
    public boolean captured() { return captured; }
    public void restore(int id, long due, boolean wasCaptured) {
        pokemonId = id; deadline = due; captured = wasCaptured;
    }

    public static int nextId(Random random, int region, int previous) {
        int start = Regions.first(region);
        int end = Regions.last(region);
        // Draw without retry loops and without repeating the previous Pokémon.
        if (previous >= start && previous <= end) {
            int next = start + random.nextInt(end - start);
            return next >= previous ? next + 1 : next;
        }
        return start + random.nextInt(end - start + 1);
    }
}
