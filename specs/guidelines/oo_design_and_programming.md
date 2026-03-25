# Object-Oriented Design and Programming Guidelines

Applies to all projects in OOP languages (C++, Java) and to Go projects.

## Class Design

- Explicitly mark methods that implement or override a supertype definition (`override` in C++, `@Override` in Java).
- Prefer composition over inheritance; keep hierarchies shallow.
- Leaf classes must not be extensible (`final` in Java, `final` in C++); non-leaf classes must be abstract.
- Never instantiate non-leaf classes directly.
- One primary responsibility per class — one reason to change.
- Utility classes — classes that contain only static members (constants, static methods, nested types) — must declare a private no-arg constructor to prevent instantiation. In C++, prefer a namespace over a utility class.

## SOLID Principles

- **S** — Single Responsibility: a class has exactly one reason to change.
- **O** — Open/Closed: open for extension, closed for modification — add behaviour without touching existing code.
- **L** — Liskov Substitution: a subtype must honour every contract of its supertype; callers must not need to know the concrete type.
- **I** — Interface Segregation: prefer narrow, focused interfaces over monolithic ones; callers depend only on the methods they use.
- **D** — Dependency Inversion: depend on abstractions, not concretions; inject dependencies rather than constructing them internally.

## Design Patterns

- Apply GoF patterns where they solve a proven, recurring design problem — name the pattern in a code comment or doc comment.
- Prefer patterns that reduce coupling: Strategy, Observer, Decorator, Factory Method, Abstract Factory.
- A simple class with a clear name beats a contrived pattern applied for its own sake.
- Document pattern intent: state _which_ pattern is applied and _why_, not _how_ it works.

## Coupling and Cohesion

- High cohesion within a class; low coupling between classes.
- Law of Demeter: talk to direct collaborators only — avoid chaining through intermediary objects (`a.getB().getC().doX()` is a smell).
- Tell, don't ask: send commands to objects rather than interrogating their state to make decisions on their behalf.

## References

- _Design Patterns: Elements of Reusable Object-Oriented Software_ — Gamma, Helm, Johnson, Vlissides, 1994 (GoF)
- _Effective Java_ — Joshua Bloch, 3rd ed., 2018
- _Clean Code_ — Robert C. Martin, 2008
- _Object-Oriented Software Construction_ — Bertrand Meyer, 2nd ed., 1997
- _A Behavioral Notion of Subtyping_ — Liskov & Wing, ACM TOPLAS 16(6), 1994 (formal LSP foundation)
- _Design Principles and Design Patterns_ — Robert C. Martin, 2000 (origin of the SOLID acronym)
