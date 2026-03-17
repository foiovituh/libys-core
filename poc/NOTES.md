# LIBYS Java PoC

## Quick Demo Note

A quick demonstration of the PoC can be run using the provided demo.sh script:

./demo.sh

- This will automatically create identities, issue a grant, and post events.  
- Observers can follow the script output to see the authority and social graph relationships in action.  
- Alternatively, users can perform manual tests by running the CLI commands (new-id, event, verify) as described below.  

------------------------------------------------------------------------

## What the PoC Already Validates

The current proof-of-concept demonstrates the core primitives of the
LIBYS protocol:

### 1. Cryptographic Identity

Command:

    new-id <name>

Example:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc new-id vitor

This validates:

-   Keypair generation
-   Identity persistence
-   Public key exposure

Conceptual model:

    identity -> keypair

------------------------------------------------------------------------

### 2. Event Creation

Command:

    event <author> <type> <target> <authority> <payload_json>

Example:

    java -cp target/libys-core-0.1.jar \
    io.github.foiovituh.libys.LibysPoc \
    event vitor system.auth.grant \
    <PUBKEY> \
    "" \
    '{"types":["social.forum.post"],"expires_at":9999999999}'

This validates:

-   Event construction
-   Payload serialization (JSON)
-   Event hashing
-   Event signing
-   Local event storage

Conceptual model:

    event -> hash -> signature

------------------------------------------------------------------------

### 3. Event Verification

Command:

    verify <event_hash>

Example:

    java -cp target/libys-core-0.1.jar \
    io.github.foiovituh.libys.LibysPoc verify <EVENT_HASH>

This validates:

-   Hash integrity
-   Signature verification

Conceptual model:

    verify(event)

------------------------------------------------------------------------

## Delegation Example Tested

Steps executed in the PoC:

1.  Create identity "vitor"
2.  Create identity "alice"
3.  Emit authorization event
4.  Alice emits a social event

Graph representation:

    vitor
      |
      └─ system.auth.grant
            |
            └─ alice
                 |
                 └─ social.forum.post

This demonstrates that LIBYS can represent a **social or authority graph
using signed events**.

------------------------------------------------------------------------

## Social Interaction Example Tested

Additional steps executed in the PoC:

1.  Alice creates a forum post
2.  Bob reacts to the post using social.like

Example command:

    java -cp target/libys-core-0.1.jar \
    io.github.foiovituh.libys.LibysPoc \
    event bob social.like <POST_HASH> "" '{}'

Graph representation:

    alice ── social.forum.post ──> post
                                    ▲
                                    │
    bob ── social.like ─────────────┘

This demonstrates that LIBYS can represent **social interactions between
identities through event relationships**.

Conceptual model:

    identity -> event -> target

------------------------------------------------------------------------

## Event Immutability Test

A manual tampering test was performed.

Procedure:

1.  Create an event normally
2.  Manually modify the JSON file (e.g., change the id field)
3.  Run verification

Result:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc verify <event_hash> -> invalid event

However:

-   Adding whitespace or line breaks **does not break verification**
-   Modifying actual event data **invalidates the signature**

This confirms that the PoC correctly enforces:

    event integrity -> hash + signature

------------------------------------------------------------------------

## Important NOTE (PoC Scope)

Authorization rules are **not enforced** in this proof-of-concept.

Example observed:

    alice -> social.forum.post

was accepted even without checking whether a valid system.auth.grant
exists.

This is intentional for the PoC.

Proposed documentation note:

    NOTE: Authorization rules are not enforced in this PoC.
    Events represent claims that can be interpreted by external
    reputation or authority engines.

------------------------------------------------------------------------

## LIBYS Architecture Philosophy

LIBYS is designed as a **protocol layer for verifiable events**, not as
an authority engine.

Layer model:

    LIBYS -> event layer
    Apps / users -> interpretation layer

Meaning:

-   LIBYS records **signed, immutable events**.
-   Applications decide **how to interpret those events**.

Example:

    system.auth.grant

This does not automatically grant permission.

It represents a **verifiable claim**, which an application may choose to
accept, reject, or weight differently.

------------------------------------------------------------------------

## CLI Structure Implemented

Current commands:

    new-id
    create identity

    event
    create signed event

    verify
    verify event integrity and signature

Example invocation pattern:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc <command>

Examples:

Create identity:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc new-id vitor

Create event:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc event ...

Verify event:

    java -cp target/libys-core-0.1.jar io.github.foiovituh.libys.LibysPoc verify <hash>

------------------------------------------------------------------------

## Storage Model (Observed)

Events are stored locally as JSON files:

    events/<event_hash>.json

This reflects a **content-addressed storage model**.

Conceptual model:

    event content -> hash -> file id

------------------------------------------------------------------------

## Tests Already Performed

The PoC successfully demonstrated:

1.  Identity creation
2.  Event creation
3.  Event hashing
4.  Event signing
5.  Event verification
6.  Delegation event (system.auth.grant)
7.  Social event emission (social.forum.post)
8.  Social interaction event (social.like)
9.  Event immutability verification

These tests validate the **core protocol mechanics of LIBYS**.