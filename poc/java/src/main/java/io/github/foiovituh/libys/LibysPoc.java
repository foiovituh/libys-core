// LibysPoc.java
package io.github.foiovituh.libys;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.foiovituh.libys.event.Event;
import io.github.foiovituh.libys.event.EventBuilder;
import io.github.foiovituh.libys.event.EventVerifier;
import io.github.foiovituh.libys.identity.Identity;
import io.github.foiovituh.libys.storage.EventStore;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;

public class LibysPoc {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: java "
                + "io.github.foiovituh.libys.LibysPoc <command>");

            return;
        }

        switch (args[0]) {
            case "new-id":
                if (args.length < 2) {
                    System.out.println("Usage: new-id <name>");
                    return;
                }
                Identity.create(args[1]);

                break;
            case "event":
                if (args.length < 6) {
                    System.out.println("Usage: event <author> <type> <target> <authority> <payload_json>");
                    return;
                }
                final Ed25519PrivateKeyParameters sk = Identity.load(args[1]);
                final Object content = MAPPER.readValue(args[5], Object.class);
                final Event event = EventBuilder.create(
                        sk,
                        args[2],
                        args[3],
                        args[4],
                        content
                );
                EventStore.save(event);

                break;
            case "verify":
                if (args.length < 2) {
                    System.out.println("Usage: verify <event_hash>");
                    return;
                }
                Event ev = EventStore.load(args[1]);
                EventVerifier.verify(ev);

                break;
            default:
                System.out.println("Unknown command: " + args[0]);
        }
    }
}