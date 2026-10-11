# Design component contracts

## Surface/Card
- `DfCard` defaults MUST resolve current theme; force `Color.White` only for intentional image/overlay styling.
- No compound use of `shadow` + `tonalElevation` + prominent border on every single list item.
- Card click vs nested icon actions must each have distinct semantics and tap area.

## Scaffold + insets
- `MainActivity.enableEdgeToEdge()` active. Preserve status/navigation bar avoidance exactly once. Do not stack `statusBarsPadding` twice.
- The global `NavHost` provides bottom-nav padding for root tabs; child sticky footer requires IME/navigation insets, not guessed constants.
- Background should not be painted twice by `DfScreenScaffold` and `DfPullRefresh` if costly/visually noisy.

## States matrix
| State | Visible UI | Action |
| --- | --- | --- |
| Initial loading | skeleton aligned to eventual content | none or cancel when meaningful |
| Refreshing | content retained + small progress | avoid duplicate request |
| Empty | explanation based on entity | add/import/extract where appropriate |
| No results | clear filters / search query | reset |
| Network error | retry + non-destructive wording | retry |
| Locked/License | explanation of entitlement | plan/upgrade route only if exists |
| Success | concise feedback without erasing context | navigate or continue |

## QA snapshots
- 360×800dp portrait, typical 393×852dp, 600dp+ tablet where supported.
- Dark and light, 1.0× and 1.3× font scale, keyboard visible, long Persian data.
- No mandatory screenshot automation in build unless dependencies/toolchain already support it.
