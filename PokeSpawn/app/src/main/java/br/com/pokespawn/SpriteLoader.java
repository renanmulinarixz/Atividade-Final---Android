package br.com.pokespawn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Requests are shared by URL; recycled grid cells never receive an old sprite.
public final class SpriteLoader {
    private final ExecutorService workers = Executors.newFixedThreadPool(3);
    private final Handler main = new Handler(Looper.getMainLooper());
    private final LruCache<String, Bitmap> memory = new LruCache<String, Bitmap>(8 * 1024 * 1024) {
        @Override protected int sizeOf(String key, Bitmap value) { return value.getByteCount(); }
    };
    private final Map<String, List<WeakReference<ImageView>>> waiting = new HashMap<>();
    private final File directory;
    private final Context context;

    public SpriteLoader(Context context) {
        this.context = context.getApplicationContext();
        directory = new File(context.getCacheDir(), "sprites");
        directory.mkdirs();
    }
    public void show(ImageView view, String url, boolean captured) {
        view.setTag(url);
        if (captured) view.clearColorFilter();
        else {
            ColorMatrix gray = new ColorMatrix(); gray.setSaturation(0);
            view.setColorFilter(new ColorMatrixColorFilter(gray));
        }
        view.setAlpha(captured ? 1f : .38f);
        Bitmap cached = memory.get(url);
        if (cached != null) { set(view, cached); return; }
        view.setImageResource(R.drawable.ic_pokeball);
        if (waiting.containsKey(url)) { waiting.get(url).add(new WeakReference<>(view)); return; }
        List<WeakReference<ImageView>> targets = new ArrayList<>();
        targets.add(new WeakReference<>(view)); waiting.put(url, targets);
        workers.execute(() -> {
            Bitmap result = null;
            try {
                String key = Integer.toHexString(url.hashCode()) + ".png";
                File local = new File(directory, key);
                result = BitmapFactory.decodeFile(local.getAbsolutePath());
                if (result == null) {
                    byte[] data = PokemonApi.download(url);
                    result = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (result != null) try (FileOutputStream output = new FileOutputStream(local)) { output.write(data); }
                }
            } catch (Exception ignored) { /* The placeholder remains visible; a later visit retries. */ }
            Bitmap bitmap = result;
            main.post(() -> {
                if (bitmap != null) memory.put(url, bitmap);
                List<WeakReference<ImageView>> views = waiting.remove(url);
                if (views != null && bitmap != null) for (WeakReference<ImageView> reference : views) {
                    ImageView target = reference.get();
                    if (target != null && url.equals(target.getTag())) set(target, bitmap);
                }
            });
        });
    }
    private void set(ImageView view, Bitmap bitmap) {
        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        drawable.setFilterBitmap(false); drawable.setAntiAlias(false);
        view.setImageDrawable(drawable);
    }
    public void close() { workers.shutdownNow(); waiting.clear(); memory.evictAll(); }
}
