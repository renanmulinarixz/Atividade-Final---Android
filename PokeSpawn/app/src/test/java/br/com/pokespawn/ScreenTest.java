package br.com.pokespawn;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.android.controller.ActivityController;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, qualifiers = "w390dp-h844dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenTest {
    public static class TestActivity extends MainActivity {
        @Override protected PokemonApi createPokemonApi() {
            return new PokemonApi(this) {
                @Override public void pokemon(int id, Result<Pokemon> result) {
                    try { result.done(new Pokemon(StorageTest.fixture(25)), null); }
                    catch (Exception e) { throw new RuntimeException(e); }
                }
                @Override public void catalog(Result<String[]> result) {
                    String[] catalog = new String[1026]; catalog[25] = "pikachu"; result.done(catalog, null);
                }
            };
        }
    }
    @Test public void loginHasWorkingEntryAndRegistrationControls() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("session", Context.MODE_PRIVATE).edit().clear().commit();
        try (ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).setup()) {
            TestActivity activity = controller.get();
            assertNotNull(activity.findViewById(R.id.login)); assertNotNull(activity.findViewById(R.id.register));
            snapshot(activity, "01-login");
        }
    }
    @Test public void captureUpdatesDexAndProfileAndSurvivesRecreation() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("session", Context.MODE_PRIVATE).edit().putString("id", "screen-test").putString("name", "Renan").commit();
        context.getSharedPreferences("trainer_screen-test", Context.MODE_PRIVATE).edit().clear().commit();
        File spriteDirectory = new File(context.getCacheDir(), "sprites"); spriteDirectory.mkdirs();
        File spriteFile = new File(spriteDirectory, Integer.toHexString(Pokemon.spriteUrl(25).hashCode()) + ".png");
        try (java.io.InputStream input = getClass().getResourceAsStream("/pikachu.png");
             FileOutputStream output = new FileOutputStream(spriteFile)) {
            output.write(PokemonApi.bytes(input));
        }
        try (ActivityController<TestActivity> controller = Robolectric.buildActivity(TestActivity.class).setup()) {
            TestActivity activity = controller.get();
            Button capture = activity.findViewById(R.id.capture); assertTrue(capture.isEnabled());
            ImageView image = firstImage(activity.getWindow().getDecorView());
            for (int attempt = 0; attempt < 100 && !(image.getDrawable() instanceof BitmapDrawable); attempt++) {
                Thread.sleep(10); shadowOf(Looper.getMainLooper()).idle();
            }
            assertTrue("Captured Pokémon must display the cached sprite", image.getDrawable() instanceof BitmapDrawable);
            snapshot(activity, "02-captura");
            capture.performClick();
            GameStore persisted = new GameStore(context, "screen-test", "Renan");
            assertTrue(persisted.has(25)); assertEquals(1, persisted.unique(0)); assertEquals(1, persisted.total());
            assertFalse(activity.findViewById(R.id.capture).isEnabled());
            controller.recreate(); activity = controller.get();
            assertFalse(activity.findViewById(R.id.capture).isEnabled());
            activity.findViewById(R.id.capture).performClick(); assertEquals(1, persisted.total());
            setScreen(activity, 1); snapshot(activity, "03-pokedex");
            setScreen(activity, 2); snapshot(activity, "04-perfil");
            setScreen(activity, 0);
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(15));
            assertTrue(activity.findViewById(R.id.capture).isEnabled());
            activity.findViewById(R.id.capture).performClick(); assertEquals(2, persisted.total());
        }
    }
    private ImageView firstImage(View view) {
        if (view instanceof ImageView) return (ImageView) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                ImageView found = firstImage(group.getChildAt(i)); if (found != null) return found;
            }
        }
        return null;
    }
    private void setScreen(MainActivity activity, int screen) throws Exception {
        Field field = MainActivity.class.getDeclaredField("screen"); field.setAccessible(true); field.setInt(activity, screen);
        Method render = MainActivity.class.getDeclaredMethod("render"); render.setAccessible(true); render.invoke(activity);
    }
    private void snapshot(MainActivity activity, String name) throws Exception {
        View view = activity.getWindow().getDecorView();
        view.measure(View.MeasureSpec.makeMeasureSpec(390, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(844, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, 390, 844);
        Bitmap bitmap = Bitmap.createBitmap(390, 844, Bitmap.Config.ARGB_8888); view.draw(new Canvas(bitmap));
        File directory = new File("build/reports/screenshots"); directory.mkdirs();
        try (FileOutputStream out = new FileOutputStream(new File(directory, name + ".png"))) { bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); }
    }
}
