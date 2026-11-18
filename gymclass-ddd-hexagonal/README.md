# GymClass DDD - Hexagonal Architecture (Ports and Adapters)

Educational Spring Boot project that models gym members, group classes and bookings using Domain-Driven Design (DDD) inside a Hexagonal Architecture.

The business domain is intentionally the same as the previous four-layer GymClass DDD version. The main change is architectural: the application core exposes **input ports**, requires **output ports**, and external technologies are connected through **adapters**.

## 1. Technology stack

- Java 17
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA / Hibernate
- H2 in-memory database
- Jakarta Bean Validation (only in the REST adapter)
- Maven
- JUnit 5 / Spring Boot Test

## 2. Business domain

GymClass manages members and bookings for group classes such as Yoga, Pilates and Spinning.

Business rules demonstrated by the model:

1. A member must have an active membership to book a class.
2. A membership plan restricts the types of classes a member can attend.
3. A class session has a maximum capacity.
4. A member cannot have two confirmed bookings for the same class session.
5. Bookings close when a class session starts.
6. A confirmed booking can be cancelled before the class starts; cancellation releases a place.

## 3. Hexagonal architecture

The architecture is organized around the application core. The core does not know whether it is being driven by HTTP, a command-line interface, a message broker or a test. It also does not know whether data is stored in H2, PostgreSQL, a file or another system.

```text
                         DRIVING SIDE
                   (Primary / Inbound Adapter)
                REST Controller / HTTP / JSON
                             |
                             v
                     INPUT PORTS
                 GymClassInputPort
                             |
                             v
        +---------------------------------------------+
        |              APPLICATION CORE              |
        |                                             |
        |  Application Service                        |
        |  - Register member                          |
        |  - Schedule class                           |
        |  - Book class                               |
        |  - Cancel booking                           |
        |  - List sessions                            |
        |                                             |
        |  DOMAIN                                     |
        |  - Entities and aggregates                 |
        |  - Value objects                            |
        |  - Domain services                          |
        +---------------------------------------------+
                             |
                             v
                    OUTPUT PORTS
             MemberRepositoryPort
          ClassSessionRepositoryPort
                             |
                             v
                 DRIVEN / OUTBOUND ADAPTER
             JPA Persistence Adapter / Spring Data
                             |
                             v
                         H2 Database
```

### Dependency rule

- The REST adapter depends on the input port, not on the concrete application service.
- The application service depends on output-port interfaces.
- The persistence adapters implement those output ports.
- The domain model has no dependency on Spring, JPA, HTTP or H2.
- Spring configuration acts as the **composition root**: it creates and wires the application service, domain service and adapters.

The term *port* describes a contract at the boundary of the core. An *adapter* translates between that contract and a technology or external protocol.

## 4. Project structure

```text
gymclass-ddd-hexagonal/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/gymclass/
    │   │   ├── domain/
    │   │   │   ├── model/
    │   │   │   │   ├── Member.java
    │   │   │   │   ├── ClassSession.java
    │   │   │   │   └── Booking.java
    │   │   │   ├── valueobject/
    │   │   │   └── service/
    │   │   │       └── BookingPolicy.java
    │   │   │
    │   │   ├── application/
    │   │   │   ├── port/
    │   │   │   │   ├── in/
    │   │   │   │   │   └── GymClassInputPort.java
    │   │   │   │   └── out/
    │   │   │   │       ├── MemberRepositoryPort.java
    │   │   │   │       └── ClassSessionRepositoryPort.java
    │   │   │   ├── service/
    │   │   │   │   └── GymClassApplicationService.java
    │   │   │   └── dto/
    │   │   │
    │   │   ├── adapter/
    │   │   │   ├── in/
    │   │   │   │   └── web/
    │   │   │   │       ├── GymClassController.java
    │   │   │   │       ├── ApiExceptionHandler.java
    │   │   │   │       └── dto/ApiRequests.java
    │   │   │   └── out/
    │   │   │       └── persistence/
    │   │   │           ├── MemberRepositoryAdapter.java
    │   │   │           ├── ClassSessionRepositoryAdapter.java
    │   │   │           ├── MemberJpaEntity.java
    │   │   │           ├── ClassSessionJpaEntity.java
    │   │   │           ├── BookingJpaEntity.java
    │   │   │           └── SpringData*Repository.java
    │   │   │
    │   │   ├── config/
    │   │   │   └── HexagonalConfiguration.java
    │   │   └── GymClassApplication.java
    │   └── resources/application.yml
    └── test/java/com/example/gymclass/domain/
        └── ClassSessionTest.java
```

## 5. Ports and adapters in this project

### 5.1 Input port (primary port)

`application/port/in/GymClassInputPort.java` declares the use cases offered by the application:

- `registerMember`
- `getMember`
- `scheduleClass`
- `listSessions`
- `bookClass`
- `cancelBooking`

The REST controller is a primary (driving) adapter. It receives HTTP requests, validates the transport payload and invokes the input port. It does not call the application implementation directly.

### 5.2 Application service

`application/service/GymClassApplicationService.java` implements `GymClassInputPort`. It coordinates the use cases, loads aggregates through output ports, invokes domain behavior and requests persistence. It does not contain the membership or booking invariants themselves.

### 5.3 Output ports (secondary ports)

- `MemberRepositoryPort`
- `ClassSessionRepositoryPort`

These interfaces describe what the application needs from persistence without prescribing a database or framework.

### 5.4 Output adapters

`MemberRepositoryAdapter` and `ClassSessionRepositoryAdapter` implement the output ports using Spring Data JPA. JPA entities are persistence models and are deliberately separate from domain entities. The adapters translate between persistence records and domain aggregates.

`SpringDataMemberRepository` and `SpringDataClassSessionRepository` are implementation details inside the persistence adapter. They are not ports exposed to the application core.

### 5.5 Composition root

`config/HexagonalConfiguration.java` creates the domain service and application service beans. This is where Spring wires the concrete adapters to the abstract ports.

The REST adapter owns the HTTP transaction boundary in this demonstration. If another driving adapter is added (for example, a message consumer or CLI), define an equivalent transaction boundary for that entry path.

## 6. DDD elements preserved

| DDD concept | Implementation |
|---|---|
| Bounded Context | Gym Class Management |
| Entities | `Member`, `ClassSession`, `Booking` |
| Value Objects | `MemberId`, `SessionId`, `BookingId`, `EmailAddress`, `ClassSchedule`, `MembershipPlan`, `MembershipStatus`, `ClassType` |
| Aggregates | `Member` and `ClassSession` |
| Aggregate root | `ClassSession` controls its `Booking` child entities |
| Domain service | `BookingPolicy` |
| Application service | `GymClassApplicationService` |
| Repository ports | `MemberRepositoryPort`, `ClassSessionRepositoryPort` |

`ClassSession` remains the aggregate root that controls booking and cancellation. There is no independent booking repository.

## 7. Requirements

- JDK 17 or later
- Maven 3.8+
- IntelliJ IDEA, Eclipse or Visual Studio Code (optional)
- Postman or curl (for API requests)

Check the installed versions:

```bash
java -version
mvn -version
```

## 8. Run the application

From the project root:

```bash
mvn clean test
mvn spring-boot:run
```

The REST API is available at `http://localhost:8080`.

H2 console: `http://localhost:8080/h2-console`

Connection settings:

```text
JDBC URL: jdbc:h2:mem:gymclassdb
User Name: sa
Password: (leave empty)
```

The database is in memory; its data is recreated when the application restarts.

## 9. REST API examples

### Register a member

`POST /api/members`

```json
{
  "fullName": "Alex Morgan",
  "email": "alex@example.com",
  "plan": "PREMIUM"
}
```

Plans: `BASIC`, `STANDARD`, `PREMIUM`.

### Retrieve a member

`GET /api/members/{memberId}`

### Schedule a class

`POST /api/sessions`

```json
{
  "title": "Morning Yoga",
  "type": "YOGA",
  "startsAt": "2027-12-15T09:00:00",
  "endsAt": "2027-12-15T10:00:00",
  "capacity": 12
}
```

Class types: `YOGA`, `PILATES`, `SPINNING`.

### List sessions

`GET /api/sessions`

### Book a class

`POST /api/sessions/{sessionId}/bookings/{memberId}`

### Cancel a booking

`DELETE /api/sessions/{sessionId}/bookings/{memberId}`

Business-rule violations return an HTTP 400 response. Missing members or sessions return HTTP 404.

## 10. Suggested exploration exercises

1. Trace a request from `GymClassController` through `GymClassInputPort` to `GymClassApplicationService`.
2. Identify the output port used when registering a member.
3. Trace how `MemberRepositoryAdapter` translates a `Member` into `MemberJpaEntity` and back.
4. Replace the persistence adapter with an in-memory fake implementation for a unit test. The application service should not need to know which adapter is being used.
5. Propose a second input adapter, such as a command-line interface or message consumer, that invokes the same input port without changing the domain.
6. Explain what would need to change to use PostgreSQL instead of H2.
7. Add a new use case and identify whether it requires a new input port operation, a new output port, or only existing contracts.

## 11. Testing

Run all tests with:

```bash
mvn test
```

The domain tests exercise aggregate invariants without starting Spring or connecting to H2. This is intentional: core business behavior should be testable independently from external technologies.

## 12. Educational comparison with the four-layer version

| Four-layer version | Hexagonal version |
|---|---|
| Presentation layer | Primary / driving adapter |
| Application layer | Application core and input/output ports |
| Domain layer | Domain model inside the core |
| Infrastructure layer | Secondary / driven adapters and configuration |
| Controller depends on application service | Controller depends on input port |
| Application uses repository abstractions | Application uses explicit output ports |
| Infrastructure implements repositories | Output adapters implement output ports |

The four-layer and hexagonal approaches are not mutually exclusive. Hexagonal architecture focuses on isolating the application core through ports and adapters; DDD focuses on modeling the business domain. They can be combined, as demonstrated here.

## 13. License and educational use

This project is intended as a teaching demonstration. It favors explicit names and Javadoc comments over production-level infrastructure concerns.
