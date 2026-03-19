# Architecture Microservices - Plateforme de coworking

Projet d'architecture logicielle pour la gestion d'une plateforme de réservation de salles de coworking.

## Architecture

L'application est construite selon une architecture microservices avec Spring Boot et Spring Cloud :

```
                        API Gateway (8080)
                              │
              ┌───────────────┼───────────────┐
              │               │               │
         Room Service    Member Service   Reservation Service
            (8081)          (8082)            (8083)
              │               │               │
              └───────────────┼───────────────┘
                              │
                       Apache Kafka
```

### Infrastructure

- **Config Server** (8888) : Configuration centralisée
- **Discovery Server** (8761) : Eureka pour l'enregistrement des services
- **API Gateway** (8080) : Point d'entrée unique avec routage

### Microservices métier

- **Room Service** : Gestion des salles de coworking (CRUD, disponibilité)
- **Member Service** : Gestion des membres et abonnements (BASIC, PRO, ENTERPRISE)
- **Reservation Service** : Gestion des réservations avec validations cross-services

## Design Patterns implémentés

Le microservice **Reservation** implémente deux Design Patterns complémentaires :

### 1. Builder Pattern (Créationnel)
- **Objectif** : Encapsuler la construction complexe d'une réservation
- **Validations** : Disponibilité de la salle, suspension du membre, cohérence des créneaux
- **Classe** : `ReservationBuilder`

### 2. State Pattern (Comportemental)
- **Objectif** : Gérer les transitions d'état de manière sûre
- **États** : CONFIRMED → COMPLETED / CANCELLED
- **Classes** : `ReservationState`, `ConfirmedState`, `CompletedState`, `CancelledState`

📖 **Documentation complète** : Voir [DESIGN_PATTERN.md](DESIGN_PATTERN.md)

## Prérequis

- Java 17+
- Maven 3.8+
- Docker (optionnel, pour Kafka)

## Lancement de l'application

### 1. Démarrer Kafka (avec Docker Compose)

```bash
docker-compose up -d
```

### 2. Démarrer les services d'infrastructure

```bash
# Config Server
cd config-server
./mvnw spring-boot:run

# Discovery Server (Eureka)
cd discovery-server
./mvnw spring-boot:run

# API Gateway
cd api-gateway
./mvnw spring-boot:run
```

### 3. Démarrer les microservices métier

```bash
# Room Service
cd room-service
./mvnw spring-boot:run

# Member Service
cd member-service
./mvnw spring-boot:run

# Reservation Service
cd reservation-service
./mvnw spring-boot:run
```

### 4. Accéder aux services

- **API Gateway** : http://localhost:8080
- **Eureka Dashboard** : http://localhost:8761
- **Config Server** : http://localhost:8888

## Tests

### Exécuter tous les tests d'un service

```bash
cd reservation-service
./mvnw test
```

### Tests des Design Patterns

```bash
cd reservation-service
./mvnw test -Dtest=ReservationBuilderTest,ConfirmedStateTest,CompletedStateTest,CancelledStateTest
```

**Couverture** : 25 tests pour les Design Patterns (Builder + State)

## Documentation API

Chaque microservice expose sa documentation Swagger :

- Room Service : http://localhost:8081/swagger-ui.html
- Member Service : http://localhost:8082/swagger-ui.html
- Reservation Service : http://localhost:8083/swagger-ui.html

## Événements Kafka

### Topics utilisés

- `room.deleted` : Suppression d'une salle → annulation des réservations associées
- `member.deleted` : Suppression d'un membre → suppression des réservations associées
- `reservation.created` : Création d'une réservation → vérification des quotas
- `reservation.status.changed` : Changement de statut → mise à jour de la suspension du membre

### Logique métier asynchrone

1. **Suppression d'une salle** : Toutes les réservations CONFIRMED de cette salle sont automatiquement annulées
2. **Suppression d'un membre** : Toutes les réservations du membre sont supprimées
3. **Quota atteint** : Si un membre atteint son quota, `suspended` passe à `true`
4. **Réservation libérée** : Si une réservation est annulée/complétée et que le membre était suspendu, `suspended` repasse à `false`

## Structure du projet

```
.
├── config-server/          # Serveur de configuration centralisée
├── discovery-server/       # Eureka pour la découverte de services
├── api-gateway/            # Gateway API avec Spring Cloud Gateway
├── room-service/           # Microservice de gestion des salles
├── member-service/         # Microservice de gestion des membres
├── reservation-service/    # Microservice de gestion des réservations
│   ├── src/main/java/com/coworking/reservation/
│   │   ├── builder/        # Builder Pattern
│   │   ├── state/          # State Pattern
│   │   ├── model/          # Entités JPA
│   │   ├── service/        # Logique métier
│   │   ├── controller/     # API REST
│   │   └── event/          # Événements Kafka
│   └── src/test/          # Tests unitaires
├── DESIGN_PATTERN.md       # Documentation des Design Patterns
└── README.md               # Ce fichier
```

## Auteur

Architecture Logicielle - M2 Architecture et Solutions  
Mars 2026