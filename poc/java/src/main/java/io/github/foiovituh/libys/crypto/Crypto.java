package io.github.foiovituh.libys.crypto;

import java.security.MessageDigest;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;

public class Crypto {
    public static String generateSha256(byte[] data) throws Exception {
        final MessageDigest digest = MessageDigest
            .getInstance("SHA-256");

        return generateHex(digest.digest(data));
    }

    public static byte[] sign(byte[] message, Ed25519PrivateKeyParameters sk) {
        final Ed25519Signer signer = new Ed25519Signer();

        signer.init(true, sk);
        signer.update(message, 0, message.length);

        return signer.generateSignature();
    }

    public static boolean verify(byte[] message, byte[] sig, byte[] pubkey) {
        final Ed25519Signer verifier = new Ed25519Signer();

        verifier.init(false, new Ed25519PublicKeyParameters(pubkey, 0));
        verifier.update(message, 0, message.length);

        return verifier.verifySignature(sig);
    }

    public static String generateHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    public static byte[] hexToBytes(String hex) {
        final byte[] out = new byte[hex.length() / 2];

        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(
                hex.substring(i * 2, i * 2 + 2),
                16)
            ;
        }

        return out;
    }
}