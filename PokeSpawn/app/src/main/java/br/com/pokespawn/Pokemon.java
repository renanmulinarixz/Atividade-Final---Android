package br.com.pokespawn;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Locale;

public final class Pokemon {
    public final int id;
    public final String name, sprite, types;
    public final int height, weight;
    public final int[] stats;
    public final String json;

    public Pokemon(String source) throws JSONException {
        JSONObject data = new JSONObject(source);
        id = data.getInt("id");
        name = data.getString("name");
        String image = data.getJSONObject("sprites").optString("front_default", "");
        sprite = image.startsWith("https://") ? image : spriteUrl(id);
        JSONArray entries = data.getJSONArray("types");
        StringBuilder labels = new StringBuilder();
        for (int i = 0; i < entries.length(); i++) {
            if (i > 0) labels.append(" / ");
            labels.append(typeName(entries.getJSONObject(i).getJSONObject("type").getString("name")));
        }
        types = labels.toString();
        height = data.getInt("height");
        weight = data.getInt("weight");
        stats = new int[6];
        JSONArray values = data.getJSONArray("stats");
        for (int i = 0; i < Math.min(6, values.length()); i++) stats[i] = values.getJSONObject(i).getInt("base_stat");
        json = source;
    }

    public String title() { return displayName(name); }
    public String number() { return String.format(Locale.ROOT, "#%04d", id); }
    public static String spriteUrl(int id) {
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
    }
    public static String displayName(String raw) {
        if (raw == null || raw.isEmpty()) return "???";
        return raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1).replace('-', ' ');
    }
    private static String typeName(String type) {
        String[] en = {"normal","fire","water","electric","grass","ice","fighting","poison","ground","flying","psychic","bug","rock","ghost","dragon","dark","steel","fairy"};
        String[] pt = {"Normal","Fogo","Água","Elétrico","Planta","Gelo","Lutador","Veneno","Terra","Voador","Psíquico","Inseto","Pedra","Fantasma","Dragão","Sombrio","Aço","Fada"};
        for (int i = 0; i < en.length; i++) if (en[i].equals(type)) return pt[i];
        return type;
    }
}
