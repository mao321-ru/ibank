#!/bin/bash

# На основе файла ${IMPORT_DIR}/${REALM}.export.json, полученном экспортом realm из web-интерфейса с
# (при экспорте включить groups and roles, clients) формируется файл
# ${IMPORT_DIR}/${REALM}.realm.json с актуальными паролями клиентов (которые не экспортируются).
# Также обновляется файл с тестовым realm в ${IMPORT_TEST_DIR} (должен отличаться от основного
# только секретами сервисов).
#
# Замечания:
# т.к. через web-интерфейс нельзя явно указать secret, можно:
#   - создать клиента
#   - экспортировать realm в ibank.export.json
#   - выполнить этот скрипт получив ibank.realm.json
#   - вручную указать секрет в ibank.realm.json
#   - импортировать ibank.realm.json через web-интерфейс, получив в результате нужный секрет
#

REALM="ibank"

KEYCLOAK_URL="http://localhost:8954"

IMPORT_DIR="./keycloak/import"
IMPORT_TEST_DIR="./common-test/base/src/test/resources/keycloak"

TOKEN=$( \
    curl -s -X POST \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "username=admin" \
      -d "password=admin" \
      -d "grant_type=password" \
      -d "client_id=admin-cli" \
      ${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p' \
)

# данные по клиентам включая secret
curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  "${KEYCLOAK_URL}/admin/realms/${REALM}/clients" \
  > ${IMPORT_DIR}/new_clients.json

# jq скачал для винды: https://github.com/jqlang/jq/releases/download/jq-1.7.1/jq-windows-amd64.exe
jq --slurpfile new ${IMPORT_DIR}/new_clients.json '.clients = $new[0]' ${IMPORT_DIR}/${REALM}.export.json \
  > ${IMPORT_DIR}/${REALM}.realm.json

# Упрощенная реализация установки тестовых секретов для сервисов, которые должны иметь вид
# <clientId>-TestSecret, например "front-service-TestSecret"
# (используется в IntegrationTestBase.java)
sed 's/-Secret"/-TestSecret"/' ${IMPORT_DIR}/${REALM}.realm.json > ${IMPORT_TEST_DIR}/${REALM}.realm.json

rm -f ${IMPORT_DIR}/new_clients.json
