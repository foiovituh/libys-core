package io.github.foiovituh.libys.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.foiovituh.libys.crypto.Crypto;
import java.time.Instant;
import java.util.List;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;

public class EventBuilder {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Event create(Ed25519PrivateKeyParameters sk, String type,
            String subject, String authId, Object content) throws Exception {
        final byte[] pk = sk.generatePublicKey().getEncoded();
        final long created = Instant.now().getEpochSecond();
        final String pkHex = Crypto.generateHex(pk);
        List<Object> arr = List.of(
                pkHex,
                created,
                type,
                subject,
                authId,
                content
        );
        byte[] serialized = MAPPER.writeValueAsBytes(arr);
        String id = Crypto.generateSha256(serialized);
        byte[] sig = Crypto.sign(Crypto.hexToBytes(id), sk);
        Event event = new Event();

        event.id = id;
        event.pubkey = pkHex;
        event.created_at = created;
        event.type = type;
        event.subject = subject;
        event.auth_id = authId;
        event.content = content;
        event.sig = Crypto.generateHex(sig);

        return event;
    }
}