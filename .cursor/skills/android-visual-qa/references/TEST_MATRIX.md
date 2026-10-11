# Scenario-oriented Android QA matrix

## Critical smoke flows
- Login -> Home -> Filing -> Dataset -> Listing Detail -> Back -> Filing.
- Home -> Contacts -> Contact Detail -> Reminder creation -> Today.
- Property create -> save -> Property Detail -> edit -> back.
- Extract -> filters -> start -> progress -> dataset or explicit error.
- Search -> filter -> no results -> reset -> results.
- Team / Contracts / AI only in a feature slice that changes them.

## Verification distinctions
- Static code inspection: can flag likely issues, cannot attest pixel-level appearance.
- Gradle compile/lint: compilation and code checks, not user-journey correctness.
- Unit tests: business rules and ViewModel logic, not screenshot proof.
- Compose Preview: representative layout not true runtime/IME/permissions.
- Emulator/device screenshot + manual flow: strongest UI evidence.

## Critical performance
- Stable LazyColumn keys and preserved scroll state when filters update.
- Avoid per-item large gradients/blurs, nested LazyColumn in unbounded column.
- Recomposition only on necessary state changes; no frequent expensive formatting per frame.
