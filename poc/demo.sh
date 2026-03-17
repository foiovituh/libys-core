#!/usr/bin/env bash
set -euo pipefail

readonly ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
readonly MAIN_CLASS='io.github.foiovituh.libys.LibysPoc'
readonly JAVA_DIR="${ROOT_DIR}/java"
readonly JAR="${JAVA_DIR}/target/libys-core-0.1.jar"
readonly IDENTITIES_DIR="${ROOT_DIR}/data/identities"
readonly EVENTS_DIR="${ROOT_DIR}/data/events"

build() {
  echo "==> Building Java PoC"
  (cd "$JAVA_DIR" && mvn -q clean package)
}

run() {
  (cd "$ROOT_DIR" && java -cp "$JAR" "$MAIN_CLASS" "$@")
}

clean() {
  rm -rf "${ROOT_DIR}/data"
}

get_last_event_id() {
  ls -t "$EVENTS_DIR" | head -n1 | sed 's/.json$//'
}

get_pubkey() {
  xxd -p -c 256 "${IDENTITIES_DIR}/$1.pk"
}

short() {
  echo "$1" | cut -c1-10
}

separator() {
  echo "--------------------------------"
}

main() {
  echo
  separator
  echo "LIBYS PoC demonstration"
  separator
  echo

  clean
  build

  echo
  echo "==> Creating identities"
  run new-id alice
  run new-id vitor

  VITOR_PK="$(get_pubkey vitor)"
  
  echo
  echo "==> Grant"
  grant_payload='{"types":["social.forum.post"],"expires_at":1893456000}'
  run event alice system.auth.grant "$VITOR_PK" "" "$grant_payload"

  GRANT_ID="$(get_last_event_id)"
  GRANT_FILE="${EVENTS_DIR}/${GRANT_ID}.json"
  ALLOWED_TYPE=$(jq -r '.content.types[0]' "$GRANT_FILE")

  echo "  Alice -> Vitor"
  echo "  allows: $ALLOWED_TYPE"
  echo "  id    : $(short "$GRANT_ID")..."

  echo
  echo "==> Delegated event"
  run event vitor social.forum.post "" "$GRANT_ID" '"hello from delegated authority"'

  POST_ID="$(get_last_event_id)"
  POST_FILE="${EVENTS_DIR}/${POST_ID}.json"
  EVENT_TYPE=$(jq -r '.type' "$POST_FILE")

  echo "  Vitor -> $EVENT_TYPE"
  echo "  auth  : $(short "$GRANT_ID")..."
  echo "  id    : $(short "$POST_ID")..."

  echo
  echo "==> Verifying events"

  run verify "$GRANT_ID"
  run verify "$POST_ID"
  
  echo
  echo "==> Authority chain"
  echo
  echo "  Alice"
  echo "    +- grant -> Vitor"
  echo "         +- allows: $ALLOWED_TYPE"
  echo "              +- Vitor -> post (authorized)"

  echo
  echo "==> Files"
  echo "  identities: $(ls -1 "$IDENTITIES_DIR" | wc -l)"
  echo "  events    : $(ls -1 "$EVENTS_DIR" | wc -l)"

  echo
  separator
  echo "PoC finished"
  separator
  echo
}

main "$@"