# User Experience (UX)

Sections apply only where the artifact exposes that interface.

## General Advice

- Minimise steps to frequent tasks; optimise for the common case.
- Be consistent: same action → same result; same label → same meaning.
- Acknowledge every user action within 100 ms ([Nielsen Norman Group](https://www.nngroup.com/articles/response-times-3-important-limits/)).
- Progressive disclosure: show only what is needed now.
- Safe, sensible defaults; power users override via flags or settings.
- Never discard user data without explicit confirmation.
- Error messages state the cause and suggest a fix; never blame the user.
- Accessibility is not optional: keyboard, screen reader, and color-blind support from the start.

## Command Line Tools

- Require as few mandatory arguments as possible; safe defaults cover the rest.
- Color only on TTY; honor `NO_COLOR` ([no-color.org](https://no-color.org)) and `--no-color`.
- Show progress for operations > 2 s.
- Confirm destructive operations interactively; `--yes` / `--force` skips the prompt.
- Include at least one example in `--help`.
- Support `--output json` for machine-readable output alongside the human-readable default.
- On partial success, report what succeeded and what failed; exit non-zero.

## Text Based User Interface (TUI)

- Show active key bindings in a persistent status bar.
- `q`, `Ctrl+C`, `Esc` quit or cancel in every view.
- `j`/`k` navigate lists; `Enter` confirms; `Backspace`/`u` goes back.
- Handle terminal resize gracefully.
- Keyboard must suffice; mouse is optional and additive.
- Use color and bold sparingly; readable on dark and light themes.
- Long operations are async with a progress indicator; UI stays responsive.

## Graphical User Interface (GUI)

### Native

#### Personal Computers and Notebooks

- Follow the platform HIG (Apple HIG, Windows App Design, GNOME HIG).
- Keyboard shortcuts for all frequent actions; document in menus or tooltips.
- Long operations run in background with a progress bar and cancel option.
- Undo/redo for all state-modifying actions.
- Adapt to any window size; no fixed pixel dimensions.
- Persist window size, position, and settings across sessions.
- Drag-and-drop where intuitive (open files, reorder items).

#### Mobile Devices and Tablets

- Touch targets ≥ 44 pt (iOS) / 48 dp (Android).
- Primary actions reachable with one thumb (bottom half of screen).
- Support portrait and landscape without data loss.
- Use platform-native navigation (back gesture, tab bar, bottom sheet).
- Prefer pickers, toggles, and steppers over text entry.
- Haptic feedback for confirmatory or irreversible actions.

### Web

- Mobile-first, responsive; no horizontal scroll at 320 px.
- Core functionality works without JavaScript.
- WCAG 2.1 AA: contrast, keyboard navigation, screen-reader labels.
- Visible focus indicators on all interactive elements.
- CLS < 0.1 after initial render ([Core Web Vitals](https://web.dev/articles/cls)).
- Inline validation with field-level errors; never use placeholder as label.
- Optimistic UI or spinner for async actions.
