# openmrs-module-ipd
Openmrs module for Bahmni IPD Care and Monitoring

OpenMRS Module Bahmni-events Backend
=================================
This repository handles Bahmni IPD Care and Monitoring.

## Packaging
```mvn clean package```

### Prerequisite
    JDK 1.8

## Deploy

Copy ```openmrs-module-ipd/omod/target/bahmni-ipd-1.0.0-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS

## Nursing Tasks API (IPD-owned)

This module now provides the non-medication nursing tasks API required by
`trigonal-esm-ipd-app`:

- `GET /ws/rest/v1/tasks?patient={uuid}&status=REQUESTED,IN_PROGRESS`
- `POST /ws/rest/v1/tasks`
- `POST /ws/rest/v1/tasks/{uuid}`

### Required privileges

- `Get Tasks`
- `Add Tasks`
- `Edit Tasks`

### Cutover notes

- Keep frontend task calls pointed at `/ws/rest/v1/tasks` (no frontend path change required).
- Do not rely on `openmrs-module-fhir2Extension` for nursing task APIs in this deployment path.
- Ensure this IPD OMOD is deployed and Liquibase runs to create `ipd_task`.