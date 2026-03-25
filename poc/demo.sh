#!/usr/bin/env bash
set -euo pipefail

readonly ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
readonly MAIN_CLASS='io.github.foiovituh.libys.LibysPoc'
readonly JAVA_DIR="${ROOT_DIR}/java"
readonly JAR="${JAVA_DIR}/target/libys-core-0.1.jar"
readonly IDENTITIES_DIR="${ROOT_DIR}/data/identities"
readonly EVENTS_DIR="${ROOT_DIR}/data/events"

build() {
  printf "\n==> Building Java PoC\n"
  (cd "$JAVA_DIR" && mvn -q clean package)
}

run() {
  (cd "$ROOT_DIR" && java -cp "$JAR" "$MAIN_CLASS" "$@")
}

clean() {
  rm -rf "${ROOT_DIR}/data"
}

get_last_event_id() {
  local id=$(ls -t "$EVENTS_DIR" 2>/dev/null | head -n1 | sed 's/.json$//')

  if [ -z "$id" ]; then
    echo "ERROR: no events found"
    exit 1
  fi

  echo "$id"
}

get_pubkey() {
  local file="${IDENTITIES_DIR}/$1.pk"

  if [ ! -f "$file" ]; then
    echo "ERROR: identity not found: $1"
    exit 1
  fi

  xxd -p -c 256 "$file"
}

short() {
  printf "%s\n" "$1" | cut -c1-10
}

separator() {
  printf "%s\n" "--------------------------------"
}

count_files() {
  find "$1" -type f 2>/dev/null | wc -l
}

print_header() {
  separator
  printf "LIBYS PoC demonstration\n"
  separator
}

create_identities() {
  printf "\n==> Creating identities\n"

  run new-id alice >/dev/null
  run new-id vitor >/dev/null
  run new-id shop >/dev/null
  run new-id api >/dev/null

  ALICE_PK="$(get_pubkey alice)"
  VITOR_PK="$(get_pubkey vitor)"
  SHOP_PK="$(get_pubkey shop)"
  API_PK="$(get_pubkey api)"

  printf "alice  -> %s...\n" "$(short "$ALICE_PK")"
  printf "vitor  -> %s...\n" "$(short "$VITOR_PK")"
  printf "shop   -> %s...\n" "$(short "$SHOP_PK")"
  printf "api    -> %s...\n" "$(short "$API_PK")"
}

basic_interaction() {
  printf "\n==> Basic interaction example\n"
  printf "This demonstrates a simple signed event between identities.\n"

  run event alice social.forum.post "" "" \
  '{"text":"hello world"}' >/dev/null

  POST_ID="$(get_last_event_id)"

  printf "\nalice --[social.forum.post]--> content\n"
}

market_interaction() {
  printf "\n==> Market interaction (reputation signal)\n"
  printf "This represents a real economic interaction between identities.\n"

  run event vitor market.order.completed "$SHOP_PK" "" \
  '{"order_id":"ord_1","amount":100,"currency":"USD"}' >/dev/null

  printf "\nvitor --[market.order.completed]--> shop\n"
}

service_signal() {
  printf "\n==> Service signal (reliability)\n"
  printf "This demonstrates how operational events can be recorded.\n"

  run event api api.status.incident "" "" \
  '{"service":"payments","status":"down","incident_id":"inc_1"}' >/dev/null

  printf "\napi --[api.status.incident]--> system\n"
}

delegation_flow() {
  printf "\n==> Delegation (contextual authority)\n"
  printf "Alice grants API permission to act on her behalf.\n"

  grant_payload='{"types":["social.forum.post"],"expires_at":1893456000}'
  run event alice system.auth.grant "$API_PK" "" "$grant_payload" >/dev/null

  GRANT_ID="$(get_last_event_id)"
  ALLOWED_TYPE="$(jq -r '.content | fromjson | .types[0]' "${EVENTS_DIR}/${GRANT_ID}.json")"

  printf "\nalice --[system.auth.grant]--> api\n"
  printf "  allows: %s\n" "$ALLOWED_TYPE"

  printf "\n==> Delegated action\n"
  printf "API performs an action using delegated authority.\n"

  run event api social.forum.post "" "$GRANT_ID" \
  '{"text":"posted via api"}' >/dev/null

  printf "\napi --[social.forum.post]--> content (authorized)\n"
}

verification() {
  printf "\n==> Verification\n"

  if run verify "$GRANT_ID" >/dev/null 2>&1; then
    printf "system.auth.grant : valid signature and hash\n"
  else
    printf "system.auth.grant : invalid\n"
  fi
}

print_graph() {
  printf "\n==> Resulting trust graph\n"

  printf "alice\n"
  printf "  ├─ social.forum.post\n"
  printf "  └─ system.auth.grant\n"
  printf "       └─ api\n"
  printf "            └─ social.forum.post (via delegation)\n"

  printf "\nvitor\n"
  printf "  └─ market.order.completed -> shop\n"

  printf "\napi\n"
  printf "  └─ api.status.incident\n"
}

summary() {
  printf "\n==> Summary\n"
  printf "identities: %s\n" "$(count_files "$IDENTITIES_DIR")"
  printf "events    : %s\n" "$(count_files "$EVENTS_DIR")"
}

data_visibility() {
  printf "\n==> Data (local storage)\n"
  printf "Events are stored as content-addressed JSON files.\n\n"

  printf "identities:\n"
  printf "  %s\n" "$IDENTITIES_DIR"

  printf "\nevents:\n"
  printf "  %s\n" "$EVENTS_DIR"

  printf "\nexample files:\n"
  ls -1 "$EVENTS_DIR" | head -n2 | sed "s|^|  ${EVENTS_DIR}/|"
}

main() {
  print_header

  clean
  build

  create_identities
  basic_interaction
  market_interaction
  service_signal
  delegation_flow
  verification
  print_graph
  summary
  data_visibility

  printf "\n"
  separator
  printf "PoC finished\n"
  separator
}

main "$@"
