#!/bin/bash
curl -X POST -u "oauth2-client:oauth2-client-password" -d "grant_type=password&username=user1&password=user1password" http://localhost:8080/oauth/token
