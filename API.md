# IPD Module REST API Reference

The IPD (Inpatient Department) module provides REST APIs for ward management, medication administration, scheduling, care teams, and visit-based treatments.

**Base URL:** `{openmrs_base_url}/ws/rest/v1`  
Example: `http://localhost:8080/openmrs/ws/rest/v1`

**Authentication:** Session-based or Basic Auth (OpenMRS standard)

---

## Table of Contents

1. [APIs (Request & Response with Samples)](#1-apis-request--response-with-samples)
2. [Comparison with Upstream Repositories](#2-comparison-with-upstream-repositories)
3. [Comparison Table: Nidan vs Bahmni](#3-comparison-table-nidan-vs-bahmni)
4. [Bahmni Frontend Compatibility & Breaking Changes](#4-bahmni-frontend-compatibility--breaking-changes)

---

## 1. APIs (Request & Response with Samples)

### 1.1 Medication Administration

#### POST /ipd/scheduledMedicationAdministrations

Creates multiple scheduled medication administration records.

**Request:**
```json
[
  {
    "patientUuid": "86526ed5-3c4b-4a3b-9a2b-1c2d3e4f5a6b",
    "encounterUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "orderUuid": "order-uuid-here",
    "providers": [
      {
        "providerUuid": "provider-uuid-here",
        "function": "concept-uuid-for-performer"
      }
    ],
    "notes": [],
    "status": "COMPLETED",
    "drugUuid": "drug-concept-uuid",
    "dose": 500.0,
    "doseUnits": "concept-uuid-for-mg",
    "route": "concept-uuid-for-oral",
    "administeredDateTime": 1710580200,
    "slotUuid": "slot-uuid-here"
  }
]
```

**Response (200 OK):**
```json
[
  {
    "uuid": "abc-123-def-456",
    "patientUuid": "86526ed5-3c4b-4a3b-9a2b-1c2d3e4f5a6b",
    "encounterUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "orderUuid": "order-uuid-here",
    "providers": [
      {
        "uuid": "performer-uuid",
        "provider": {"uuid": "...", "display": "Dr. Smith"},
        "function": "performer"
      }
    ],
    "notes": [],
    "status": "COMPLETED",
    "statusReason": null,
    "drug": {"uuid": "...", "display": "Paracetamol 500mg"},
    "dosingInstructions": null,
    "dose": 500.0,
    "doseUnits": {"uuid": "...", "display": "mg"},
    "route": {"uuid": "...", "display": "Oral"},
    "site": null,
    "administeredDateTime": "2024-03-16T10:30:00.000+0000"
  }
]
```

**Sample curl:**
```bash
curl -X POST "http://localhost:8080/openmrs/ws/rest/v1/ipd/scheduledMedicationAdministrations" \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=..." \
  -d '[{"patientUuid":"patient-uuid","encounterUuid":"enc-uuid","orderUuid":"order-uuid","providers":[{"providerUuid":"provider-uuid","function":"concept-uuid"}],"notes":[],"status":"COMPLETED","drugUuid":"drug-uuid","dose":500.0,"doseUnits":"concept-uuid","route":"concept-uuid","administeredDateTime":1710580200,"slotUuid":"slot-uuid"}]'
```

---

#### POST /ipd/adhocMedicationAdministrations

Creates a single adhoc (one-off) medication administration.

**Request:** Same structure as above (single object, no array).

**Response (200 OK):** Single `MedicationAdministrationResponse` object.

---

#### PUT /ipd/adhocMedicationAdministrations/{medicationAdministrationUuid}

Updates an existing adhoc medication administration.

**Request:** Same structure as create; include `uuid` for update.

---

### 1.2 Wards

#### GET /ipd/wards/{wardUuid}/summary?providerUuid={providerUuid}

**Response (200 OK):**
```json
{
  "totalPatients": 25,
  "totalProviderPatients": 8
}
```

---

#### GET /ipd/wards/{wardUuid}/patients?offset=0&limit=10&sortBy=bedNumber

**Response (200 OK):**
```json
{
  "admittedPatients": [
    {
      "patientDetails": {"uuid": "...", "display": "John Doe"},
      "bedDetails": {"uuid": "...", "display": "Bed 101"},
      "visitDetails": {"uuid": "...", "display": "..."},
      "newTreatments": 2,
      "careTeam": {
        "uuid": "...",
        "patientUuid": "...",
        "participants": [
          {
            "uuid": "...",
            "provider": {"uuid": "...", "display": "Dr. Smith"},
            "startTime": 1710580200000,
            "endTime": null,
            "voided": false
          }
        ]
      }
    }
  ],
  "totalPatients": 25
}
```

---

#### GET /ipd/wards/{wardUuid}/myPatients?providerUuid={uuid}&offset=0&limit=10

Same response structure as `/patients`.

---

#### GET /ipd/wards/{wardUuid}/patients/search?offset=0&limit=10&searchKeys=name&searchKeys=identifier&searchValue=John

Same response structure as `/patients`.

---

### 1.3 Visit Medications

#### GET /ipdVisit/{visitUuid}/medication?includes=emergencyMedications

**Response (200 OK):**
```json
{
  "ipdDrugOrders": [
    {
      "orderUuid": "order-uuid",
      "drugName": "Paracetamol 500mg",
      "dose": 500.0,
      "doseUnitsName": "mg",
      "routeName": "Oral",
      "frequency": "Twice daily",
      "drugOrderSchedule": {
        "firstDaySlotsStartTime": [1710580200000],
        "dayWiseSlotsStartTime": [43200000],
        "medicationAdministrationStarted": true,
        "allSlotsAttended": true
      }
    }
  ],
  "emergencyMedications": [
    {
      "uuid": "...",
      "status": "COMPLETED",
      "administeredDateTime": "2024-03-16T10:30:00.000+0000",
      "drug": {"uuid": "...", "display": "Paracetamol"},
      "dose": 5.0
    }
  ]
}
```

---

### 1.4 Schedule

#### POST /ipd/schedule/type/medication

**Request:**
```json
{
  "patientUuid": "patient-uuid",
  "orderUuid": "order-uuid",
  "providerUuid": "provider-uuid",
  "slotStartTime": 1710580200,
  "firstDaySlotsStartTime": [1710580200],
  "dayWiseSlotsStartTime": [43200000],
  "remainingDaySlotsStartTime": [],
  "medicationFrequency": "FIXED_SCHEDULE_FREQUENCY",
  "serviceType": "MEDICATION_REQUEST"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "patientUuid": "patient-uuid",
  "comments": null,
  "startDate": 1710580200,
  "endDate": null,
  "order": null
}
```

---

#### POST /ipd/schedule/type/medication/edit

Same request/response as create.

---

#### GET /ipd/schedule/type/medication?patientUuid={uuid}&startTime={epoch}&endTime={epoch}&view=drugChart

**Response (200 OK):** `List<MedicationScheduleResponse>` with slots.

---

#### GET /ipd/schedule/type/medication?patientUuid={uuid}&orderUuids=order1&orderUuids=order2

**Response (200 OK):** `List<MedicationSlotResponse>`.

---

#### GET /ipd/schedule/type/medication/patientsMedicationSummary?patientUuids=uuid1&patientUuids=uuid2&startTime={epoch}&endTime={epoch}

**Response (200 OK):** `List<PatientMedicationSummaryResponse>`.

---

### 1.5 Care Team

#### POST /ipd/careteam/participants

**Request:**
```json
{
  "patientUuid": "patient-uuid",
  "careTeamParticipantsRequest": [
    {
      "providerUuid": "provider-uuid",
      "startTime": 1710580200000,
      "endTime": null,
      "voided": false
    }
  ]
}
```

**Response (200 OK):**
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

---

### 1.6 FHIR CareTeam (Nidan-only)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/ws/fhir2/R4/CareTeam/{id}` | GET | Read CareTeam by ID |
| `/ws/fhir2/R4/CareTeam?patient={ref}&encounter={ref}` | GET | Search CareTeam |

---

## 2. Comparison with Upstream Repositories

### 2.1 [Bahmni/openmrs-module-ipd](https://github.com/Bahmni/openmrs-module-ipd)

| Aspect | Bahmni Upstream | Nidan (Ours) |
|--------|-----------------|---------------|
| **REST endpoints** | Same paths | Same paths |
| **Medication storage** | FHIR2 (FHIR MedicationAdministration in DB) | Domain model (medication-administration-api) |
| **doseUnits, route, site, function** | Concept **name** (`getConceptByName`) | Concept **UUID** (`getConceptByUuid`) |
| **Dependencies** | fhir2Extension-api, fhir2Extension-omod, medication-administration 1.0.0 | No fhir2Extension; medication-administration 2.0.0-nidan-SNAPSHOT |
| **FHIR CareTeam** | Via fhir2Extension (if present) | Built-in in IPD (`org.openmrs.module.ipd.fhir2`) |
| **FHIR2 version** | 2.1.0 | 3.1.0-nidan-SNAPSHOT (local build) |

### 2.2 [Bahmni/bahmni-module-fhir2-addl-extension](https://github.com/Bahmni/bahmni-module-fhir2-addl-extension)

This module provides FHIR2 extensions (MedicationAdministration, CareTeam, etc.) used by Bahmni IPD. Nidan does **not** use this module; we implement FHIR CareTeam directly in the IPD module and use medication-administration-api for MedicationAdministration storage.

---

## 3. Comparison Table: Nidan vs Bahmni

| Feature | Bahmni | Nidan |
|---------|--------|-------|
| **Module version** | 1.2.0-SNAPSHOT | 1.2.0-nidan-SNAPSHOT |
| **fhir2Extension** | Required | Not used |
| **medication-administration-api** | 1.0.0 | 2.0.0-nidan-SNAPSHOT |
| **FHIR2** | 2.1.0 | 3.1.0-nidan-SNAPSHOT |
| **MedicationAdministration storage** | FHIR resource (FHIR2 DB) | Domain model (Hibernate) |
| **doseUnits in request** | Concept name (e.g. `"mg"`) | Concept UUID |
| **route in request** | Concept name (e.g. `"Oral"`) | Concept UUID |
| **site in request** | Concept name | Concept UUID |
| **function in providers** | Concept name (e.g. `"Performer"`) | Concept UUID |
| **FHIR CareTeam** | Via fhir2Extension | Built-in in IPD |
| **API.md** | Not present | Present (this file) |

---

## 4. Bahmni Frontend Compatibility & Breaking Changes

The [Bahmni/openmrs-module-ipd-frontend](https://github.com/Bahmni/openmrs-module-ipd-frontend) uses these IPD endpoints (from `constants.js`):

| Frontend constant | URL | Nidan backend |
|-------------------|-----|---------------|
| MEDICATIONS_BASE_URL | `/ipd/schedule/type/medication` | ✓ Same |
| EDIT_MEDICATIONS_BASE_URL | `/ipd/schedule/type/medication/edit` | ✓ Same |
| ADMINISTERED_MEDICATIONS_BASE_URL | `/ipd/scheduledMedicationAdministrations` | ✓ Same |
| EMERGENCY_MEDICATIONS_BASE_URL | `/ipd/adhocMedicationAdministrations` | ✓ Same |
| BOOKMARK_PATIENT_BASE_URL | `/ipd/careteam/participants` | ✓ Same |
| ALL_DRUG_ORDERS_URL | `/ipdVisit/{visitUuid}/medication?includes=emergencyMedications` | ✓ Same |
| WARD_SUMMARY_URL | `/ipd/wards/{wardId}/summary` | ✓ Same |
| GET_PATIENT_LIST_URL | `/ipd/wards/{wardId}/patients` | ✓ Same |
| GET_MY_PATIENT_LIST_URL | `/ipd/wards/{wardId}/myPatients` | ✓ Same |
| GET_SEARCH_PATIENT_LIST_URL | `/ipd/wards/{wardId}/patients/search` | ✓ Same |
| GET_SLOTS_FOR_PATIENTS_URL | `/ipd/schedule/type/medication/patientsMedicationSummary` | ✓ Same |

### Breaking Changes When Using Bahmni Frontend with Nidan Backend

| Issue | Bahmni frontend sends | Nidan backend expects | Fix |
|-------|------------------------|------------------------|-----|
| **doseUnits** | Concept name (e.g. `"mg"`) | Concept UUID | Frontend must send concept UUID or backend must add `getConceptByName` fallback |
| **route** | Concept name (e.g. `"Oral"`) | Concept UUID | Same |
| **site** | Concept name | Concept UUID | Same |
| **function (in providers)** | Concept name (e.g. `"Performer"`, `"Witness"`, `"Verifier"`) | Concept UUID | Same |
| **Schedule GET params** | `forDate` (in DrugChartUtils) | `startTime`, `endTime` (epoch seconds) | Frontend must convert date to startTime/endTime |
| **bahmnicore** | `/bahmnicore/*` (diagnosis, encounter, etc.) | Not in IPD; requires bahmnicore module | Deploy bahmnicore or adapt frontend |
| **admissionLocation** | `/admissionLocation` for ward list | Not in IPD | Use OpenMRS location API or bedmanagement |
| **tasks** | `/tasks` for nursing tasks | Not in IPD | Requires tasks module |

### Non-IPD Dependencies Used by Bahmni Frontend

The Bahmni IPD frontend also calls:

- `BAHMNI_CORE` – diagnosis, encounter, forms, observations, visit summary
- `FHIR2_R4` – AllergyIntolerance
- `LIST_OF_WARDS_URL` – `/admissionLocation`
- `BED_INFORMATION_URL` – `/beds`
- `GET_TASKS_FOR_PATIENTS_URL` – `/tasks`

These are outside the IPD module. A full Bahmni frontend deployment requires bahmnicore, bedmanagement, and possibly a tasks module.

---

## Error Responses

| Status | Description |
|--------|-------------|
| 400 Bad Request | Invalid parameters or business logic error |
| 403 Forbidden | Missing required privilege |
| 500 Internal Server Error | Server error |

---

## Privileges Reference

| Privilege | Description |
|-----------|-------------|
| Edit Medication Administration | Create/update scheduled medication administrations |
| Edit adhoc medication tasks | Create/update adhoc medication administrations |
| Edit Medication Tasks | Create/update medication schedules |
| Get Medication Administration | View medication administration data |
| Get Medication Tasks | View medication schedule/slot data |
