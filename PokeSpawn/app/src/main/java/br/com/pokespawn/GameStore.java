package br.com.pokespawn;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameStore {
    private final SharedPreferences prefs;
    public final String accountId;

    public GameStore(Context context, String accountId, String name) {
        this.accountId = accountId;
        prefs = context.getSharedPreferences("trainer_" + accountId, Context.MODE_PRIVATE);
        if (!prefs.contains("created")) prefs.edit().putLong("created", System.currentTimeMillis()).apply();
        prefs.edit().putString("name", name).apply();
    }
    public String name() { return prefs.getString("name", "Treinador"); }
    public boolean has(int id) { return prefs.contains("pokemon:" + id); }
    public int region() { return prefs.getInt("region", 0); }
    public void region(int region) { prefs.edit().putInt("region", region).apply(); }
    public long created() { return prefs.getLong("created", 0); }

    // The capture's metadata and counts are stored together in one atomic preference edit.
    public boolean add(Pokemon pokemon, long now) {
        try {
            JSONObject item = entry(pokemon.id);
            if (item == null) item = new JSONObject().put("first", now).put("count", 0);
            item.put("last", now).put("count", item.getInt("count") + 1).put("data", pokemon.json);
            return prefs.edit().putString("pokemon:" + pokemon.id, item.toString()).commit();
        } catch (JSONException e) { return false; }
    }
    public JSONObject entry(int id) {
        try {
            String raw = prefs.getString("pokemon:" + id, null);
            return raw == null ? null : new JSONObject(raw);
        } catch (JSONException e) { return null; }
    }
    public Pokemon pokemon(int id) {
        try { JSONObject entry = entry(id); return entry == null ? null : new Pokemon(entry.getString("data")); }
        catch (JSONException e) { return null; }
    }
    public int unique(int region) {
        int count = 0;
        for (int i = Regions.first(region); i <= Regions.last(region); i++) if (has(i)) count++;
        return count;
    }
    public int total() {
        int count = 0;
        for (String key : prefs.getAll().keySet()) if (key.startsWith("pokemon:")) {
            try { count += new JSONObject(prefs.getString(key, "{}")).optInt("count"); }
            catch (JSONException ignored) { /* Other captures remain available. */ }
        }
        return count;
    }
    public List<Integer> recent() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 1; i <= Regions.TOTAL; i++) if (has(i)) ids.add(i);
        Collections.sort(ids, (a, b) -> Long.compare(entry(b).optLong("last"), entry(a).optLong("last")));
        return ids.subList(0, Math.min(6, ids.size()));
    }
}
