# Domain Model

The domain model shows entities, value objects, aggregates, and their relationships.
It is the authoritative source for class structure in the domain layer.

<!--
For the AI coding assistant:
- Entities have identity (id field). Generate them as classes with equals/hashCode on id only.
- Value objects have no identity. Generate them as immutable records/data classes.
- Aggregate roots are marked <<AR>>. Only access aggregates through their root.
- Relationships show cardinality. A 1-to-many owned collection lives inside the aggregate.
- Use exact class names from this diagram and specs/glossary.md.
-->

## Class Diagram

```plantuml
@startuml
!theme plain
skinparam classAttributeIconSize 0
skinparam classFontSize 13
skinparam packageStyle rectangle
hide empty members

package "TODO: Aggregate / Package Name" {

  ' Aggregate Root — owns the lifecycle of the aggregate
  class "TODO: AggregateRoot" <<AR>> {
    +id: TODO: IdType
    +status: TODO: StatusEnum
    +createdAt: Instant
    --
    +TODO: businessMethod(): void
    +TODO: anotherMethod(param: Type): Result
  }

  ' Entity inside aggregate — has identity but only accessible via root
  class "TODO: ChildEntity" {
    +id: TODO: ChildId
    +TODO: field: Type
  }

  ' Value Object — immutable, no identity
  class "TODO: ValueObject" <<VO>> {
    +TODO: field: Type
    +TODO: otherField: Type
  }

  ' Enum
  enum "TODO: StatusEnum" {
    TODO_STATUS_A
    TODO_STATUS_B
    TODO_STATUS_C
  }

  "TODO: AggregateRoot" *-- "0..*" "TODO: ChildEntity" : contains >
  "TODO: AggregateRoot" *-- "1" "TODO: ValueObject" : has >
  "TODO: AggregateRoot" --> "TODO: StatusEnum"
}

package "TODO: Second Aggregate / Package Name" {

  class "TODO: OtherRoot" <<AR>> {
    +id: TODO: OtherId
  }

  ' Reference by ID only — never a direct object reference across aggregate boundaries
  "TODO: AggregateRoot" ..> "TODO: OtherRoot" : ref by id >
}

@enduml
```

## Aggregate Boundaries

Aggregates define transactional consistency boundaries.
Never load or modify two aggregates in the same transaction (except via sagas/events).

| Aggregate Root | Owns                              | References by ID only |
| -------------- | --------------------------------- | --------------------- |
| TODO: RootName | TODO: ChildEntity, TODO: ValueObj | TODO: OtherRoot       |

## State Transitions

<!-- Add a state diagram per aggregate that has a lifecycle status. -->

```plantuml
@startuml
!theme plain

[*] --> TODO_INITIAL : created

TODO_INITIAL --> TODO_NEXT_STATE : TODO: trigger event / command
TODO_NEXT_STATE --> TODO_TERMINAL : TODO: trigger
TODO_INITIAL --> TODO_CANCELLED : cancelled
TODO_NEXT_STATE --> TODO_CANCELLED : cancelled

TODO_TERMINAL --> [*]
TODO_CANCELLED --> [*]

note right of TODO_CANCELLED : terminal state\ncannot be undone
@enduml
```
