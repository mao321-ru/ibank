#!/bin/bash

TOKEN=$( \
    curl -s -X POST \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "username=admin" \
      -d "password=admin" \
      -d "grant_type=password" \
      -d "client_id=admin-cli" \
      http://localhost:8954/realms/master/protocol/openid-connect/token \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p' \
)

# данные по клиентам включая secret
curl -s -X GET \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8954/admin/realms/ibank/clients" \
  > ./new_clients.json

jq --slurpfile new new_clients.json '.clients = $new[0]' ./import/ibank.export.json \
  > ./import/ibank.realm.json

rm -f ./clients-data.json ./new_clients.json
