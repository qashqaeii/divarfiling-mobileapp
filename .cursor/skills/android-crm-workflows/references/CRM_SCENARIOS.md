# CRM acceptance scenarios

| Area | Scenario | UX acceptance |
| --- | --- | --- |
| Contacts | no results after filter | show reset, keep search visible |
| Contacts | empty CRM | entrypoint add lead, no fabricated data |
| Contacts | phone unavailable | disable/hide call, don't render fake number |
| Contact detail | changing customer type | preserve dependent fields; correct validation |
| Contact detail | match suggestions | explain source and context of results |
| Properties | create many fields | readable grouping and form state preserved |
| Properties | photos/maps | permissions, URI persistence, loading, retry |
| Property detail | documents/amenities | label and meaning; horizontal overflow absent |
| Deals | next action / stage | show stage, date and status prominently |
| Contracts | signing/PDF availability | truthfully show pending/error/available |
| Today | overdue and completed | distinct text+icons, recoverable filter |
| Calendar | Jalali + recurrence | timezone and reminder semantics unchanged |
| Team CRM | copied/shared data | role/privacy/ownership never relaxed |

Never implement a sample action by fake state or placeholder toast in production. If no API exists, report gap; avoid inventing a network endpoint.
