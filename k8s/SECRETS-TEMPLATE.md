# Secrets required, not committed to this repo

Create these manually before deploying — actual values live only in your local `k8s/*-secret.yaml` files, which are gitignored.

- `postgres-secret.yaml` — POSTGRES_USER, POSTGRES_PASSWORD
- `auth-service-secret.yaml` — SPRING_DATASOURCE_PASSWORD, JWT_SECRET
- `product-service-secret.yaml` — primary/replica DB passwords
- `order-service-secret.yaml` — shard0/1/2 DB passwords
- `api-gateway-secret.yaml` — JWT_SECRET (must match auth-service's value exactly)

JWT_SECRET is currently a placeholder value, not a real production secret — replace before this project goes anywhere beyond local learning.
