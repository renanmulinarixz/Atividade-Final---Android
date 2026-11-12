package br.com.pokespawn;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokemonApi {
    public interface Result<T> { void done(T value, Exception error); }
    private final ExecutorService workers = Executors.newFixedThreadPool(2);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final File cache;
    private final Context context;

    public PokemonApi(Context context) {
        this.context = context.getApplicationContext();
        cache = new File(context.getFilesDir(), "pokeapi");
        cache.mkdirs();
    }
    public void pokemon(int id, Result<Pokemon> result) {
        workers.execute(() -> {
            try {
                Pokemon pokemon = new Pokemon(read("pokemon-" + id, "https://pokeapi.co/api/v2/pokemon/" + id + "/"));
                // Start the encounter only after its image is available locally.
                File sprites = new File(context.getCacheDir(), "sprites"); sprites.mkdirs();
                File image = new File(sprites, Integer.toHexString(pokemon.sprite.hashCode()) + ".png");
                if (!image.exists() || image.length() == 0) {
                    byte[] bitmap = download(pokemon.sprite);
                    if (android.graphics.BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length) == null)
                        throw new Exception("Imagem do Pokémon indisponível.");
                    File temporary = File.createTempFile("sprite", ".tmp", sprites);
                    try (FileOutputStream output = new FileOutputStream(temporary)) { output.write(bitmap); }
                    if (!temporary.renameTo(image)) temporary.delete();
                }
                main.post(() -> result.done(pokemon, null));
            } catch (Exception e) { main.post(() -> result.done(null, e)); }
        });
    }
    public void catalog(Result<String[]> result) {
        workers.execute(() -> {
            try {
                String raw;
                try { raw = read("catalog", "https://pokeapi.co/api/v2/pokemon?limit=1025&offset=0"); }
                catch (Exception error) {
                    try (InputStream input = context.getAssets().open("catalog.json")) {
                        raw = new String(bytes(input), StandardCharsets.UTF_8);
                    }
                }
                JSONArray data = new JSONObject(raw).getJSONArray("results");
                String[] names = new String[Regions.TOTAL + 1];
                for (int i = 0; i < Math.min(data.length(), Regions.TOTAL); i++) names[i + 1] = data.getJSONObject(i).getString("name");
                main.post(() -> result.done(names, null));
            } catch (Exception e) { main.post(() -> result.done(null, e)); }
        });
    }
    private String read(String key, String url) throws Exception {
        File local = new File(cache, key + ".json");
        if (local.exists()) {
            try (InputStream input = new FileInputStream(local)) {
                String value = new String(bytes(input), StandardCharsets.UTF_8);
                new JSONObject(value);
                return value;
            } catch (Exception ignored) { local.delete(); }
        }
        String value = new String(download(url), StandardCharsets.UTF_8);
        new JSONObject(value);
        File pending = new File(cache, key + ".tmp");
        try (FileOutputStream output = new FileOutputStream(pending)) { output.write(value.getBytes(StandardCharsets.UTF_8)); }
        pending.renameTo(local);
        return value;
    }
    static byte[] download(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(12_000);
        connection.setReadTimeout(12_000);
        connection.setRequestProperty("User-Agent", "PokeSpawn-Educational/1.0");
        try {
            if (connection.getResponseCode() != 200) throw new Exception("Resposta HTTP " + connection.getResponseCode());
            try (InputStream input = connection.getInputStream()) { return bytes(input); }
        } finally { connection.disconnect(); }
    }
    static byte[] bytes(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }
    public void close() { workers.shutdownNow(); }
}
