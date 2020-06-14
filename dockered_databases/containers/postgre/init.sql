CREATE USER server_user WITH PASSWORD 'ERVb95UQhjV7pBnKUxHCe9nrqRrq7tGQ';
CREATE DATABASE katolk_database;
GRANT ALL PRIVILEGES ON DATABASE katolk_database TO server_user;
ALTER USER server_user VALID UNTIL 'infinity';
