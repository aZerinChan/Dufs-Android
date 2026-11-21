package local.dufs.manage;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONObject;

final class Profile {
    private static final String ALIAS = "dufs-profile-v1";
    final URI root;
    final String user, password;
    final DufsClient client;
    Profile(URI root, String user, String password) {
        this.root = root; this.user = user; this.password = password;
        this.client = new DufsClient(user, password);
    }
    private static SharedPreferences prefs(Context ctx) { return ctx.getSharedPreferences("dufs", Context.MODE_PRIVATE); }
    private static SecretKey key(boolean create) throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore"); store.load(null);
        SecretKey k = (SecretKey) store.getKey(ALIAS, null);
        if (k != null || !create) return k;
        KeyGenerator gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        gen.init(new KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());
        return gen.generateKey();
    }
    static Profile load(Context ctx) throws Exception {
        String blob = prefs(ctx).getString("encrypted", null);
        if (blob == null) return null;
        String[] pieces = blob.split(":", 2);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key(false), new GCMParameterSpec(128, Base64.decode(pieces[0], Base64.NO_WRAP)));
        JSONObject j = new JSONObject(new String(c.doFinal(Base64.decode(pieces[1], Base64.NO_WRAP)), StandardCharsets.UTF_8));
        return new Profile(DufsClient.address(j.getString("url")), j.getString("user"), j.getString("password"));
    }
    void save(Context ctx) throws Exception {
        JSONObject j = new JSONObject().put("url", root.toASCIIString()).put("user", user).put("password", password);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key(true));
        String blob = Base64.encodeToString(c.getIV(), Base64.NO_WRAP) + ":" + Base64.encodeToString(c.doFinal(j.toString().getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
        if (!prefs(ctx).edit().putString("encrypted", blob).commit()) throw new java.io.IOException("无法保存连接设置");
    }
}
