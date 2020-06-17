INSERT INTO OAUTH_CLIENT_DETAILS(CLIENT_ID, RESOURCE_IDS, CLIENT_SECRET, SCOPE, AUTHORIZED_GRANT_TYPES, AUTHORITIES, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY)
	VALUES ('oauth2-client', 'resource-server-rest-api',
	/*oauth2-client-password*/'$2a$04$SUTTW8QxEvsyKjpicfcAre/VKH3fIpazEN/dlhK0iXznOFSiSkW62',
	'read,write', 'password,authorization_code,refresh_token,implicit', 'USER', 10800, 2592000);
