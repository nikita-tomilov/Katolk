#!/bin/bash
curl -X POST -u "oauth2-client:oauth2-client-password" -d "grant_type=password&username=user2&password=user2password" http://localhost:8081/oauth/token
