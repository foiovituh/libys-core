# 🕸️ LIBYS Protocol
![GitHub License](https://img.shields.io/github/license/foiovituh/libys-core)
 
> <b>Abstract</b>. A purely decentralized version of reputation would allow interactions to be recorded without the need for a central mediator. We propose LIBYS, a protocol where verifiable interaction events between cryptographic identities form a shared graph. Reputation can be derived from hierarchical domains and controlled by temporary capabilities (contextual authority) granted by the user. The protocol creates a reconstruction burden for identity manipulation or abandonment, ensuring that an identity's utility is proportional to its history of legitimate and authorized interactions.

## 📄 Whitepaper
Read the full technical specification: [`WHITEPAPER.pdf`](./WHITEPAPER.pdf)
 
## 📦 Proof of Concept (PoC)
This repository contains a Java-based implementation of the core cryptographic primitives and a Bash-orchestrated demonstration.
 
### Prerequisites
- Java 17+ & Maven
- `jq` and `xxd` (for the demo script)

### Quick Start
To see the protocol in action (identity creation, authority delegation, and event emission), run the automated demo:

```bash
chmod +x poc/demo.sh
./poc/demo.sh
```

### Manual Testing
You can interact with the CLI directly. Detailed examples of `new-id`, `event`, and `verify` commands are available in [poc/NOTES.md](./poc/NOTES.md).

## ⭐ Support the Project
If you find the concept of sovereign reputation interesting, please give this project a star! It helps with visibility and motivates continued development of the LIBYS protocol.
 
## 📄 License
Distributed under the MIT License. See [LICENSE](./LICENSE) for more information.