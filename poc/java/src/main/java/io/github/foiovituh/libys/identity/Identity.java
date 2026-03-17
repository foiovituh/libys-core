package io.github.foiovituh.libys.identity;

import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import static io.github.foiovituh.libys.util.StringUtil.PK_EXTENSION;
import static io.github.foiovituh.libys.util.StringUtil.SK_EXTENSION;

public class Identity {
    private static final String DIR = "data/identities";

    public static Ed25519PrivateKeyParameters create(String name)
            throws Exception {
        new File(DIR).mkdirs();

        final Ed25519PrivateKeyParameters sk =
                new Ed25519PrivateKeyParameters(new SecureRandom());

        final byte[] pk = sk.generatePublicKey().getEncoded();

        Files.write(new File(DIR, name + SK_EXTENSION).toPath(),
                sk.getEncoded());
        Files.write(new File(DIR, name + PK_EXTENSION).toPath(), pk);

        System.out.println("Identity created: " + name);
        System.out.println("Pubkey: " + bytesToHex(pk));

        return sk;
    }

    public static Ed25519PrivateKeyParameters load(String name)
            throws Exception {

        final byte[] key = Files.readAllBytes(
                new File(DIR, name + SK_EXTENSION).toPath()
        );

        return new Ed25519PrivateKeyParameters(key, 0);
    }

    static String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }
}