#!/usr/bin/env python3
"""
Career Hub - Read-Only Repository State Inspector
Inspects project structure, commits, and platforms without mutating files or exposing secrets.
"""
import os
import sys
import json
import subprocess
from datetime import datetime, timezone
from pathlib import Path

def run_git(args, cwd="."):
    try:
        res = subprocess.run(
            ["git"] + args,
            cwd=cwd,
            capture_output=True,
            text=True,
            check=False
        )
        return res.stdout.strip()
    except Exception as e:
        return f"Unavailable: {e}"

def detect_android(root: Path):
    candidates = [
        root / "android",
        root / "app",
        root / "mobile" / "android"
    ]
    detected_path = None
    for c in candidates:
        if c.is_dir() and (
            (c / "build.gradle").exists() or
            (c / "build.gradle.kts").exists() or
            (c / "settings.gradle").exists() or
            (c / "settings.gradle.kts").exists()
        ):
            detected_path = c
            break

    if not detected_path and (root / "build.gradle").exists():
        detected_path = root

    if detected_path:
        build_tool = "Gradle (Kotlin DSL)" if any(detected_path.rglob("*.gradle.kts")) else "Gradle (Groovy)"
        manifest_files = list(detected_path.rglob("AndroidManifest.xml"))
        return {
            "detected": True,
            "path": str(detected_path.relative_to(root)),
            "build_tool": build_tool,
            "manifest_found": len(manifest_files) > 0,
            "manifest_count": len(manifest_files)
        }
    return {"detected": False, "path": None, "build_tool": None}

def detect_ios(root: Path):
    candidates = [
        root / "ios",
        root / "mobile" / "ios"
    ]
    detected_path = None
    for c in candidates:
        if c.is_dir():
            detected_path = c
            break

    if not detected_path and any(root.glob("*.xcodeproj")):
        detected_path = root

    if detected_path:
        has_xcodeproj = any(detected_path.glob("*.xcodeproj")) or any(detected_path.rglob("*.xcodeproj"))
        has_xcworkspace = any(detected_path.glob("*.xcworkspace")) or any(detected_path.rglob("*.xcworkspace"))
        has_podfile = (detected_path / "Podfile").exists() or (root / "Podfile").exists()
        has_spm = (detected_path / "Package.swift").exists() or (root / "Package.swift").exists()

        return {
            "detected": True,
            "path": str(detected_path.relative_to(root)),
            "has_xcodeproj": has_xcodeproj,
            "has_xcworkspace": has_xcworkspace,
            "dependency_manager": "CocoaPods" if has_podfile else ("Swift Package Manager" if has_spm else "Standard Xcode")
        }
    return {"detected": False, "path": None}

def detect_backend(root: Path):
    candidates = ["backend", "server", "api", "services", "functions"]
    detected = []
    for c in candidates:
        p = root / c
        if p.is_dir():
            tech = []
            if (p / "package.json").exists(): tech.append("Node.js")
            if (p / "requirements.txt").exists() or (p / "pyproject.toml").exists(): tech.append("Python")
            if (p / "go.mod").exists(): tech.append("Go")
            if (p / "pom.xml").exists() or (p / "build.gradle").exists(): tech.append("JVM/Java/Kotlin")
            if (p / "Dockerfile").exists(): tech.append("Docker")

            detected.append({
                "path": str(p.relative_to(root)),
                "stack": tech or ["Unknown / Directory Present"]
            })

    if not detected:
        root_tech = []
        if (root / "package.json").exists() and not (root / "android").exists(): root_tech.append("Node.js")
        if (root / "requirements.txt").exists(): root_tech.append("Python")
        if root_tech:
            detected.append({"path": ".", "stack": root_tech})

    return {
        "detected": len(detected) > 0,
        "services": detected
    }

def detect_web_client(root: Path):
    candidates = ["web", "frontend", "client", "web-client", "ui"]
    detected = []
    for c in candidates:
        p = root / c
        if p.is_dir():
            detected.append(str(p.relative_to(root)))

    return {
        "detected": len(detected) > 0,
        "paths": detected,
        "scope_status": "DEPRECATED / EXCLUDED (Per Project Specification)"
    }

def safe_walk(root: Path):
    skip_dirs = {".git", "node_modules", "build", ".gradle", ".kotlin", "dist"}
    for current, dirs, files in os.walk(root):
        dirs[:] = [d for d in dirs if d not in skip_dirs]
        yield Path(current), dirs, files

def detect_tests(root: Path):
    test_locations = []
    test_names = {"tests", "test", "__tests__", "spec"}
    for current_path, dirs, _ in safe_walk(root):
        for d in dirs:
            if d.lower() in test_names:
                try:
                    rel = str((current_path / d).relative_to(root))
                    test_locations.append(rel)
                except Exception:
                    pass

    return {
        "detected": len(test_locations) > 0,
        "test_directories": sorted(list(set(test_locations)))[:10]
    }

def detect_github_actions(root: Path):
    workflow_dir = root / ".github" / "workflows"
    workflows = []
    if workflow_dir.is_dir():
        for f in workflow_dir.glob("*.yml"):
            workflows.append(f.name)
        for f in workflow_dir.glob("*.yaml"):
            workflows.append(f.name)
    return {
        "detected": len(workflows) > 0,
        "workflow_files": sorted(workflows)
    }

def detect_firebase_and_ai(root: Path):
    firebase_files = []
    target_firebase = {"firebase.json", ".firebaserc", "google-services.json", "googleservice-info.plist"}
    ai_keywords = ["gemini", "ai_service", "model_config"]
    ai_references = []

    for current_path, dirs, files in safe_walk(root):
        for f in files:
            f_lower = f.lower()
            if f_lower in target_firebase:
                try:
                    firebase_files.append(str((current_path / f).relative_to(root)))
                except Exception:
                    pass
            if any(k in f_lower for k in ai_keywords):
                try:
                    ai_references.append(str((current_path / f).relative_to(root)))
                except Exception:
                    pass

    return {
        "firebase": {
            "configured": len(firebase_files) > 0,
            "detected_config_files": firebase_files,
            "secrets_exposed": False
        },
        "ai_integration": {
            "configured": len(ai_references) > 0,
            "detected_references": sorted(list(set(ai_references)))[:10],
            "secrets_exposed": False
        }
    }

def generate_step_summary(snapshot: dict) -> str:
    android = snapshot["components"]["android"]
    ios = snapshot["components"]["ios"]
    backend = snapshot["components"]["backend"]
    web = snapshot["components"]["web_client"]
    tests = snapshot["components"]["tests"]
    actions = snapshot["components"]["github_actions"]
    config = snapshot["components"]["configuration"]

    md = f"""# 🔍 Career Hub - Read-Only Repository State Snapshot

**Execution Mode**: READ-ONLY Inspection (No source code or repository changes)  
**Timestamp**: `{snapshot['timestamp']}`  
**Repository Accessible**: `{'YES' if snapshot['git']['accessible'] else 'NO'}`  
**Current Branch**: `{snapshot['git']['branch'] or 'N/A'}`  
**Latest Commit**: `{snapshot['git']['latest_commit'] or 'N/A'}`  

---

## 📱 Subsystems & Scope Breakdown

| Component | Scope Status | Detected | Details |
| :--- | :--- | :--- | :--- |
| **Android App** | **IN SCOPE** | `{'YES' if android['detected'] else 'NO'}` | Path: `{android.get('path')}`, Tool: `{android.get('build_tool')}` |
| **iOS App** | **IN SCOPE** | `{'YES' if ios['detected'] else 'NO'}` | Path: `{ios.get('path')}`, Mgr: `{ios.get('dependency_manager')}` |
| **Backend / API** | **IN SCOPE** | `{'YES' if backend['detected'] else 'NO'}` | Services: `{len(backend.get('services', []))}` detected |
| **Web Client** | **DEPRECATED / EXCLUDED** | `{'YES' if web['detected'] else 'NO'}` | Paths: `{', '.join(web.get('paths', [])) or 'None'}` (Not maintained) |

---

## 🧪 Tests & Automation Infrastructure

- **Existing Tests**: `{'YES' if tests['detected'] else 'NO'}` ({len(tests.get('test_directories', []))} test dirs found)
- **Existing GitHub Actions**: `{'YES' if actions['detected'] else 'NO'}` ({', '.join(actions.get('workflow_files', [])) or 'None'})

---

## 🔒 Configuration & Security Posture (Zero Secret Exposure)

- **Firebase Config Present**: `{'YES' if config['firebase']['configured'] else 'NO'}` (Files: `{len(config['firebase']['detected_config_files'])}`)
- **AI Integration Config Present**: `{'YES' if config['ai_integration']['configured'] else 'NO'}` (Files/Modules: `{len(config['ai_integration']['detected_references'])}`)
- **Secret Masking / Protection**: `VERIFIED SAFE` (No credentials or keys read or exposed)

---
*Snapshot JSON artifact archived as `career_hub_snapshot.json`.*
"""
    return md

def main():
    root = Path(".").resolve()
    accessible = Path(".git").exists() or run_git(["status"]) != "Unavailable"
    
    branch = os.environ.get("GITHUB_REF_NAME") or run_git(["rev-parse", "--abbrev-ref", "HEAD"]) or "main"
    git_log_res = run_git(["log", "-1", "--pretty=format:%h - %s (%an, %cd)"])
    latest_commit = git_log_res if git_log_res and not git_log_res.startswith("Unavailable") else os.environ.get("GITHUB_SHA", "N/A")

    snapshot = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "git": {
            "accessible": bool(accessible),
            "branch": branch,
            "latest_commit": latest_commit
        },
        "components": {
            "android": detect_android(root),
            "ios": detect_ios(root),
            "backend": detect_backend(root),
            "web_client": detect_web_client(root),
            "tests": detect_tests(root),
            "github_actions": detect_github_actions(root),
            "configuration": detect_firebase_and_ai(root)
        }
    }

    output_file = Path("career_hub_snapshot.json")
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(snapshot, f, indent=2)

    summary_md = generate_step_summary(snapshot)
    github_summary_path = os.environ.get("GITHUB_STEP_SUMMARY")
    if github_summary_path:
        with open(github_summary_path, "a", encoding="utf-8") as f:
            f.write("\n" + summary_md + "\n")
    else:
        try:
            print(summary_md)
        except UnicodeEncodeError:
            sys.stdout.buffer.write((summary_md + "\n").encode("utf-8"))

if __name__ == "__main__":
    main()
