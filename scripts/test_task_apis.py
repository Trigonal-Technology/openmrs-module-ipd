#!/usr/bin/env python3
"""
Smoke test for IPD Task APIs.

Covers:
  - POST /ws/rest/v1/tasks
  - GET  /ws/rest/v1/tasks?patient=...&status=...
  - POST /ws/rest/v1/tasks/{uuid}

Usage:
  python scripts/test_task_apis.py --base-url http://localhost/openmrs --username admin --password Admin123
"""

from __future__ import annotations

import argparse
import base64
import json
import sys
from datetime import datetime, timezone
from urllib import parse, request, error


def iso_now() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def auth_header(username: str, password: str) -> str:
    token = base64.b64encode(f"{username}:{password}".encode("utf-8")).decode("ascii")
    return f"Basic {token}"


def http_json(method: str, url: str, headers: dict[str, str], payload: dict | None = None) -> tuple[int, dict]:
    data = None
    req_headers = dict(headers)
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        req_headers["Content-Type"] = "application/json"
    req = request.Request(url=url, method=method, data=data, headers=req_headers)
    try:
        with request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode("utf-8")
            return resp.status, json.loads(body) if body else {}
    except error.HTTPError as e:
        body = e.read().decode("utf-8") if e.fp else ""
        parsed = {}
        if body:
            try:
                parsed = json.loads(body)
            except json.JSONDecodeError:
                parsed = {"raw": body}
        return e.code, parsed


def fail(step: str, code: int, body: dict) -> None:
    print(f"[FAIL] {step} -> HTTP {code}")
    print(json.dumps(body, indent=2))
    sys.exit(1)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://localhost/openmrs", help="OpenMRS base URL")
    parser.add_argument("--username", default="admin")
    parser.add_argument("--password", default="Admin123")
    parser.add_argument("--patient-uuid", default="", help="Patient UUID to test against (required if patient search returns none)")
    args = parser.parse_args()

    base = args.base_url.rstrip("/")
    rest = f"{base}/ws/rest/v1"
    headers = {"Authorization": auth_header(args.username, args.password), "Accept": "application/json"}

    # 1) Resolve patient UUID
    patient_uuid = args.patient_uuid
    if not patient_uuid:
        # Many OpenMRS REST distributions disable patient get-all and only allow search (q=...)
        patient_url = f"{rest}/patient?q=a&limit=1&v={parse.quote('custom:(uuid,display)')}"
        code, body = http_json("GET", patient_url, headers)
        if code != 200:
            fail("GET patient", code, body)
        results = body.get("results") or []
        if not results:
            print("[FAIL] GET patient -> no patients returned; cannot continue")
            print("       Re-run with --patient-uuid <existing-patient-uuid>")
            sys.exit(1)
        patient_uuid = results[0]["uuid"]
    print(f"[OK] patient selected: {patient_uuid}")

    # 2) Create task
    create_url = f"{rest}/tasks"
    create_payload = {
        "name": f"API Smoke Task {iso_now()}",
        "description": "Created by Python smoke test",
        "intent": "order",
        "status": "REQUESTED",
        "priority": "routine",
        "patient": {"uuid": patient_uuid},
        "executionPeriod": {"start": iso_now()},
    }
    code, body = http_json("POST", create_url, headers, create_payload)
    if code != 200:
        fail("POST /tasks (create)", code, body)
    task_uuid = body.get("uuid")
    if not task_uuid:
        fail("POST /tasks (create: missing uuid)", code, body)
    print(f"[OK] task created: {task_uuid}")

    # 3) List active statuses
    list_url = f"{rest}/tasks?patient={parse.quote(patient_uuid)}&status=REQUESTED,IN_PROGRESS"
    code, body = http_json("GET", list_url, headers)
    if code != 200:
        fail("GET /tasks list", code, body)
    listed = body.get("results") or []
    print(f"[OK] listed active tasks: {len(listed)}")

    # 4) Update to IN_PROGRESS
    update_url = f"{rest}/tasks/{task_uuid}"
    in_progress_payload = {"status": "IN_PROGRESS", "notes": "Started by smoke test"}
    code, body = http_json("POST", update_url, headers, in_progress_payload)
    if code != 200:
        fail("POST /tasks/{uuid} IN_PROGRESS", code, body)
    print(f"[OK] task moved to IN_PROGRESS: {body.get('status')}")

    # 5) Update to COMPLETED
    completed_payload = {"status": "COMPLETED", "notes": "Completed by smoke test"}
    code, body = http_json("POST", update_url, headers, completed_payload)
    if code != 200:
        fail("POST /tasks/{uuid} COMPLETED", code, body)
    print(f"[OK] task moved to COMPLETED: {body.get('status')}")

    # 6) Verify completed filter
    list_completed_url = f"{rest}/tasks?patient={parse.quote(patient_uuid)}&status=COMPLETED"
    code, body = http_json("GET", list_completed_url, headers)
    if code != 200:
        fail("GET /tasks completed list", code, body)
    completed = body.get("results") or []
    found = any(t.get("uuid") == task_uuid for t in completed)
    if not found:
        fail("GET /tasks completed list (created task not found)", code, body)
    print(f"[OK] completed task present in filtered list: {task_uuid}")

    print("\nAll task API smoke tests passed.")


if __name__ == "__main__":
    main()

