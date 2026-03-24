# UI Application Architecture

Applies to all services that include a frontend component.
Language-agnostic guidance is in the [General Coding Guidelines](general_coding.md).

## Overview

Four-layer architecture based on Martin Fowler's **Presentation Model** (2004), from which MVVM
evolved. Works universally across desktop (Eclipse RCP), mobile (iOS/Android), and web.
Core benefit: all UI behavior lives in plain objects testable without a UI framework.

```
Domain Model
     │  (feeds data into)
     ▼
Presentation Model    (formats data for display: strings, numbers, dates)
     │  (computes state for)
     ▼
View Model            (widget state: enabled, visible, validation status)
     │  (bidirectional data binding)
     ▼
View / Widget         (passive renderer — no logic, no state)
```

Reference: [Presentation Model — Martin Fowler](https://martinfowler.com/eaaDev/PresentationModel.html)

## Domain Model

- Pure business entities and operations — no UI imports, no formatting, no display state.
- Shared with non-UI layers (e.g., service/API layer).
- No UI dependency; independently unit-testable.

## Presentation Model

- One PM per screen or form.
- Holds display-ready data: formatted strings (dates, currency, units), filtered/sorted lists, computed labels.
- Values bind directly to display widgets — no formatting in the View.
- Derived from the Domain Model; no raw domain objects.
- Tested by asserting formatted output values.

## View Model

- Holds widget control state only — no data fields.
- Boolean flags such as `isLoginEnabled`, `isSpinnerVisible`, `hasUsernameError`.
- Computed/reactive properties: e.g., `isLoginEnabled` is `true` when all required PM fields are non-empty.
- Bound to widget properties (`enabled`, `visible`, `textColor`, `borderColor`).
- Tested by asserting widget states under different inputs — no UI framework needed.
- Reactive binding frameworks (e.g., Bond for iOS/Swift) suit this layer well.

## View / Widget

- Binds to PM fields (display data) and VM fields (widget state) via the framework's native binding mechanism.
- Forwards gestures (taps, text changes) to PM/VM as commands.
- Contains zero branching logic — all decisions belong in the PM or VM.
- Not unit-tested — no logic to assert.

## Data Binding

- Use **bidirectional binding** for input controls: user input updates PM/VM fields; PM/VM changes update the widget.
- Use **unidirectional binding** for display controls: PM/VM state drives widget properties only.
- Use the framework's native reactive mechanism: observers, reactive properties, property-change listeners, or signals.
- Keep binding declarations in the View; never let PM or VM reference a widget directly.

## Testability

- PM and VM are plain objects — test with plain unit tests; no UI harness, headless browser, or instrumentation.
- Test all display states (PM), all widget states (VM), and all commands/interactions.
- View has no logic — no unit test needed.
- Use architecture tests (e.g., ArchUnit for Java, go-arch-lint for Go) to enforce layer dependency rules:
  - View must not be imported by PM or VM.
  - PM must not import UI framework types.
  - VM must not contain data fields.

## Anti-patterns to Avoid

- Logic in View event handlers — move it to the PM or VM.
- Domain Model used as PM directly — always add the formatting layer.
- Mixing data and widget-state fields in one object — keep PM and VM separate.
- One monolithic VM for an entire screen — split into focused VMs per section.
- Circular layer dependencies.
- PM or VM importing UI framework classes (e.g., `UIButton`, `JButton`, `HTMLElement`).
