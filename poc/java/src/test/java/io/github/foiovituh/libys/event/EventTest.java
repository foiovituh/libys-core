package io.github.foiovituh.libys.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.foiovituh.libys.crypto.Crypto;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class EventTest {
    private Ed25519PrivateKeyParameters newKey() {
        return new Ed25519PrivateKeyParameters(
            new java.security.SecureRandom()
        );
    }

    @Test
    void shouldCreateValidEvent() throws Exception {
        final var sk = newKey();
        final Event event = EventBuilder.create(
                sk,
                "social.forum.post",
                "",
                "",
                "{\"text\":\"hello\"}"
        );

        assertNotNull(event.id);
        assertNotNull(event.sig);
        assertTrue(EventVerifier.verify(event));
    }

    @Test
    void shouldBeDeterministicForSameInput() throws Exception {
        final var sk = newKey();
        final long fixedTime = 1700000000L;
        final String content = "{\"text\":\"hello\"}";

        final Event e1 = createWithFixedTime(sk, fixedTime, content);
        final Event e2 = createWithFixedTime(sk, fixedTime, content);

        assertEquals(e1.id, e2.id);
        assertEquals(e1.sig, e2.sig);
    }

    @Test
    void shouldChangeIdWhenContentChanges() throws Exception {
        final var sk = newKey();
        final long fixedTime = 1700000000L;

        final Event e1 = createWithFixedTime(sk, fixedTime, "{\"text\":\"hello\"}");
        final Event e2 = createWithFixedTime(sk, fixedTime, "{\"text\":\"hello!\"}");

        assertNotEquals(e1.id, e2.id);
    }

    @Test
    void shouldFailIfContentIsTampered() throws Exception {
        final var sk = newKey();

        final Event event = EventBuilder.create(
                sk,
                "social.forum.post",
                "",
                "",
                "{\"text\":\"hello\"}"
        );

        // tamper
        event.content = "{\"text\":\"hacked\"}";

        assertFalse(EventVerifier.verify(event));
    }

    @Test
    void shouldFailIfSignatureIsTampered() throws Exception {
        final var sk = newKey();

        final Event event = EventBuilder.create(
                sk,
                "social.forum.post",
                "",
                "",
                "{\"text\":\"hello\"}"
        );

        // tamper signature
        event.sig = event.sig.substring(0, event.sig.length() - 2)
                + "aa";

        assertFalse(EventVerifier.verify(event));
    }

    @Test
    void contentStringMustAffectHashExactly() throws Exception {
        final var sk = newKey();
        final long fixedTime = 1700000000L;

        final Event e1 = createWithFixedTime(sk, fixedTime, "{\"a\":1}");
        final Event e2 = createWithFixedTime(sk, fixedTime, "{ \"a\":1 }");

        assertNotEquals(e1.id, e2.id);
    }

    // helper pra fixar timestamp
    private Event createWithFixedTime(Ed25519PrivateKeyParameters sk,
            long ts, String content) throws Exception {
        final byte[] pk = sk.generatePublicKey().getEncoded();
        final String pkHex = Crypto.generateHex(pk);

        final List<Object> arr = List.of(
                pkHex,
                ts,
                "social.forum.post",
                "",
                "",
                content
        );

        final byte[] serialized = new ObjectMapper().writeValueAsBytes(arr);
        final String id = Crypto.generateSha256(serialized);
        final byte[] sig = Crypto.sign(Crypto.hexToBytes(id), sk);

        final Event e = new Event();
        e.id = id;
        e.pubkey = pkHex;
        e.created_at = ts;
        e.type = "social.forum.post";
        e.subject = "";
        e.auth_id = "";
        e.content = content;
        e.sig = Crypto.generateHex(sig);

        return e;
    }
}