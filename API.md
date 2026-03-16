# IPD Module REST API Reference

The IPD (Inpatient Department) module provides REST APIs for ward management, medication administration, scheduling, care teams, and visit-based treatments.

**Base URL:** `{openmrs_base_url}/ws/rest/v1`  
Example: `http://localhost:8080/openmrs/ws/rest/v1`

**Authentication:** Session-based or Basic Auth (OpenMRS standard)

---

## Table of Contents

1. [Medication Administration](#1-medication-administration)
2. [Wards](#2-wards)
3. [Visit Medications](#3-visit-medications)
4. [Schedule](#4-schedule)
5. [Care Team](#5-care-team)

---

## 1. Medication Administration

Base path: `/ipd`

### 1.1 Create Scheduled Medication Administrations

Creates multiple scheduled medication administration records.

**Endpoint:** `POST /ipd/scheduledMedicationAdministrations`

**Required privilege:** `Edit Medication Administration`

**Request body:** `List<MedicationAdministrationRequest>`

| Field | Type | Description |
|-------|------|-------------|
| uuid | string | (optional) UUID for update |
| patientUuid | string | Patient UUID |
| encounterUuid | string | Encounter UUID |
| orderUuid | string | Drug order UUID |
| providers | array | List of performer objects |
| notes | array | List of note objects |
| status | string | e.g. COMPLETED, INPROGRESS |
| statusReason | string | Reason for status |
| drugUuid | string | Drug concept UUID |
| dosingInstructions | string | Free-text instructions |
| dose | number | Dose amount |
| doseUnits | string | Dose units concept |
| route | string | Route concept |
| site | string | Administration site |
| administeredDateTime | long | Unix timestamp (seconds) |
| slotUuid | string | Associated slot UUID |

** providers item:**
| Field | Type |
|-------|------|
| uuid | string |
| providerUuid | string |
| function | string |

** notes item:**
| Field | Type |
|-------|------|
| uuid | string |
| authorUuid | string |
| recordedTime | long |
| text | string |

**Response:** `200 OK` - `List<MedicationAdministrationResponse>`

```json
[
  {
    "uuid": "abc-123-def",
    "patientUuid": "patient-uuid",
    "encounterUuid": "encounter-uuid",
    "orderUuid": "order-uuid",
    "providers": [{"uuid": "...", "provider": {...}, "function": "performer"}],
    "notes": [{"uuid": "...", "author": {...}, "recordedTime": "...", "text": "..."}],
    "status": "COMPLETED",
    "statusReason": null,
    "drug": {"uuid": "...", "display": "Paracetamol 500mg"},
    "dosingInstructions": "Take with food",
    "dose": 500.0,
    "doseUnits": {"uuid": "...", "display": "mg"},
    "route": {"uuid": "...", "display": "Oral"},
    "site": null,
    "administeredDateTime": "2024-03-16T10:30:00.000+0000"
  }
]
```

**Example request:**
```bash
curl -X POST "http://localhost:8080/openmrs/ws/rest/v1/ipd/scheduledMedicationAdministrations" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '[
    {
      "patientUuid": "86526ed5-3c4b-4a3b-9a2b-1c2d3e4f5a6b",
      "encounterUuid": "enc-uuid-here",
      "orderUuid": "order-uuid-here",
      "providers": [{"providerUuid": "provider-uuid", "function": "performer"}],
      "notes": [],
      "status": "COMPLETED",
      "drugUuid": "drug-concept-uuid",
      "dose": 500.0,
      "doseUnits": "mg",
      "route": "oral",
      "administeredDateTime": 1710580200,
      "slotUuid": "slot-uuid"
    }
  ]'
```

---

### 1.2 Create Adhoc Medication Administration

Creates a single adhoc (one-off) medication administration.

**Endpoint:** `POST /ipd/adhocMedicationAdministrations`

**Required privilege:** `Edit adhoc medication tasks`

**Request body:** `MedicationAdministrationRequest` (single object, same structure as above)

**Response:** `200 OK` - `MedicationAdministrationResponse`

**Example request:**
```bash
curl -X POST "http://localhost:8080/openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '{
    "patientUuid": "86526ed5-3c4b-4a3b-9a2b-1c2d3e4f5a6b",
    "encounterUuid": "enc-uuid",
    "orderUuid": "order-uuid",
    "providers": [{"providerUuid": "provider-uuid", "function": "performer"}],
    "notes": [],
    "status": "COMPLETED",
    "drugUuid": "drug-uuid",
    "dose": 10.0,
    "doseUnits": "mg",
    "route": "oral",
    "administeredDateTime": 1710580200
  }'
```

---

### 1.3 Update Adhoc Medication Administration

Updates an existing adhoc medication administration.

**Endpoint:** `PUT /ipd/adhocMedicationAdministrations/{medicationAdministrationUuid}`

**Path parameter:** `medicationAdministrationUuid` - UUID of the medication administration

**Request body:** `MedicationAdministrationRequest` (same structure as create)

**Response:** `200 OK` - `MedicationAdministrationResponse`

**Example request:**
```bash
curl -X PUT "http://localhost:8080/openmrs/ws/rest/v1/ipd/adhocMedicationAdministrations/ma-uuid-here" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '{
    "uuid": "ma-uuid-here",
    "patientUuid": "patient-uuid",
    "status": "ENTEREDINERROR",
    "notes": [{"text": "Administered in error"}]
  }'
```

---

## 2. Wards

Base path: `/ipd/wards`

### 2.1 Get Ward Summary

Returns patient statistics for a ward, optionally filtered by provider.

**Endpoint:** `GET /ipd/wards/{wardUuid}/summary`

**Path parameter:** `wardUuid` - Ward/location UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| providerUuid | string | Yes | Provider UUID to filter "my patients" count |

**Response:** `200 OK` - `IPDWardPatientSummaryResponse`

```json
{
  "totalPatients": 25,
  "totalProviderPatients": 8
}
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/wards/ward-location-uuid/summary?providerUuid=provider-uuid"
```

---

### 2.2 Get Ward Patients

Returns paginated list of patients in a ward.

**Endpoint:** `GET /ipd/wards/{wardUuid}/patients`

**Path parameter:** `wardUuid` - Ward/location UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| offset | integer | Yes | Pagination offset |
| limit | integer | Yes | Page size |
| sortBy | string | No | Sort field |

**Response:** `200 OK` - `IPDPatientDetailsResponse`

```json
{
  "admittedPatients": [
    {
      "patientDetails": {"uuid": "...", "display": "John Doe", ...},
      "bedDetails": {"uuid": "...", "display": "Bed 101"},
      "visitDetails": {"uuid": "...", "display": "..."},
      "newTreatments": 2,
      "careTeam": {
        "uuid": "...",
        "patientUuid": "...",
        "participants": [{"uuid": "...", "provider": {...}, "startTime": 1710580200000, "endTime": null, "voided": false}]
      }
    }
  ],
  "totalPatients": 25
}
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/wards/ward-uuid/patients?offset=0&limit=10&sortBy=bedNumber"
```

---

### 2.3 Get Provider's Patients in Ward

Returns paginated list of patients assigned to a specific provider in a ward.

**Endpoint:** `GET /ipd/wards/{wardUuid}/myPatients`

**Path parameter:** `wardUuid` - Ward/location UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| providerUuid | string | Yes | Provider UUID |
| offset | integer | Yes | Pagination offset |
| limit | integer | Yes | Page size |
| sortBy | string | No | Sort field |

**Response:** `200 OK` - `IPDPatientDetailsResponse` (same structure as 2.2)

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/wards/ward-uuid/myPatients?providerUuid=provider-uuid&offset=0&limit=10"
```

---

### 2.4 Search Patients in Ward

Searches patients in a ward by specified keys and value.

**Endpoint:** `GET /ipd/wards/{wardUuid}/patients/search`

**Path parameter:** `wardUuid` - Ward/location UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| offset | integer | Yes | Pagination offset |
| limit | integer | Yes | Page size |
| searchKeys | list | Yes | Fields to search (e.g. name, identifier) |
| searchValue | string | Yes | Search term |
| sortBy | string | No | Sort field |

**Response:** `200 OK` - `IPDPatientDetailsResponse` (same structure as 2.2)

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/wards/ward-uuid/patients/search?offset=0&limit=10&searchKeys=name&searchKeys=identifier&searchValue=John"
```

---

## 3. Visit Medications

Base path: `/ipdVisit/{visitUuid}`

### 3.1 Get Visit Medications

Returns prescribed drug orders and optionally emergency medications for a visit.

**Endpoint:** `GET /ipdVisit/{visitUuid}/medication`

**Path parameter:** `visitUuid` - Visit UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| includes | list | No | Include "emergencyMedications" to add emergency meds |

**Required privileges:** `Get Medication Administration`, `Get Medication Tasks`

**Response:** `200 OK` - `IPDTreatmentsResponse`

```json
{
  "ipdDrugOrders": [
    {
      "orderUuid": "order-uuid",
      "drugName": "Paracetamol 500mg",
      "dose": 500.0,
      "doseUnitsName": "mg",
      "routeName": "Oral",
      "durationUnitsName": "days",
      "duration": 5,
      "quantity": 10.0,
      "quantityUnitsName": "tablet",
      "frequency": "Twice daily",
      "asNeeded": false,
      "action": "NEW",
      "orderSetUuid": null,
      "providerUuid": "provider-uuid",
      "providerName": "Dr. Smith",
      "drugOrderSchedule": {
        "firstDaySlotsStartTime": [1710580200000],
        "dayWiseSlotsStartTime": [43200000],
        "remainingDaySlotsStartTime": [],
        "slotStartTime": 1710580200000,
        "medicationAdministrationStarted": true,
        "pendingSlotsAvailable": false,
        "allSlotsAttended": true,
        "notes": null
      }
    }
  ],
  "emergencyMedications": [
    {
      "uuid": "...",
      "status": "COMPLETED",
      "administeredDateTime": "...",
      "drug": {...},
      "dose": 5.0,
      ...
    }
  ]
}
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipdVisit/visit-uuid/medication?includes=emergencyMedications"
```

---

## 4. Schedule

Base path: `/ipd/schedule`

### 4.1 Create Medication Schedule

Creates a new medication schedule with slots.

**Endpoint:** `POST /ipd/schedule/type/medication`

**Required privilege:** `Edit Medication Tasks`

**Request body:** `ScheduleMedicationRequest`

| Field | Type | Description |
|-------|------|-------------|
| patientUuid | string | Patient UUID |
| orderUuid | string | Drug order UUID |
| providerUuid | string | Provider UUID |
| comments | string | Optional comments |
| slotStartTime | long | Unix timestamp (seconds) for schedule start |
| firstDaySlotsStartTime | array | Timestamps for first-day slots |
| dayWiseSlotsStartTime | array | Recurring daily slot times (ms from midnight) |
| remainingDaySlotsStartTime | array | Slots for remaining days |
| medicationFrequency | string | `START_TIME_DURATION_FREQUENCY` or `FIXED_SCHEDULE_FREQUENCY` |
| serviceType | string | `MEDICATION_REQUEST`, `EMERGENCY_MEDICATION_REQUEST`, `AS_NEEDED_MEDICATION_REQUEST`, `AS_NEEDED_PLACEHOLDER` |

**Response:** `200 OK` - `ScheduleMedicationResponse`

```json
{
  "id": 1,
  "patientUuid": "patient-uuid",
  "comments": "Morning dose",
  "startDate": 1710580200,
  "endDate": null,
  "order": null
}
```

**Example request:**
```bash
curl -X POST "http://localhost:8080/openmrs/ws/rest/v1/ipd/schedule/type/medication" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '{
    "patientUuid": "patient-uuid",
    "orderUuid": "order-uuid",
    "providerUuid": "provider-uuid",
    "slotStartTime": 1710580200,
    "firstDaySlotsStartTime": [1710580200],
    "dayWiseSlotsStartTime": [43200000],
    "remainingDaySlotsStartTime": [],
    "medicationFrequency": "FIXED_SCHEDULE_FREQUENCY",
    "serviceType": "MEDICATION_REQUEST"
  }'
```

---

### 4.2 Update Medication Schedule

Updates an existing medication schedule.

**Endpoint:** `POST /ipd/schedule/type/medication/edit`

**Required privilege:** `Edit Medication Tasks`

**Request body:** `ScheduleMedicationRequest` (same as create, include schedule identifier if applicable)

**Response:** `200 OK` - `ScheduleMedicationResponse`

---

### 4.3 Get Medication Slots by Date Range

Returns medication slots for a patient within a time frame.

**Endpoint:** `GET /ipd/schedule/type/medication`

**Required query params:** `patientUuid`, `startTime`, `endTime`

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patientUuid | string | Yes | Patient UUID |
| startTime | long | Yes | Start timestamp (epoch seconds) |
| endTime | long | Yes | End timestamp (epoch seconds) |
| visitUuid | string | No | Visit UUID (defaults to active visit) |
| view | string | No | `drugChart` for drug chart view (uses administered time) |

**Required privileges:** `Get Medication Administration`, `Get Medication Tasks`

**Response:** `200 OK` - `List<MedicationScheduleResponse>`

```json
[
  {
    "id": 1,
    "uuid": "schedule-uuid",
    "serviceType": "MedicationRequest",
    "comments": null,
    "startDate": 1710580200,
    "endDate": null,
    "slots": [
      {
        "id": 1,
        "uuid": "slot-uuid",
        "serviceType": "MedicationRequest",
        "status": "COMPLETED",
        "startTime": 1710580200,
        "order": {...},
        "medicationAdministration": {...},
        "notes": null
      }
    ]
  }
]
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/schedule/type/medication?patientUuid=patient-uuid&startTime=1710504000&endTime=1710590400&view=drugChart"
```

---

### 4.4 Get Medication Slots by Order

Returns medication slots for a patient, optionally filtered by order UUIDs or service type.

**Endpoint:** `GET /ipd/schedule/type/medication`

**Required query param:** `patientUuid` only (no startTime/endTime)

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patientUuid | string | Yes | Patient UUID |
| serviceType | string | No | `MEDICATION_REQUEST`, `EMERGENCY_MEDICATION_REQUEST`, etc. |
| orderUuids | list | No | Filter by order UUIDs |

**Required privileges:** `Get Medication Administration`, `Get Medication Tasks`

**Response:** `200 OK` - `List<MedicationSlotResponse>`

```json
[
  {
    "id": 1,
    "uuid": "slot-uuid",
    "serviceType": "MedicationRequest",
    "status": "COMPLETED",
    "startTime": 1710580200,
    "order": {...},
    "medicationAdministration": {...},
    "notes": null
  }
]
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/schedule/type/medication?patientUuid=patient-uuid&orderUuids=order1&orderUuids=order2"
```

---

### 4.5 Get Patients Medication Summary

Returns medication summary for multiple patients within a time range.

**Endpoint:** `GET /ipd/schedule/type/medication/patientsMedicationSummary`

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| patientUuids | list | Yes | List of patient UUIDs |
| startTime | long | Yes | Start timestamp (epoch seconds) |
| endTime | long | Yes | End timestamp (epoch seconds) |
| includePreviousSlot | boolean | No | Include previous slot in summary |
| includeSlotDuration | boolean | No | Include slot duration |

**Response:** `200 OK` - `List<PatientMedicationSummaryResponse>`

```json
[
  {
    "patientUuid": "patient-uuid",
    "prescribedOrderSlots": [
      {
        "orderUuid": "order-uuid",
        "currentSlots": [...],
        "previousSlot": {...},
        "initialSlotStartTime": 1710580200,
        "finalSlotStartTime": 1710666600
      }
    ],
    "emergencyMedicationSlots": [...]
  }
]
```

**Example request:**
```bash
curl "http://localhost:8080/openmrs/ws/rest/v1/ipd/schedule/type/medication/patientsMedicationSummary?patientUuids=uuid1&patientUuids=uuid2&startTime=1710504000&endTime=1710590400"
```

---

## 5. Care Team

Base path: `/ipd/careteam`

### 5.1 Create/Update Care Team Participants

Saves or updates care team participants for a patient.

**Endpoint:** `POST /ipd/careteam/participants`

**Request body:** `CareTeamRequest`

| Field | Type | Description |
|-------|------|-------------|
| patientUuid | string | Patient UUID |
| careTeamParticipantsRequest | array | List of participant objects |

** careTeamParticipantsRequest item:**
| Field | Type | Description |
|-------|------|-------------|
| uuid | string | (optional) For update |
| startTime | long | Start timestamp (ms) |
| endTime | long | End timestamp (ms) |
| providerUuid | string | Provider UUID |
| voided | boolean | Whether voided |

**Response:** `200 OK` - `CareTeamResponse`

```json
{
  "uuid": "careteam-uuid",
  "patientUuid": "patient-uuid",
  "participants": [
    {
      "uuid": "participant-uuid",
      "provider": {"uuid": "...", "display": "Dr. Smith"},
      "startTime": 1710580200000,
      "endTime": null,
      "voided": false
    }
  ]
}
```

**Example request:**
```bash
curl -X POST "http://localhost:8080/openmrs/ws/rest/v1/ipd/careteam/participants" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '{
    "patientUuid": "patient-uuid",
    "careTeamParticipantsRequest": [
      {
        "providerUuid": "provider-uuid",
        "startTime": 1710580200000,
        "endTime": null,
        "voided": false
      }
    ]
  }'
```

---

## Error Responses

All endpoints may return:

| Status | Description |
|--------|-------------|
| 400 Bad Request | Invalid parameters or business logic error |
| 403 Forbidden | Missing required privilege |
| 500 Internal Server Error | Server error |

Error response format:

```json
{
  "error": {
    "message": "Error description",
    "code": "optional_error_code",
    "detail": "Optional detailed message"
  }
}
```

---

## Privileges Reference

| Privilege | Description |
|-----------|-------------|
| Edit Medication Administration | Create/update scheduled medication administrations |
| Edit adhoc medication tasks | Create/update adhoc medication administrations |
| Edit Medication Tasks | Create/update medication schedules |
| Get Medication Administration | View medication administration data |
| Get Medication Tasks | View medication schedule/slot data |
