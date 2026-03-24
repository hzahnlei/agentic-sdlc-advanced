# Refactoring Guidelines

- Never mix refactoring commits with feature or bug-fix commits — keeps history readable and reviews focused.
- Tests must pass before and after every step; if tests break, you changed behavior.
- Work in small, committed steps — each commit is one coherent, passing refactoring.
- Refactor before adding a feature, not during ([Fowler](https://martinfowler.com/articles/preparatory-refactoring-example.html): "make the change easy, then make the easy change").
- Boy scout rule: leave it cleaner than you found it, but stay within the scope of the current task.
- Rename relentlessly — renaming to reflect true intent is the highest-value, lowest-risk refactoring.
- Extract when code needs a comment — if a block needs a comment to explain what it does, extract it into a named function.
- Never speculate — only refactor to support a current, concrete need; YAGNI applies here too.
- For large structural changes: use the strangler fig pattern — incrementally replace old code; avoid big-bang rewrites.
