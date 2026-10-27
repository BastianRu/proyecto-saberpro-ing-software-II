# Saber Pro Question Bank — JPA (Workshop 10)

Software Engineering Lab II — Universidad del Cauca — 2026-2

## Purpose

Layered Spring Boot application that persists the Saber Pro question bank with Spring Data JPA
and H2. **The domain model has no persistence annotations.** It defines technology-independent
repository contracts, and the infrastructure implements those contracts with JPA records and
mappers.

It covers:

- The question lifecycle: `DRAFT → PENDING_REVIEW → IN_REVIEW → APPROVED → PUBLISHED → ARCHIVED`.
  A question can also be rejected and sent back to `DRAFT`.
- The four relationship types and inheritance.
- Five use cases: create, query, classify, submit for review, and create a quiz using only
  published questions.

Design documents (in Spanish, for the report):

- [`docs/informe-parte1-demo.md`](docs/informe-parte1-demo.md): Part I, analysis of the teacher's
  demo (architecture table, answers, relational schema, execution evidence).
- [`docs/diseno-banco-preguntas.md`](docs/diseno-banco-preguntas.md): Parts II and III (UML class
  diagram, relational model, relationship and inheritance decisions, use cases, test matrix,
  reflection answers).

## Technology

Java 17+, Spring Boot 3.5, Spring Web, Spring Data JPA / Hibernate, H2 (in memory), Maven,
JUnit 5.

## Architecture

```text
co.edu.unicauca.questionbank
├── api/                    REST controllers, DTOs, GlobalExceptionHandler
├── application/            use cases (QuestionService, ReviewService, QuizService, CatalogService)
├── domain/
│   ├── model/              plain Java domain model (Question hierarchy, Quiz, User, ...)
│   ├── repository/         technology-independent repository contracts
│   └── exception/          business rule exceptions
├── infrastructure/jpa/
│   ├── record/             JPA entities (the only classes with JPA annotations)
│   ├── repository/         Spring Data interfaces
│   ├── mapper/             domain <-> record conversion
│   └── *RepositoryJpa      implementations of the domain contracts
└── config/                 sample data loader
```

Dependency rules:

- `api → application → domain ← infrastructure`.
- Application services depend only on the `domain.repository` interfaces, never on `JpaRepository`.

| Concept | Mapping |
|---|---|
| One-to-one | `QuestionRecord` ↔ `QuestionMetadataRecord`. `@MapsId` gives a shared primary key `question_metadata.question_id`. |
| One-to-many | `MultipleChoiceQuestionRecord.options` ↔ `AnswerOptionRecord.question`, FK `answer_options.question_id`. Uses cascade and orphan removal. |
| Many-to-one | `QuestionRecord.author` (`author_id`, mandatory), `QuestionRecord.topic` (`topic_id`), `QuizRecord.teacher`. |
| Many-to-many | `QuestionRecord.competencies` through the join table `question_competencies`. |
| Associative entity | `QuizQuestionRecord`: quiz ↔ question, with `position` and `points`. |
| Inheritance | `QuestionRecord` → `MultipleChoiceQuestionRecord`, `TrueFalseQuestionRecord`, using `JOINED`. |

## Run

```bash
cd questionbank-jpa
mvn clean test          # 37 domain, service, JPA and API tests
mvn spring-boot:run     # starts on http://localhost:8080 with sample data
```

H2 console: `http://localhost:8080/h2-console`. Connect with JDBC URL `jdbc:h2:mem:questionbank`,
user `sa`, and an empty password.

On startup, sample data is loaded through the use cases:

- Users: 1 `admin`, 2 `ana.perez` (AUTHOR), 3 `luis.gomez` (REVIEWER), 4 `maria.ruiz` (TEACHER),
  5 `juan.diaz` (STUDENT).
- 2 topics and 2 competencies.
- 4 questions: two published, one pending review, one draft.
- 1 quiz.

To start with an empty database, set `questionbank.sample-data.enabled=false`.

## API examples

```bash
# Create a multiple choice question (4 distractors + 1 correct answer)
curl -X POST localhost:8080/api/questions -H "Content-Type: application/json" -d '{
  "type": "MULTIPLE_CHOICE", "statement": "Which JPA strategy stores a hierarchy in one table?",
  "difficulty": "MEDIUM", "authorId": 2,
  "options": [{"text":"JOINED","correct":false},{"text":"SINGLE_TABLE","correct":true},
              {"text":"TABLE_PER_CLASS","correct":false},{"text":"EMBEDDED","correct":false},
              {"text":"MAPPED_SUPERCLASS","correct":false}],
  "metadata": {"cognitiveLevel":"REMEMBER","estimatedTimeSeconds":60}
}'

# Classify it and submit it for review (DRAFT -> PENDING_REVIEW)
curl -X PUT  localhost:8080/api/questions/5/classification -H "Content-Type: application/json" \
     -d '{"topicId":2,"competencyIds":[1,2]}'
curl -X POST localhost:8080/api/questions/5/submit

# Review: start, then approve or reject with a reason
curl -X POST localhost:8080/api/questions/5/start-review -H "Content-Type: application/json" -d '{"reviewerId":3}'
curl -X POST localhost:8080/api/questions/5/reject -H "Content-Type: application/json" \
     -d '{"reviewerId":3,"reason":"Ambiguous statement"}'
curl -X POST localhost:8080/api/questions/5/reopen          # REJECTED -> DRAFT

# Publish an approved question
curl -X POST localhost:8080/api/questions/{id}/publish

# Queries
curl localhost:8080/api/questions?status=PENDING_REVIEW
curl localhost:8080/api/questions?authorId=2
curl localhost:8080/api/questions?competencyId=1           # published questions of a competency

# Create a quiz (only PUBLISHED questions are accepted)
curl -X POST localhost:8080/api/quizzes -H "Content-Type: application/json" \
     -d '{"title":"Mock exam","teacherId":4,"questionIds":[1,2]}'
```

Error responses share a single JSON format (`status`, `error`, `message`, `details`):

| Status | Cause |
|---|---|
| 400 | Invalid content. `details` lists every broken rule. |
| 404 | Unknown resource. |
| 409 | Forbidden status transition, or a database constraint violation. |
| 422 | Other business rule, such as a quiz with an unpublished question or a wrong role. |
