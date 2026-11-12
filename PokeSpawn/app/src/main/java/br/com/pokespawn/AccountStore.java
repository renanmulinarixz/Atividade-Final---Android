package br.com.pokespawn;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Local accounts for this installation. No remote service or email verification.
public final class AccountStore {
    private final SharedPreferences prefs;
    public AccountStore(Context context) { prefs = context.getSharedPreferences("accounts", Context.MODE_PRIVATE); }

    public String authenticate(String username, char[] password, boolean create) throws Exception {
        String key = Normalizer.normalize(username.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        if (key.length() < 3 || key.length() > 80) throw new Exception("Use um nome ou e-mail de 3 a 80 caracteres.");
        if (password.length < 6) throw new Exception("Use uma senha com pelo menos 6 caracteres.");
        String entry = prefs.getString("account:" + key, null);
        if (create) {
            if (entry != null) throw new Exception("Esse treinador já existe. Use Entrar.");
            byte[] salt = new byte[24];
            new SecureRandom().nextBytes(salt);
            JSONObject account = new JSONObject();
            account.put("name", username.trim());
            account.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP));
            account.put("hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP));
            String id = hex(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)));
            account.put("id", id);
            if (!prefs.edit().putString("account:" + key, account.toString()).commit()) throw new Exception("Não foi possível salvar a conta.");
            return id;
        }
        if (entry == null) throw new Exception("Treinador não encontrado. Toque em Criar conta.");
        JSONObject account = new JSONObject(entry);
        byte[] salt = Base64.decode(account.getString("salt"), Base64.NO_WRAP);
        byte[] expected = Base64.decode(account.getString("hash"), Base64.NO_WRAP);
        if (!MessageDigest.isEqual(expected, hash(password, salt))) throw new Exception("Senha incorreta.");
        return account.getString("id");
    }

    private static byte[] hash(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, 120_000, 256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded(); }
        finally { spec.clearPassword(); }
    }
    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 255));
        return out.toString();
    }
}
