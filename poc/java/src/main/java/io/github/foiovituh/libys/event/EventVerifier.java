package io.github.foiovituh.libys.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.foiovituh.libys.crypto.Crypto;
import java.util.List;

public class EventVerifier {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static boolean verify(Event event) throws Exception {
        List<Object> arr = List.of(
                event.pubkey,
                event.created_at,
                event.type,
                event.subject,
                event.auth_id,
                event.content
        );
        final byte[] serialized = MAPPER.writeValueAsBytes(arr);
        final String computed = Crypto.generateSha256(serialized);

        if (!computed.equals(event.id)) {
            return false;
        }

        final boolean ok = Crypto.verify(
                Crypto.hexToBytes(event.id),
                Crypto.hexToBytes(event.sig),
                Crypto.hexToBytes(event.pubkey)
        );

        if (!ok) {
            return false;
        }

        return true;
    }
}