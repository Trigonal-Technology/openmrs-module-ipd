# IPD REST API (v1)

**Base URL:** `http://localhost/openmrs/ws/rest/v1`  
**Module:** `bahmni-ipd`  
**Version:** `1.2.0-nidan-SNAPSHOT`  
**Last updated:** 2026-04-15  

All paths below are relative to the base URL unless noted.

---

## Authentication

Use HTTP Basic Auth (same as standard OpenMRS REST).

**Example header:** `Authorization: Basic <base64(username:password)>`

---

## Conventions

- **JSON** request bodies use `Content-Type: application/json`.
- **UUIDs** in path parameters are validated where the controller uses `@Pattern` (lowercase hex, 8-4-4-4-12).
- **Errors** are often returned as `{ "error": "message" }` (see [Error handling](#error-handling)).

---

## 1. Task templates

**Base path:** `/ipd/task-templates`

### 1.1 List task templates

`GET /ipd/task-templates`

**Privilege:** `Get Task Templates`

**Query parameters**

| Parameter  | Type   | Required | Description |
|------------|--------|----------|-------------|
| `wardUuid` | string | No       | If set, filter by ward (location) UUID. If omitted, active templates are listed. |

**Response `200`:** `{ "results": [ TaskTemplateResponse, ... ] }`

`TaskTemplateResponse` includes: `uuid`, `name`, `description`, `taskType` `{ uuid, display }`, `ward` (optional), `priority`, `estimatedDurationMinutes`, `defaultAssigneeRole` (optional), `active`, `recurrence` (optional; see below).

**Recurrence object (response):** `type`, `interval`, `startDate`, `endDate`, `activeTimes` (string array), `daysOfWeek` (integer array).

### 1.2 Create task template

`POST /ipd/task-templates`

**Privilege:** `Manage Task Templates`

**Request body** (`TaskTemplateRequest`)

| Field                      | Type    | Required | Notes |
|----------------------------|---------|----------|-------|
| `name`                     | string  | Yes      | Max 100; pattern allows alphanumerics, spaces, `-_.,()[]` |
| `description`              | string  | No       | Max 500 |
| `taskTypeUuid`             | string  | Yes*     | Concept UUID |
| `priority`                 | enum    | Yes      | `ROUTINE`, `URGENT`, `STAT` (maps to `Task.TaskPriority`) |
| `wardUuid`                 | string  | No       | Location UUID |
| `active`                   | boolean | No       | Default `true` |
| `defaultAssigneeRoleUuid`  | string  | No       | Concept UUID for default assignee role |
| `estimatedDurationMinutes` | integer | No       | |
| `recurrence`               | object  | No       | See below |

\*`taskTypeUuid` is required for a valid create; unknown concept → `400` with `Invalid taskTypeUuid`.

**Recurrence object (request):** `RecurrenceRequest`

| Field          | Type      | Description |
|----------------|-----------|-------------|
| `type`         | string    | Must match `RecurrenceType` enum name (e.g. `DAILY`) |
| `interval`     | integer   | Default 1 |
| `startDate`    | string    | ISO local datetime string |
| `endDate`      | string    | Optional |
| `activeTimes`  | string[]  | e.g. `["10:00","14:00"]` (stored comma-separated) |
| `daysOfWeek`   | integer[] | Optional |

**Response:** `200 OK` with a single `TaskTemplateResponse` (not `201`).

### 1.3 Get task template by UUID

`GET /ipd/task-templates/{templateUuid}`

**Privilege:** `Get Task Templates`

**Response:** `200` — one `TaskTemplateResponse`; `404` — not found; `400` — invalid UUID format.

### 1.4 Update template active flag

`PATCH /ipd/task-templates/{templateUuid}`

**Privilege:** `Manage Task Templates`

**Body:** JSON map with `active` (boolean) required.

**Response:** `200` with updated `TaskTemplateResponse`.

### 1.5 Void task template

`DELETE /ipd/task-templates/{templateUuid}?reason=...`

**Privilege:** `Manage Task Templates`

**Query:** `reason` optional (5–255 chars if provided); default server-side: `"Voided by user"`.

**Response:** `200` — `{ "message": "Task template voided successfully" }`.

### 1.6 Apply template to patient

`POST /ipd/task-templates/{templateUuid}/apply`

**Privilege:** `Apply Task Templates`

**Body** (`ApplyTemplateRequest`)

| Field         | Type   | Required | Description |
|---------------|--------|----------|-------------|
| `patientUuid` | string | Yes      | Patient UUID |
| `wardUuid`    | string | Yes      | Ward/location UUID |
| `startDate`   | string | No       | `yyyy-MM-ddTHH:mm:ss` (local); default now |
| `endDate`     | string | No       | Same format |

**Response `200`:**

```json
{
  "message": "Template applied successfully",
  "generatedTasks": 0,
  "patientTaskTemplateUuid": "...",
  "patientUuid": "...",
  "wardUuid": "..."
}
```

`generatedTasks` is the count from immediate generation for a short window after apply.

---

## 2. IPD tasks (nursing / non-medication tasks)

**Base path:** `/ipd/tasks`  
These endpoints work with the IPD `Task` model (not the old `/ipd/task-instances` path, which is not present in this module version).

### 2.1 List tasks

`GET /ipd/tasks?patient={patientUuid}&status={statuses}`

**Privilege:** `Get Tasks`

| Parameter | Required | Description |
|-----------|----------|-------------|
| `patient` | Yes      | Patient UUID |
| `status`  | No       | Comma-separated `TaskStatus` values |

**`TaskStatus` values:** `REQUESTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

**Response `200`:** `{ "results": [ TaskResponse, ... ] }`

**`TaskResponse` fields:** `uuid`, `name`, `description`, `intent` (`order` / `proposal`), `status`, `patient` `{ uuid, display }`, `taskType` (optional) `{ uuid, display }`, `executionPeriod` (optional) `{ start, end }` (ISO-8601 instants as strings).

### 2.2 Create task

`POST /ipd/tasks`

**Privilege:** `Add Tasks`

**Body** (`TaskRequest`) — server checks non-empty `name`, `patient.uuid`, and `status`.

| Field              | Type   | Notes |
|--------------------|--------|-------|
| `name`             | string | Required |
| `description`      | string | |
| `intent`           | string | `ORDER` or `PROPOSAL` (default `ORDER`) |
| `status`           | string | Required; one of `TaskStatus` |
| `priority`         | string | `ROUTINE`, `URGENT`, `STAT` (default `ROUTINE`) |
| `notes`            | string | |
| `patient`          | object | `{ "uuid": "..." }` required |
| `taskType`         | object | `{ "uuid": "..." }` optional concept |
| `executionPeriod`  | object | `{ "start", "end" }` ISO-8601 instant strings |

**Response:** `200` with `TaskResponse`.

**Status transitions** (on update) are restricted: e.g. `REQUESTED` → `IN_PROGRESS` or `CANCELLED`; `IN_PROGRESS` → `COMPLETED` or `CANCELLED`.

### 2.3 Update task

`POST /ipd/tasks/{taskUuid}`

**Privilege:** `Edit Tasks`

**Body** (`TaskUpdateRequest`)

| Field              | Type   | Description |
|--------------------|--------|---------------|
| `status`           | string | New status (validated transition) |
| `notes`            | string | |
| `executionPeriod`  | object | `start` / `end` instants |

**Response:** `200` with `TaskResponse`; `404` if task not found.

There is **no** `GET /ipd/tasks/{taskUuid}` in the current controller; use list-by-patient or your own storage of UUIDs from create/list responses.

---

## 3. Task acknowledgment (doctor / provider)

**Base path:** `/ipd/tasks` (same as §2; implemented in a separate controller)

### 3.1 Acknowledge completed task

`POST /ipd/tasks/{taskUuid}/acknowledge`

**Body** (`AcknowledgeTaskRequest`)

| Field                   | Type   | Required | Notes |
|-------------------------|--------|----------|-------|
| `acknowledgmentMethod`  | string | Yes      | `QR_SCAN`, `NFC_TAP`, `MANUAL_ENTRY`, `BIOMETRIC` (case-insensitive) |
| `deviceId`              | string | No       | Max 50 |
| `notes`                 | string | No       | Max 500 |

**Behavior:** Task must be `COMPLETED`; only one acknowledgment per task; current user must resolve to a **Provider** or the call returns `403` with a plain-text body.

**Success `200` (example):**

```json
{
  "uuid": "...",
  "taskUuid": "...",
  "acknowledgedBy": "...",
  "acknowledgmentTime": "...",
  "status": "Acknowledged"
}
```

**Other statuses:** `404` task not found; `400` not completed; `409` already acknowledged; `500` on server error (plain text).

*Note: `TaskAcknowledgmentController` does not explicitly check the `Acknowledge Tasks` privilege; align server configuration if you need strict RBAC.*

### 3.2 Get acknowledgment for a task

`GET /ipd/tasks/{taskUuid}/acknowledgment`

**Response `200`:** `uuid`, `acknowledgedBy`, `acknowledgmentTime`, `notes`, `billable`.  
**`404`:** no acknowledgment.

---

## 4. Wards

**Base path:** `/ipd/wards`

| Method | Path | Description |
|--------|------|-------------|
| `GET`  | `/{wardUuid}/summary?providerUuid=` | Ward summary for dashboard (returns `IPDWardPatientSummaryResponse`) |
| `GET`  | `/{wardUuid}/patients?offset=&limit=&sortBy=` | Paginated patients in ward |
| `GET`  | `/{wardUuid}/myPatients?providerUuid=&offset=&limit=&sortBy=` | Patients for a provider in ward |
| `GET`  | `/{wardUuid}/patients/search?offset=&limit=&searchKeys=&searchValue=&sortBy=` | Search in ward (`searchKeys` is repeatable / list param) |

---

## 5. Medication schedule

**Base path:** `/ipd/schedule`

| Method | Path | Privileges (typical) |
|--------|------|------------------------|
| `POST` | `/type/medication` | `Edit Medication Tasks` — create schedule |
| `POST` | `/type/medication/edit` | `Edit Medication Tasks` — update schedule |
| `GET`  | `/type/medication?patientUuid=&startTime=&endTime=&visitUuid=&view=` | `Get Medication Administration` **and** `Get Medication Tasks` — `startTime` / `endTime` are **epoch milliseconds** (UTC) converted server-side |
| `GET`  | `/type/medication?patientUuid=&serviceType=&orderUuids=` | Same pair of privileges; optional filters |
| `GET`  | `/type/medication/patientsMedicationSummary?patientUuids=&startTime=&endTime=&includePreviousSlot=&includeSlotDuration=` | Multi-patient summary; epoch millis for time range |

Request/response types: `ScheduleMedicationRequest`, `ScheduleMedicationResponse`, `MedicationSlotResponse`, `MedicationScheduleResponse`, `PatientMedicationSummaryResponse` (see `omod/.../contract/`).

---

## 6. Visit – medications and treatments

**Base path:** `/ipdVisit/{visitUuid}`

`GET /ipdVisit/{visitUuid}/medication?includes=...`

**Privileges:** `Get Medication Administration` **and** `Get Medication Tasks`

**Query:** `includes` may include `emergencyMedications` to add emergency med slots to the payload.

**Response:** `IPDTreatmentsResponse` (prescribed orders + optional emergency list).

---

## 7. Medication administration (scheduled / ad-hoc)

**Base path:** `/ipd`

| Method | Path | Privilege |
|--------|------|-----------|
| `POST` | `/scheduledMedicationAdministrations` | `Edit Medication Administration` — body: **array** of `MedicationAdministrationRequest` |
| `POST` | `/adhocMedicationAdministrations` | `Edit adhoc medication tasks` — single `MedicationAdministrationRequest` |
| `PUT`  | `/adhocMedicationAdministrations/{medicationAdministrationUuid}` | Update ad-hoc (no privilege check in controller — rely on OpenMRS if configured) |

---

## 8. Emergency medications to acknowledge

`GET /ipd/emergencyMedicationsToAcknowledge?providerUuid=&locationUuid=`

**Aliases:** `provider_uuid`, `location_uuid` (snake_case) are accepted.

**Privilege:** `Get Medication Administration`

**Required:** `providerUuid` (or `provider_uuid`).

**Response `200`:** JSON array of `EmergencyMedicationToAcknowledgeRest`:

- `identifier`, `name`, `gender`, `patient_uuid`, `date_of_birth` (epoch ms)
- `medication_administration_uuid`, `administered_date_time` (epoch ms)
- `administered_drug_name`, `administered_dose`, `administered_dose_units`, `administered_route`
- `performer`: `{ medication_administration_performer_uuid, provider_uuid, display }` (witness / performer details)
- `visit_uuid`

---

## 9. Care team

`POST /ipd/careteam/participants`

**Body** (`CareTeamRequest`): `patientUuid`, `careTeamParticipantsRequest` (list — see `CareTeamParticipantRequest` in code).

**Response:** `CareTeamResponse` — `200` on success.

---

## 10. FHIR (optional)

R5 **CareTeam** search is exposed via the FHIR2 module: provider `CareTeamFhirResourceProvider` in the `api` module. Use your platform’s standard FHIR base URL, not the `/ws/rest/v1` prefix.

---

## Security notes

- Task template and acknowledgment request DTOs use **HTML escaping** on selected fields where implemented (`sanitize()`).
- **UUIDs** for templates use regex validation on several routes.
- Do not rely on undocumented filters (e.g. “PathTraversalFilter”) without verifying your OpenMRS distribution.

---

## Error handling

Common shapes:

- `{ "error": "message" }` — many IPD REST controllers
- `RestUtil.wrapErrorResponse` — some medication/ward routes (OpenMRS-style wrapper)

| HTTP | Meaning |
|------|---------|
| 200  | OK (including some creates that return 200) |
| 400  | Validation / bad input |
| 401  | Unauthenticated |
| 403  | Missing privilege (when checked) or forbidden (e.g. not a provider) |
| 404  | Not found |
| 409  | Conflict (e.g. duplicate acknowledgment) |
| 500  | Server error |

---

## Privilege quick reference

| Privilege | Used for (summary) |
|-----------|--------------------|
| `Get Task Templates` / `Manage Task Templates` / `Apply Task Templates` | §1 |
| `Get Tasks` / `Add Tasks` / `Edit Tasks` | §2 |
| `Get Medication Administration` / `Edit Medication Administration` | Med admin, emergency list |
| `Get Medication Tasks` / `Edit Medication Tasks` / `Delete Medication Tasks` | Schedules, slots |
| `Edit adhoc medication tasks` | Ad-hoc med admin |

Other constants exist in `PrivilegeConstants` (e.g. `Acknowledge Tasks`, `Get Task Instances`, `Complete Tasks`) for roles and future use; not all are enforced on every controller method—verify in code if you need strict guarantees.

---

## cURL quick reference

```bash
# List task templates
curl -s -u admin:Admin123 \
  "http://localhost/openmrs/ws/rest/v1/ipd/task-templates"

# List patient tasks
curl -s -u admin:Admin123 \
  "http://localhost/openmrs/ws/rest/v1/ipd/tasks?patient=PATIENT_UUID&status=REQUESTED,IN_PROGRESS"

# Apply template
curl -s -u admin:Admin123 -H "Content-Type: application/json" \
  -d '{"patientUuid":"...","wardUuid":"..."}' \
  "http://localhost/openmrs/ws/rest/v1/ipd/task-templates/TEMPLATE_UUID/apply"
```

For authoritative behavior, refer to the Spring controllers under `omod/src/main/java/org/openmrs/module/ipd/web/controller/`.
