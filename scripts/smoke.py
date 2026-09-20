"""End-to-end API and signed webhook check; standard library only."""
import hashlib
import hmac
import json
import os
import sys
import uuid
from urllib.error import HTTPError
from urllib.request import Request, urlopen

BASE = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080").rstrip("/")
secret_value = os.getenv("GITHUB_WEBHOOK_SECRET")
if not secret_value:
    sys.exit("Set GITHUB_WEBHOOK_SECRET in your shell to the same value as in .env before running this check.")
SECRET = secret_value.encode()

def call(method, path, data=None, expected=200, headers=None):
    body = None if data is None else json.dumps(data, separators=(",", ":")).encode()
    request = Request(BASE + path, body, method=method, headers={"Content-Type": "application/json", **(headers or {})})
    try:
        response = urlopen(request, timeout=10)
    except HTTPError as error:
        response = error
    with response:
        status, content = response.status, response.read()
    assert status == expected, f"{method} {path}: expected {expected}, got {status}: {content!r}"
    return json.loads(content) if content else None

call("GET", "/actuator/health")
suffix = uuid.uuid4().hex[:10]
project_path = None
try:
    created = call("POST", "/api/v1/projects", {
        "name": "smoke-project", "repositoryUrl": f"https://github.com/forgecloud-smoke/{suffix}", "branch": "main"
    }, 201)["project"]
    project_path = "/api/v1/projects/" + created["id"]
    call("GET", project_path)
    manual = call("POST", project_path + "/pipelines", expected=201)["pipeline"]
    assert manual["status"] == "QUEUED" and manual["triggerType"] == "MANUAL"

    payload = {"repository": {"full_name": f"forgecloud-smoke/{suffix}"}, "ref": "refs/heads/main", "after": "a" * 40}
    raw = json.dumps(payload, separators=(",", ":")).encode()
    delivery = str(uuid.uuid4())
    signature = "sha256=" + hmac.new(SECRET, raw, hashlib.sha256).hexdigest()
    webhook_headers = {"X-GitHub-Event": "push", "X-GitHub-Delivery": delivery, "X-Hub-Signature-256": signature}
    first = call("POST", "/api/v1/webhooks/github", payload, 202, webhook_headers)["pipeline"]
    second = call("POST", "/api/v1/webhooks/github", payload, 202, webhook_headers)["pipeline"]
    assert first["id"] == second["id"] and first["commitSha"] == "a" * 40
    runs = call("GET", "/api/v1/pipelines")["pipelines"]
    assert any(run["id"] == first["id"] for run in runs)
    call("POST", "/api/v1/webhooks/github", payload, 400, {**webhook_headers, "X-Hub-Signature-256": "sha256=wrong"})
    call("DELETE", project_path, expected=204)
    call("GET", project_path, expected=404)
    project_path = None
    print("PASS: health, project CRUD, manual trigger, signed webhook, deduplication, rejection and cascade delete")
finally:
    if project_path:
        try:
            call("DELETE", project_path, expected=204)
        except Exception as exception:
            print(f"Cleanup failed: {exception}", file=sys.stderr)
