# Design Patterns implémentés dans le microservice Reservation

## Auteur
Architecture Logicielle - M2 Architecture et Solutions

## Date
Mars 2026

---

## 1. Problématique métier identifiée

Le microservice Reservation présente deux défis architecturaux majeurs :

### 1.1 Construction complexe d'une réservation
La création d'une réservation nécessite plusieurs validations métier **avant** de pouvoir confirmer la réservation :
- Vérification de la disponibilité de la salle (appel REST vers Room Service)
- Vérification que le membre n'est pas suspendu (appel REST vers Member Service)
- Validation des créneaux horaires (cohérence start < end, pas de réservation dans le passé)

Ces validations étaient initialement dispersées dans la méthode `createReservation()` du service, créant une forte complexité et un couplage élevé.

### 1.2 Gestion des transitions d'état
Une réservation suit un cycle de vie strict avec des règles de transition :
- **CONFIRMED** → peut devenir **COMPLETED** ou **CANCELLED**
- **COMPLETED** → état terminal (aucune transition possible)
- **CANCELLED** → état terminal (aucune transition possible)

Les transitions interdites doivent être bloquées (ex: on ne peut pas compléter une réservation déjà annulée).

Chaque transition doit publier un événement Kafka pour informer les autres microservices (notamment Member Service pour gérer les quotas).

---

## 2. Patterns choisis

Nous avons implémenté **deux Design Patterns complémentaires** :

### 2.1 Builder Pattern (Créationnel)
**Responsabilité** : Encapsuler la construction complexe d'une réservation avec toutes ses validations.

### 2.2 State Pattern (Comportemental)
**Responsabilité** : Gérer les transitions d'état et les comportements spécifiques à chaque état.

---

## 3. Builder Pattern - Justification et implémentation

### 3.1 Pourquoi le Builder Pattern ?

Le **Builder Pattern** est particulièrement adapté lorsque :
- La construction d'un objet nécessite plusieurs étapes complexes
- Les validations doivent être effectuées avant la création
- On veut garantir que l'objet créé est toujours dans un état valide
- On souhaite une API fluide et lisible

Dans notre cas, créer une réservation valide requiert :
1. Vérifier la disponibilité de la salle
2. Vérifier que le membre n'est pas suspendu
3. Valider les créneaux horaires
4. Créer l'objet avec le statut CONFIRMED

### 3.2 Diagramme de classes

```
┌─────────────────────────────────────────────────────────────┐
│                    ReservationBuilder                       │
├─────────────────────────────────────────────────────────────┤
│ - roomId: Long                                              │
│ - memberId: Long                                            │
│ - startDateTime: LocalDateTime                              │
│ - endDateTime: LocalDateTime                                │
│ - roomClient: RoomClient                                    │
│ - memberClient: MemberClient                                │
├─────────────────────────────────────────────────────────────┤
│ + withRoomId(Long): ReservationBuilder                      │
│ + withMemberId(Long): ReservationBuilder                    │
│ + withTimeSlot(start, end): ReservationBuilder              │
│ + build(): Reservation                                      │
│ - validateRoom(): void                                      │
│ - validateMember(): void                                    │
│ - validateTimeSlot(): void                                  │
│ - reset(): void                                             │
└─────────────────────────────────────────────────────────────┘
                         │
                         │ creates
                         ▼
            ┌────────────────────────┐
            │     Reservation        │
            ├────────────────────────┤
            │ - id: Long             │
            │ - roomId: Long         │
            │ - memberId: Long       │
            │ - startDateTime        │
            │ - endDateTime          │
            │ - status: CONFIRMED    │
            └────────────────────────┘
```

### 3.3 Exemple d'utilisation

**Avant (sans Builder)** :
```java
public Reservation createReservation(Reservation reservation) {
    // Validation de la salle
    Map<String, Boolean> roomAvailability = roomClient.checkAvailability(reservation.getRoomId());
    if (!roomAvailability.get("available")) {
        throw new BusinessRuleException("Room is not available");
    }
    
    // Validation du membre
    Map<String, Boolean> memberSuspension = memberClient.isSuspended(reservation.getMemberId());
    if (memberSuspension.get("suspended")) {
        throw new BusinessRuleException("Member is suspended");
    }
    
    // Création
    reservation.setStatus(ReservationStatus.CONFIRMED);
    return reservationRepository.save(reservation);
}
```

**Après (avec Builder)** :
```java
public Reservation createReservation(Reservation reservationRequest) {
    Reservation reservation = reservationBuilder
        .withRoomId(reservationRequest.getRoomId())
        .withMemberId(reservationRequest.getMemberId())
        .withTimeSlot(reservationRequest.getStartDateTime(), reservationRequest.getEndDateTime())
        .build(); // Toutes les validations sont effectuées ici
    
    return reservationRepository.save(reservation);
}
```

### 3.4 Bénéfices apportés

✅ **Séparation des responsabilités** : La logique de validation est déportée hors du service  
✅ **Réutilisabilité** : Le builder peut être utilisé dans différents contextes (tests, migrations, etc.)  
✅ **Lisibilité** : L'API fluide rend le code plus expressif  
✅ **Testabilité** : Le builder peut être testé indépendamment du service  
✅ **Garantie de cohérence** : Impossible de créer une réservation invalide  

---

## 4. State Pattern - Justification et implémentation

### 4.1 Pourquoi le State Pattern ?

Le **State Pattern** est adapté lorsque :
- Un objet a plusieurs états avec des comportements différents
- Les transitions entre états suivent des règles strictes
- On veut éviter les `if/else` ou `switch` sur les états
- Chaque état peut avoir une logique complexe

Dans notre cas :
- Une réservation a 3 états : CONFIRMED, COMPLETED, CANCELLED
- Seules certaines transitions sont autorisées
- Chaque transition doit publier un événement Kafka
- Les états terminaux (COMPLETED, CANCELLED) doivent bloquer toute nouvelle transition

### 4.2 Diagramme d'états

```
                    ┌───────────────┐
                    │   [CREATE]    │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │   CONFIRMED   │
                    └───┬───────┬───┘
                        │       │
            cancel()    │       │    complete()
                        │       │
                        ▼       ▼
                ┌──────────┐  ┌──────────┐
                │CANCELLED │  │COMPLETED │
                │ (final)  │  │ (final)  │
                └──────────┘  └──────────┘
                
Règles de transition :
- CONFIRMED → COMPLETED : OK (publie événement Kafka)
- CONFIRMED → CANCELLED : OK (publie événement Kafka)
- COMPLETED → * : INTERDIT (exception)
- CANCELLED → * : INTERDIT (exception)
```

### 4.3 Diagramme de classes

```
┌────────────────────────────────────────┐
│      <<interface>>                     │
│      ReservationState                  │
├────────────────────────────────────────┤
│ + cancel(Reservation): void            │
│ + complete(Reservation): void          │
│ + getStateName(): String               │
└────────────────┬───────────────────────┘
                 │
      ┌──────────┴──────────┬─────────────────┐
      │                     │                 │
      ▼                     ▼                 ▼
┌─────────────┐      ┌─────────────┐   ┌─────────────┐
│ConfirmedState│      │CompletedState│   │CancelledState│
├─────────────┤      ├─────────────┤   ├─────────────┤
│ + cancel()  │      │ + cancel()  │   │ + cancel()  │
│ + complete()│      │ + complete()│   │ + complete()│
└─────────────┘      └─────────────┘   └─────────────┘
      │                    │                  │
      │                    │                  │
   Publie              Exception           Exception
   événement           levée               levée
   Kafka
```

### 4.4 Implémentation des états

#### ConfirmedState (État actif)
```java
@Component
public class ConfirmedState implements ReservationState {
    
    @Override
    public void cancel(Reservation reservation) {
        // Change le statut
        reservation.setStatus(ReservationStatus.CANCELLED);
        // Change l'état
        reservation.changeState(cancelledState);
        // Publie l'événement Kafka
        eventPublisher.publishReservationStatusChanged(...);
    }
    
    @Override
    public void complete(Reservation reservation) {
        // Change le statut
        reservation.setStatus(ReservationStatus.COMPLETED);
        // Change l'état
        reservation.changeState(completedState);
        // Publie l'événement Kafka
        eventPublisher.publishReservationStatusChanged(...);
    }
}
```

#### CompletedState et CancelledState (États terminaux)
```java
@Component
public class CompletedState implements ReservationState {
    
    @Override
    public void cancel(Reservation reservation) {
        throw new BusinessRuleException("Cannot cancel a completed reservation");
    }
    
    @Override
    public void complete(Reservation reservation) {
        throw new BusinessRuleException("Reservation is already completed");
    }
}
```

### 4.5 Utilisation dans le modèle Reservation

```java
@Entity
public class Reservation {
    
    // Champ persisté en base
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    
    // État comportemental (transient, non persisté)
    @Transient
    private ReservationState currentState;
    
    // Méthodes déléguant au state
    public void cancel() {
        if (currentState != null) {
            currentState.cancel(this);
        }
    }
    
    public void complete() {
        if (currentState != null) {
            currentState.complete(this);
        }
    }
}
```

### 4.6 Exemple d'utilisation dans le service

**Avant (sans State Pattern)** :
```java
public Reservation cancelReservation(Long id) {
    Reservation reservation = reservationRepository.findById(id).orElseThrow(...);
    
    ReservationStatus previousStatus = reservation.getStatus();
    reservation.setStatus(ReservationStatus.CANCELLED);
    Reservation updated = reservationRepository.save(reservation);
    
    // Publication manuelle de l'événement
    eventPublisher.publishReservationStatusChanged(...);
    
    return updated;
}
```

**Après (avec State Pattern)** :
```java
public Reservation cancelReservation(Long id) {
    Reservation reservation = reservationRepository.findById(id).orElseThrow(...);
    
    // Le state gère la transition ET la publication de l'événement
    reservation.cancel();
    
    return reservationRepository.save(reservation);
}
```

### 4.7 Bénéfices apportés

✅ **Validation des transitions** : Impossible d'effectuer une transition interdite  
✅ **Encapsulation** : Chaque état contient sa propre logique  
✅ **Élimination des conditions** : Plus de `if (status == CANCELLED) { ... }`  
✅ **Publication automatique** : Les événements Kafka sont publiés par le state  
✅ **Extensibilité** : Ajout facile de nouveaux états (PENDING, EXPIRED)  
✅ **Open/Closed Principle** : Ouvert à l'extension, fermé à la modification  

---

## 5. Synergie des deux patterns

Les deux patterns se complètent parfaitement :

```
    ReservationBuilder          Reservation           ReservationState
          │                          │                       │
          │                          │                       │
    ┌─────▼─────┐             ┌──────▼──────┐        ┌─────▼─────┐
    │  Valide   │             │   Contient  │        │  Gère les │
    │    et     │   creates   │    état     │  uses  │transitions│
    │  Construit├────────────►│ comportemental├───────►│    et     │
    │           │             │             │        │événements │
    └───────────┘             └─────────────┘        └───────────┘
```

**Flux complet** :
1. **Builder** : Valide et construit une réservation avec statut CONFIRMED
2. **Service** : Persiste la réservation et lui attribue un ConfirmedState
3. **State** : Gère les transitions (cancel/complete) et publie les événements Kafka

---

## 6. Couverture de tests

### 6.1 Tests du Builder (12 tests)
- ✅ Construction avec données valides
- ✅ Salle indisponible → exception
- ✅ Membre suspendu → exception
- ✅ Date de fin avant date de début → exception
- ✅ Date de début = date de fin → exception
- ✅ Réservation dans le passé → exception
- ✅ RoomId null → exception
- ✅ MemberId null → exception
- ✅ Créneaux horaires null → exception
- ✅ RoomClient en erreur → exception
- ✅ MemberClient en erreur → exception
- ✅ Réinitialisation du builder entre deux builds

### 6.2 Tests des States (13 tests)

**ConfirmedState (7 tests)** :
- ✅ cancel() change le statut à CANCELLED
- ✅ cancel() change l'état vers CancelledState
- ✅ cancel() publie un événement Kafka
- ✅ complete() change le statut à COMPLETED
- ✅ complete() change l'état vers CompletedState
- ✅ complete() publie un événement Kafka
- ✅ getStateName() retourne "CONFIRMED"

**CompletedState (3 tests)** :
- ✅ cancel() lève une exception
- ✅ complete() lève une exception
- ✅ getStateName() retourne "COMPLETED"

**CancelledState (3 tests)** :
- ✅ cancel() lève une exception
- ✅ complete() lève une exception
- ✅ getStateName() retourne "CANCELLED"

**Résultat** : **25 tests au total, 100% de succès**

---

## 7. Conclusion

L'implémentation conjointe du **Builder Pattern** et du **State Pattern** dans le microservice Reservation apporte :

### Améliorations immédiates
- Code plus lisible et maintenable
- Séparation claire des responsabilités
- Validation robuste des données
- Transitions d'état sécurisées
- Tests exhaustifs et indépendants

### Bénéfices à long terme
- **Évolutivité** : Ajout facile de nouveaux états (PENDING, EXPIRED, REFUNDED)
- **Maintenabilité** : Modifications isolées dans des classes spécifiques
- **Fiabilité** : Impossible de créer des réservations invalides ou des transitions illégales
- **Observabilité** : Publication automatique des événements Kafka pour traçabilité

### Conformité aux principes SOLID
- **S**ingle Responsibility : Chaque classe a une responsabilité unique
- **O**pen/Closed : Ouvert à l'extension (nouveaux états), fermé à la modification
- **L**iskov Substitution : Tous les states implémentent la même interface
- **I**nterface Segregation : Interface ReservationState minimale et cohérente
- **D**ependency Inversion : Le service dépend d'abstractions (interface State)

---

## 8. Références

- **Builder Pattern** : "Design Patterns: Elements of Reusable Object-Oriented Software" - Gang of Four
- **State Pattern** : "Design Patterns: Elements of Reusable Object-Oriented Software" - Gang of Four
- **Spring Framework** : https://spring.io/projects/spring-boot
- **Apache Kafka** : https://kafka.apache.org/documentation/
