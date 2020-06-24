#!/bin/bash
curl -X POST -u "oauth2-client:oauth2-client-password" -d "grant_type=password&username=admin&password=admin1234" http://localhost:8080/oauth/token
