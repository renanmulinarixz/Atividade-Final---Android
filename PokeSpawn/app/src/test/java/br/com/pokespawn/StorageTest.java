package br.com.pokespawn;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class StorageTest {
    private Context context;
    public static String fixture(int id) {
        return "{\"id\":" + id + ",\"name\":\"pikachu\",\"height\":4,\"weight\":60,"
            + "\"sprites\":{\"front_default\":\"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png\"},"
            + "\"types\":[{\"type\":{\"name\":\"electric\"}}],\"stats\":[{\"base_stat\":35}]}";
    }
    @Before public void prepare() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("accounts", Context.MODE_PRIVATE).edit().clear().commit();
        for (String id : new String[]{"one", "two"}) context.getSharedPreferences("trainer_" + id, Context.MODE_PRIVATE).edit().clear().commit();
    }
    @Test public void capturesPersistAndDoNotLeakAcrossTrainers() throws Exception {
        GameStore first = new GameStore(context, "one", "Renan");
        assertTrue(first.add(new Pokemon(fixture(25)), 1000));
        assertTrue(first.add(new Pokemon(fixture(25)), 2000));
        GameStore reopened = new GameStore(context, "one", "Renan");
        assertEquals(1, reopened.unique(0)); assertEquals(2, reopened.total());
        assertEquals(1000, reopened.entry(25).getLong("first")); assertEquals(2000, reopened.entry(25).getLong("last"));
        assertEquals("Elétrico", reopened.pokemon(25).types);
        GameStore second = new GameStore(context, "two", "Outro");
        assertEquals(0, second.total()); assertFalse(second.has(25));
    }
    @Test public void accountsValidatePasswordsAndCaseInsensitiveNames() throws Exception {
        AccountStore accounts = new AccountStore(context);
        String id = accounts.authenticate("Renan", "senha123".toCharArray(), true);
        assertEquals(id, accounts.authenticate("RENAN", "senha123".toCharArray(), false));
        try { accounts.authenticate("Renan", "errada123".toCharArray(), false); fail("Accepted wrong password"); }
        catch (Exception expected) { assertEquals("Senha incorreta.", expected.getMessage()); }
        try { accounts.authenticate("renan", "senha123".toCharArray(), true); fail("Created duplicate account"); }
        catch (Exception expected) { assertTrue(expected.getMessage().contains("já existe")); }
        String raw = context.getSharedPreferences("accounts", Context.MODE_PRIVATE).getString("account:renan", "");
        assertFalse(raw.contains("senha123")); assertTrue(raw.contains("salt"));
    }
    @Test public void statsCountSpeciesByTheirNationalRegion() throws Exception {
        GameStore store = new GameStore(context, "one", "Renan");
        store.add(new Pokemon(fixture(25)), 1); store.add(new Pokemon(fixture(252)), 2);
        store.add(new Pokemon(fixture(252)), 3);
        assertEquals(1, store.unique(1)); assertEquals(1, store.unique(3));
        assertEquals(2, store.unique(0)); assertEquals(3, store.total());
        assertEquals(Integer.valueOf(252), store.recent().get(0));
    }
}
