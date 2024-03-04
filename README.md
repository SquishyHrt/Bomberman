# Online Bomberman Game Backend

This repository contains the backend for an online Bomberman game. Players place bombs to destroy obstacles and defeat enemies. The backend offers REST endpoints for client communication, using Quarkus and Hibernate.

## Introduction

Bomberman is a classic video game where players strategically place bombs to destroy obstacles and enemies.

## Goal

Create the backend for a Bomberman game with REST endpoints.

## Frameworks

Utilizes Quarkus for lightweight ease of use and Hibernate for ORM.

## How to Start

1. **Set up PostgreSQL environment variables:**
    ```bash
    echo 'export PGDATA="$HOME/postgres_data"' >> ~/.bashrc
    echo 'export PGHOST="/tmp"' >> ~/.bashrc
    source ~/.bashrc
    ```

2. **Initialize a new PostgreSQL database cluster:**
    ```bash
    nix-shell -p postgresql
    initdb --locale "$LANG" -E UTF8
    ```

3. **Create a database named 'jws' and a schema named 'jws':**
    ```bash
    export DB_USERNAME=<login>
    postgres -k "$PGHOST"
    psql postgres
    ALTER ROLE "<login>" SUPERUSER;
    CREATE DATABASE jws OWNER <login>;
    \q
    ```

4. **Extract provided files, open with IntelliJ.**

5. **Start the server:**
    ```bash
    mvn quarkus:dev
    ```

6. **Access viewer:**
    ```bash
    java -jar front-end.jar
    ```

## Endpoints

- **List Games**: Lists registered games.
- **Game Creation**: Create games and players.
- **Get a Specific Game**: Retrieve game info.
- **Join a Game**: Join existing game.
- **Start a Game**: Update game state to RUNNING.
- **Move**: Allow cardinal movement for players.
- **Set Down a Bomb**: Place bombs that destroy obstacles and affect players.
