# Hoof

"Hoof" provides the backend for Playful Programming's content management experience.

This repository houses:
- `hoof-api`: the external-facing REST API
- `hoof-worker`: a background task runner in NodeJS, for image fetching & generation

## Getting Started

Prerequisites:
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Java 23](https://jdk.java.net/23/) (can also be installed via IntelliJ: File -> Project Structure -> Project -> SDK)
- [Node 20](https://nodejs.org/en/download)
- [Docker](https://www.docker.com/get-started/)

To start the postgres database:
```sh
cp .env.example .env
docker compose up --wait
```

To run the API server:
```sh
cd api
./gradlew runFlywayMigrate # runs any database migrations
./gradlew run # (or use the Run button in IntelliJ)
```

To start the worker app:
```sh
cd worker

# Set up pnpm through Corepack: https://nodejs.org/api/corepack.html
corepack enable pnpm
corepack install
corepack up

# Install dependencies & run
pnpm install
pnpm run dev
```
