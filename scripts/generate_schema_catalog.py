#!/usr/bin/env python3
# Copyright 2026 RADAR-base contributors
#
# Licensed under the Apache License, Version 2.0
"""
Build a static HTML catalog of RADAR-base data sources and payloads.

Sections follow data sources (Garmin, Fitbit, Android Phone, …). Each section has
one measurements table (from specifications) and one merged Avro field list
(common metadata fields like time / timeReceived are omitted).

For questionnaires, only the latest aRMT semver specification is included
(Apple Health / HealthKit remains a separate source from main aRMT).
"""

from __future__ import annotations

import argparse
import html
import json
import os
import re
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Mapping

try:
    import yaml
except ImportError:
    print("PyYAML is required: pip install -r requirements-docs.txt", file=sys.stderr)
    sys.exit(1)

PROJECT_GROUP = "org.radarcns"
SPEC_CATEGORIES = ("active", "passive", "connector", "stream", "monitor", "push")

# Avro field names omitted from the merged “payload fields” table (shared metadata).
IGNORE_FIELD_NAMES: frozenset[str] = frozenset(
    {
        "time",
        "timeReceived",
        "timestamp",
        "timeZone",
        "timeZoneOffset",
    }
)

# Preferred section order (display); others follow alphabetically by title.
SECTION_PRIORITY: tuple[str, ...] = (
    "armt",
    "armt-healthkit",
    "android-phone",
    "android-google",
    "apple-sensorkit",
    "fitbit",
    "garmin",
    "oura",
    "empatica-e4",
    "polar",
    "biovotion",
    "ticwatch",
    "pebble",
    "dreem",
    "bittium",
    "oxford",
    "altoida",
    "axivity",
    "physilog",
    "carl-cloud",
    "canedge",
    "openweathermap",
    "rateaf",
    "opensmile",
    "android-phone-audio",
    "garmin-push",
    "garmin-generic",
    "radar-prmt",
    "statistics",
)


def expand_class(shorthand: str | None) -> str | None:
    if not shorthand:
        return None
    if shorthand[0] == ".":
        return PROJECT_GROUP + shorthand
    return shorthand


def load_avro_schemas(commons_root: Path) -> tuple[dict[str, dict[str, Any]], dict[str, Path]]:
    by_name: dict[str, dict[str, Any]] = {}
    paths: dict[str, Path] = {}
    for path in sorted(commons_root.rglob("*.avsc")):
        try:
            data = json.loads(path.read_text(encoding="utf-8"))
        except (json.JSONDecodeError, OSError) as e:
            print(f"Warning: skip {path}: {e}", file=sys.stderr)
            continue
        full = avro_full_name(data)
        if not full:
            continue
        by_name[full] = data
        paths[full] = path
    return by_name, paths


def avro_full_name(schema: Mapping[str, Any]) -> str | None:
    name = schema.get("name")
    if not name:
        return None
    ns = schema.get("namespace")
    if ns:
        return f"{ns}.{name}"
    if isinstance(name, str) and "." in name:
        return name
    return str(name)


def format_avro_type(t: Any) -> str:
    if isinstance(t, str):
        return t
    if isinstance(t, list):
        return " | ".join(format_avro_type(x) for x in t)
    if isinstance(t, dict):
        if "type" in t:
            inner = t["type"]
            if inner == "array":
                return f"array<{format_avro_type(t.get('items'))}>"
            if inner == "map":
                return f"map<{format_avro_type(t.get('values'))}>"
            if inner == "record":
                return str(t.get("name", "record"))
            if inner == "enum":
                return str(t.get("name", "enum"))
            return str(inner)
        return json.dumps(t)
    return str(t)


def format_sample_rate_plain(sr: Any) -> str:
    if sr is None:
        return "—"
    if not isinstance(sr, dict):
        return str(sr)
    parts: list[str] = []
    if sr.get("frequency") is not None:
        parts.append(f"{sr['frequency']} Hz")
    if sr.get("interval") is not None:
        parts.append(f"every {sr['interval']} s")
    if sr.get("dynamic"):
        parts.append("dynamic rate")
    if sr.get("configurable"):
        parts.append("user-configurable")
    return ", ".join(parts) if parts else json.dumps(sr)


def slugify(text: str) -> str:
    s = re.sub(r"[^a-zA-Z0-9]+", "-", text.strip().lower()).strip("-")
    return s or "section"


def github_blob(github_repo: str, rel_path: str) -> str:
    return f"https://github.com/{github_repo}/blob/master/{rel_path}"


def latest_armt_semver_file(active_dir: Path) -> Path | None:
    """Highest aRMT-X.Y.Z.yml under active/."""
    best: Path | None = None
    best_t: tuple[int, int, int] | None = None
    for p in active_dir.glob("aRMT-*.yml"):
        if p.name.lower().startswith("armt-healthkit"):
            continue
        m = re.match(r"aRMT-(\d+)\.(\d+)\.(\d+)\.yml$", p.name, re.I)
        if not m:
            continue
        t = (int(m.group(1)), int(m.group(2)), int(m.group(3)))
        if best_t is None or t > best_t:
            best_t = t
            best = p
    return best


def skip_armt_duplicate(path: Path, latest_armt: Path | None) -> bool:
    """Drop legacy aRMT YAML files; keep latest semver + aRMT-healthkit."""
    if path.parent.name != "active":
        return False
    low = path.name.lower()
    if not low.startswith("armt"):
        return False
    if low.startswith("armt-healthkit"):
        return False
    if latest_armt is not None and path.resolve() == latest_armt.resolve():
        return False
    return True


def normalize_data_entry(entry: dict[str, Any]) -> dict[str, Any]:
    input_topics = entry.get("input_topics")
    if input_topics and not isinstance(input_topics, list):
        input_topics = [input_topics]
    return {
        "type": entry.get("type"),
        "topic": entry.get("topic"),
        "input_topic": entry.get("input_topic"),
        "input_topics": input_topics or [],
        "windowed": entry.get("windowed"),
        "doc": entry.get("doc"),
        "sample_rate": entry.get("sample_rate"),
        "unit": entry.get("unit"),
        "processing_state": entry.get("processing_state"),
        "value_schema": entry.get("value_schema"),
        "questionnaire_definition_url": entry.get("questionnaire_definition_url"),
    }


def topic_line(n: dict[str, Any]) -> str:
    bits: list[str] = []
    if n.get("topic"):
        bits.append(str(n["topic"]))
    if n.get("input_topic"):
        bits.append(f"in: {n['input_topic']}")
    if n.get("input_topics"):
        bits.append("in: " + ", ".join(str(x) for x in n["input_topics"]))
    if n.get("windowed") is not None:
        bits.append(f"windowed: {n['windowed']}")
    return " · ".join(bits) if bits else "—"


def detail_line(n: dict[str, Any]) -> str:
    parts: list[str] = []
    if n.get("doc"):
        parts.append(str(n["doc"]))
    if n.get("questionnaire_definition_url"):
        parts.append(f"Definition: {n['questionnaire_definition_url']}")
    return " ".join(parts) if parts else "—"


def strip_version_suffix(stem: str) -> str:
    """e.g. android_phone-1.0.0 -> android_phone, radar-garmin-push-1.0.0 -> radar-garmin-push."""
    return re.sub(r"-\d+\.\d+\.\d+(-[a-z0-9]+)?$", "", stem, flags=re.I)


def source_key_and_title(path: Path, raw: dict[str, Any]) -> tuple[str, str]:
    """
    Return (stable_key, display_title) for grouping specifications into one data source.
    """
    stem = path.stem
    stem_l = stem.lower()
    blob = f"{stem_l} {raw.get('name', '')} {raw.get('vendor', '')} {raw.get('model', '')}".lower()

    def pair(key: str, title: str) -> tuple[str, str]:
        return (key, title)

    if stem_l.startswith("armt-healthkit"):
        return pair("armt-healthkit", "Apple Health")
    if stem_l.startswith("armt-"):
        return pair("armt", "aRMT")

    rules: list[tuple[str, str, str]] = [
        ("garmin", "Garmin", "garmin"),
        ("fitbit", "Fitbit", "fitbit"),
        ("oura", "Oura", "oura"),
        ("polar", "Polar", "polar"),
        ("empatica", "Empatica E4", "empatica-e4"),
        ("biovotion", "Biovotion", "biovotion"),
        ("ticwatch", "TicWatch", "ticwatch"),
        ("pebble", "Pebble", "pebble"),
        ("dreem", "Dreem", "dreem"),
        ("bittium", "Bittium Faros", "bittium"),
        ("oxford", "Oxford camera", "oxford"),
        ("altoida", "Altoida", "altoida"),
        ("axivity", "Axivity", "axivity"),
        ("physilog", "Physilog", "physilog"),
        ("android_phone_audio", "Android Phone (audio)", "android-phone-audio"),
        ("android_phone", "Android Phone", "android-phone"),
        ("android_google", "Android (Google APIs)", "android-google"),
        ("apple_sensorkit", "Apple SensorKit", "apple-sensorkit"),
        ("opensmile", "OpenSMILE audio", "opensmile"),
        ("carl-cloud", "Carl / cloud", "carl-cloud"),
        ("canedge", "CANedge", "canedge"),
        ("openweathermap", "OpenWeatherMap", "openweathermap"),
        ("rateaf", "RateAF PPG", "rateaf"),
        ("thincit", "THINC-IT", "thincit"),
        ("banking-app", "Banking app", "banking-app"),
        ("radar_prmt", "pRMT (monitoring)", "radar-prmt"),
        ("statistics", "Stream statistics", "statistics"),
    ]
    for needle, title, key in rules:
        if needle in blob or needle in stem_l:
            return pair(key, title)

    base = strip_version_suffix(stem_l)
    base = re.sub(r"^radar-", "", base)
    base = re.sub(r"-connector.*$", "", base)
    title = base.replace("_", " ").replace("-", " ").strip()
    if title:
        t = title[0].upper() + title[1:]
        return (slugify(base), t)
    return ("source-" + slugify(stem), stem)


def apple_health_types_section(raw: dict[str, Any]) -> str:
    """Extra UI block: HealthKit sample types from the aRMT HealthKit YAML (generic Avro schema)."""
    items: list[str] = []
    for entry in raw.get("data") or []:
        if not isinstance(entry, dict):
            continue
        topic = str(entry.get("topic") or "")
        doc = str(entry.get("doc") or "").strip()
        suffix = topic
        if "active_apple_healthkit_" in topic:
            suffix = topic.split("active_apple_healthkit_", 1)[-1]
        label = suffix.replace("_", " ").strip().title() or topic
        items.append(f"<li><strong>{html.escape(label)}</strong> — {html.escape(doc)}</li>")
    if not items:
        return ""
    return (
        '<div class="apple-health-block">'
        "<p class=\"apple-health-lead\">"
        "Data from the <strong>Apple Health</strong> app (HealthKit) is published using a single "
        "Avro value type <code>HealthKitTypedData</code>; each row below is a distinct sample "
        "category (Kafka topic). Values use the appropriate typed slots in that record "
        "(e.g. <code>doubleValue</code>, <code>floatValue</code>, <code>stringValue</code>) "
        "depending on the HealthKit quantity or category type."
        "</p>"
        '<p class="muted small">HealthKit-aligned types covered in this catalog:</p>'
        '<ul class="apple-health-types">'
        + "".join(items)
        + "</ul></div>"
    )


@dataclass
class MeasurementRow:
    measurement: str
    schema_short: str
    schema_full: str | None
    topics: str
    sample_rate: str
    unit: str
    processing: str
    notes: str


def collect_specs(spec_root: Path, active_dir: Path) -> list[tuple[str, Path, dict[str, Any]]]:
    latest = latest_armt_semver_file(active_dir)
    out: list[tuple[str, Path, dict[str, Any]]] = []
    for cat in SPEC_CATEGORIES:
        d = spec_root / cat
        if not d.is_dir():
            continue
        for yml in sorted(d.glob("*.yml")):
            if skip_armt_duplicate(yml, latest):
                continue
            try:
                raw = yaml.safe_load(yml.read_text(encoding="utf-8"))
            except (yaml.YAMLError, OSError) as e:
                print(f"Warning: skip {yml}: {e}", file=sys.stderr)
                continue
            if isinstance(raw, dict):
                out.append((cat, yml, raw))
    return out


def rows_for_spec(raw: dict[str, Any]) -> list[MeasurementRow]:
    rows: list[MeasurementRow] = []
    master = raw.get("master")
    if isinstance(master, str):
        full = expand_class(master)
        short = master.split(".")[-1] if master else "—"
        rows.append(
            MeasurementRow(
                measurement="Stream master",
                schema_short=short,
                schema_full=full,
                topics=raw.get("name") or "—",
                sample_rate="—",
                unit="—",
                processing="—",
                notes=raw.get("doc") or "Stream aggregation definition.",
            )
        )

    for entry in raw.get("data") or []:
        if not isinstance(entry, dict):
            continue
        n = normalize_data_entry(entry)
        vs_raw = n.get("value_schema")
        full = expand_class(vs_raw)
        short = (vs_raw or "").split(".")[-1] if vs_raw else "—"
        mt = str(n["type"]) if n.get("type") else "—"
        rows.append(
            MeasurementRow(
                measurement=mt,
                schema_short=short,
                schema_full=full,
                topics=topic_line(n),
                sample_rate=format_sample_rate_plain(n.get("sample_rate")),
                unit=str(n["unit"]) if n.get("unit") is not None else "—",
                processing=str(n["processing_state"]) if n.get("processing_state") is not None else "—",
                notes=detail_line(n),
            )
        )
    return rows


def merge_field_rows(
    full_names: set[str | None],
    schemas: dict[str, dict[str, Any]],
) -> list[tuple[str, str, str]]:
    """
    Unique field names across schemas; IGNORE_FIELD_NAMES skipped.
    Returns list of (name, merged_types, merged_docs).
    """
    by_name: dict[str, dict[str, set[str]]] = {}
    for fn in full_names:
        if not fn or fn not in schemas:
            continue
        sch = schemas[fn]
        if sch.get("type") != "record":
            continue
        for f in sch.get("fields") or []:
            fname = str(f.get("name", ""))
            if fname in IGNORE_FIELD_NAMES:
                continue
            slot = by_name.setdefault(fname, {"types": set(), "docs": set()})
            slot["types"].add(format_avro_type(f.get("type")))
            doc = (f.get("doc") or "").strip()
            if doc:
                slot["docs"].add(doc)

    out: list[tuple[str, str, str]] = []
    for fname in sorted(by_name.keys()):
        types = " | ".join(sorted(by_name[fname]["types"]))
        docs = by_name[fname]["docs"]
        doc_str = " · ".join(sorted(docs)) if docs else "—"
        out.append((fname, types, doc_str))
    return out


def is_blank_cell(val: str | None) -> bool:
    if val is None:
        return True
    t = str(val).strip()
    return t == "" or t == "—"


def measurement_column_visibility(rows: list[MeasurementRow]) -> dict[str, bool]:
    """Per-source: include column only if at least one row has a non-placeholder value."""
    if not rows:
        return {
            "measurement": True,
            "schema": True,
            "topics": True,
            "sample_rate": True,
            "unit": True,
            "processing": True,
            "notes": True,
        }
    vis = {
        "measurement": any(not is_blank_cell(r.measurement) for r in rows),
        "schema": any(not is_blank_cell(r.schema_short) for r in rows),
        "topics": any(not is_blank_cell(r.topics) for r in rows),
        "sample_rate": any(not is_blank_cell(r.sample_rate) for r in rows),
        "unit": any(not is_blank_cell(r.unit) for r in rows),
        "processing": any(not is_blank_cell(r.processing) for r in rows),
        "notes": any(not is_blank_cell(r.notes) for r in rows),
    }
    if not any(vis.values()):
        vis["measurement"] = True
        vis["schema"] = True
    return vis


def merged_fields_column_visibility(rows: list[tuple[str, str, str]]) -> tuple[bool, bool]:
    """Returns (show_avro_type, show_description). Field name column is always shown if rows non-empty."""
    if not rows:
        return (True, True)
    show_types = any(not is_blank_cell(t) for _, t, _ in rows)
    show_doc = any(not is_blank_cell(d) for _, _, d in rows)
    return (show_types, show_doc)


def render_merged_fields_table(rows: list[tuple[str, str, str]]) -> str:
    if not rows:
        return "<p class='muted'>No payload fields after omitting common metadata.</p>"
    show_types, show_doc = merged_fields_column_visibility(rows)
    headers: list[str] = ["<th>Field</th>"]
    if show_types:
        headers.append("<th>Avro type</th>")
    if show_doc:
        headers.append("<th>Description</th>")
    body: list[str] = []
    for name, types, doc in rows:
        tds = [f"<td><code>{html.escape(name)}</code></td>"]
        if show_types:
            tds.append(f"<td><code>{html.escape(types)}</code></td>")
        if show_doc:
            tds.append(f"<td>{html.escape(doc)}</td>")
        body.append("<tr>" + "".join(tds) + "</tr>")
    return (
        "<table class='fields'><thead><tr>"
        + "".join(headers)
        + "</tr></thead><tbody>"
        + "".join(body)
        + "</tbody></table>"
    )


def render_measurements_table(rows: list[MeasurementRow], spec_links: list[str]) -> str:
    vis = measurement_column_visibility(rows)
    headers: list[str] = []
    if vis["measurement"]:
        headers.append('<th class="col-measurement">Measurement</th>')
    if vis["schema"]:
        headers.append('<th class="col-schema">Value schema</th>')
    if vis["topics"]:
        headers.append('<th class="col-topics">Kafka / stream</th>')
    if vis["sample_rate"]:
        headers.append('<th class="col-sample-rate">Sampling</th>')
    if vis["unit"]:
        headers.append('<th class="col-unit">Unit</th>')
    if vis["processing"]:
        headers.append('<th class="col-processing">Processing</th>')
    if vis["notes"]:
        headers.append('<th class="col-notes">Notes</th>')

    body: list[str] = []
    for r in rows:
        tds: list[str] = []
        if vis["measurement"]:
            tds.append(f'<td class="col-measurement">{html.escape(r.measurement)}</td>')
        if vis["schema"]:
            schema_cell = html.escape(r.schema_short)
            if r.schema_full:
                schema_cell = (
                    f'<code title="{html.escape(r.schema_full)}">{schema_cell}</code>'
                )
            tds.append(f'<td class="col-schema">{schema_cell}</td>')
        if vis["topics"]:
            tds.append(
                f'<td class="col-topics topics"><code>{html.escape(r.topics)}</code></td>'
            )
        if vis["sample_rate"]:
            tds.append(f'<td class="col-sample-rate">{html.escape(r.sample_rate)}</td>')
        if vis["unit"]:
            tds.append(f'<td class="col-unit">{html.escape(r.unit)}</td>')
        if vis["processing"]:
            tds.append(f'<td class="col-processing">{html.escape(r.processing)}</td>')
        if vis["notes"]:
            tds.append(f'<td class="col-notes notes">{html.escape(r.notes)}</td>')
        body.append("<tr>" + "".join(tds) + "</tr>")

    specs_note = ""
    if spec_links:
        specs_note = (
            "<p class='spec-files'><strong>Specifications:</strong> "
            + ", ".join(spec_links)
            + "</p>"
        )
    thead = "<table class='measurements'><thead><tr>" + "".join(headers) + "</tr></thead><tbody>"
    return specs_note + thead + "".join(body) + "</tbody></table>"


def section_sort_key(key: str) -> tuple[int, str]:
    if key in SECTION_PRIORITY:
        return (SECTION_PRIORITY.index(key), key)
    return (len(SECTION_PRIORITY), key)


def generate_html(
    repo_root: Path,
    commons: Path,
    specifications: Path,
    out_dir: Path,
    github_repo: str,
) -> None:
    schemas, schema_paths = load_avro_schemas(commons)
    active_dir = specifications / "active"
    specs = collect_specs(specifications, active_dir)

    # Group: source_key -> list of (cat, path, raw)
    grouped: dict[str, list[tuple[str, Path, dict[str, Any]]]] = defaultdict(list)
    titles: dict[str, str] = {}
    for cat, yml_path, raw in specs:
        k, title = source_key_and_title(yml_path, raw)
        grouped[k].append((cat, yml_path, raw))
        titles[k] = title

    referenced: set[str] = set()
    sections_html: list[str] = []
    nav_items: list[str] = []

    for key in sorted(grouped.keys(), key=section_sort_key):
        group = grouped[key]
        title = titles[key]
        sid = f"source-{slugify(key)}"

        spec_links: list[str] = []
        all_rows: list[MeasurementRow] = []
        schema_names: set[str | None] = set()

        for spec_cat, yml_path, raw in sorted(group, key=lambda x: x[1].as_posix()):
            rel = yml_path.relative_to(repo_root).as_posix()
            url = github_blob(github_repo, rel)
            spec_links.append(f'<a href="{html.escape(url)}">{html.escape(rel)}</a>')
            for row in rows_for_spec(raw):
                all_rows.append(row)
                schema_names.add(row.schema_full)
                if row.schema_full:
                    referenced.add(row.schema_full)

        for fn in list(schema_names):
            if fn:
                referenced.add(fn)

        # Dedupe measurement rows (identical lines from merged specs)
        seen_m: set[tuple[str, ...]] = set()
        deduped_rows: list[MeasurementRow] = []
        for r in all_rows:
            sig = (r.measurement, r.schema_full or "", r.topics, r.notes)
            if sig in seen_m:
                continue
            seen_m.add(sig)
            deduped_rows.append(r)

        merged_fields = merge_field_rows(schema_names, schemas)
        search_extra = ""
        apple_block = ""
        if key == "armt-healthkit":
            hk_raw = next((r for _, _, r in group), None)
            if isinstance(hk_raw, dict):
                apple_block = apple_health_types_section(hk_raw)
                for entry in hk_raw.get("data") or []:
                    if isinstance(entry, dict):
                        search_extra += " " + str(entry.get("topic", "")) + " " + str(entry.get("doc", ""))

        search_text = " ".join(
            [title, key, search_extra]
            + [r.measurement + r.notes + r.topics for r in deduped_rows]
            + [t[0] + t[2] for t in merged_fields]
        ).lower()

        nav_items.append(
            f'<li><a href="#{sid}">{html.escape(title)} '
            f'<span class="cnt">{len(deduped_rows)}</span></a></li>'
        )

        intro = (
            "<p class=\"muted\">Data types and sampling as declared in RADAR-base specifications for this source.</p>"
            if key != "armt-healthkit"
            else "<p class=\"muted\">Apple Health (HealthKit) samples and topics; value payloads use the shared schema below.</p>"
        )

        sections_html.append(
            f"""
<section class="source-block" id="{sid}" data-search="{html.escape(search_text, quote=True)}">
  <header class="source-header">
    <h2>{html.escape(title)}</h2>
    {intro}
  </header>
  {apple_block}
  {render_measurements_table(deduped_rows, spec_links)}
  <h3 class="subh">Payload fields (merged)</h3>
  <p class="muted small">Unique field names across all value schemas for this source. Omitted: {", ".join(sorted(IGNORE_FIELD_NAMES))}.</p>
  {render_merged_fields_table(merged_fields)}
</section>
"""
        )

    unref = sorted(set(schemas.keys()) - referenced)
    appendix = ""
    if unref:
        appendix = f"""
<section class="source-block" id="appendix-other-schemas" data-search="unreferenced schemas">
  <h2>Other Avro definitions</h2>
  <p class="muted">These records exist under <code>commons/</code> but are not used as a value schema in the specifications shown above (nested types, keys, or legacy).</p>
  <ul class="compact">{
    "".join(f"<li><code>{html.escape(n)}</code></li>" for n in unref[:400])
  }{f"<li>… and {len(unref) - 400} more</li>" if len(unref) > 400 else ""}</ul>
</section>
"""

    total_sources = len(grouped)
    total_specs = len(specs)

    html_doc = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>RADAR-base — data sources &amp; payloads</title>
  <style>
    :root {{
      --bg: #f4f6f8;
      --surface: #ffffff;
      --border: #d8dee4;
      --text: #1a1f24;
      --muted: #5c6a76;
      --accent: #0b6bcb;
      --accent-soft: #e8f1fc;
    }}
    * {{ box-sizing: border-box; }}
    body {{
      margin: 0;
      font-family: "Segoe UI", system-ui, -apple-system, Roboto, sans-serif;
      line-height: 1.55;
      color: var(--text);
      background: var(--bg);
      font-size: 15px;
    }}
    .hero {{
      background: linear-gradient(135deg, #0b3d6d 0%, #0b6bcb 55%, #1a8cff 100%);
      color: #fff;
      padding: 2rem 1.5rem 2.25rem;
    }}
    .hero-inner {{ max-width: 1100px; margin: 0 auto; }}
    .hero h1 {{ margin: 0 0 0.5rem; font-size: 1.65rem; font-weight: 700; }}
    .hero p {{ margin: 0; max-width: 52rem; opacity: 0.95; }}
    .hero .stats {{ display: flex; flex-wrap: wrap; gap: 1rem; margin-top: 1.25rem; }}
    .stat {{ background: rgba(255,255,255,0.12); border-radius: 8px; padding: 0.5rem 0.9rem; font-size: 0.9rem; }}
    .stat strong {{ font-size: 1.1rem; display: block; }}
    .search-row {{ max-width: 1100px; margin: -1rem auto 0; padding: 0 1.25rem; position: relative; z-index: 2; }}
    #q {{
      width: 100%;
      padding: 0.75rem 1rem;
      border: 1px solid var(--border);
      border-radius: 10px;
      font-size: 1rem;
      box-shadow: 0 4px 20px rgba(0,0,0,0.08);
    }}
    .layout {{
      display: grid;
      grid-template-columns: 260px minmax(0, 1fr);
      gap: 1.75rem;
      max-width: 1100px;
      margin: 0 auto;
      padding: 1.75rem 1.25rem 3rem;
    }}
    @media (max-width: 880px) {{
      .layout {{ grid-template-columns: 1fr; }}
      nav.toc {{ position: static; }}
    }}
    nav.toc {{
      position: sticky;
      top: 1rem;
      align-self: start;
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 0.9rem 1rem;
      font-size: 0.88rem;
      max-height: calc(100vh - 2rem);
      overflow: auto;
    }}
    nav.toc strong {{
      display: block;
      margin-bottom: 0.5rem;
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.06em;
      color: var(--muted);
    }}
    nav.toc ul {{ list-style: none; padding: 0; margin: 0; }}
    nav.toc li {{ margin: 0.35rem 0; }}
    nav.toc .cnt {{
      display: inline-block;
      background: var(--accent-soft);
      color: var(--accent);
      border-radius: 999px;
      padding: 0 0.4rem;
      font-size: 0.75rem;
      font-weight: 600;
    }}
    .source-block {{
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 1.1rem 1.2rem 1.35rem;
      margin-bottom: 1.5rem;
    }}
    .source-header h2 {{ margin: 0 0 0.35rem; font-size: 1.2rem; }}
    .subh {{ margin: 1.25rem 0 0.35rem; font-size: 1rem; }}
    .small {{ font-size: 0.88rem; margin: 0 0 0.75rem; }}
    .spec-files {{ font-size: 0.88rem; margin: 0 0 0.75rem; }}
    .apple-health-block {{
      margin: 0 0 1rem;
      padding: 0.85rem 1rem;
      background: #f1f6fc;
      border: 1px solid var(--border);
      border-radius: 8px;
    }}
    .apple-health-lead {{ margin: 0 0 0.5rem; font-size: 0.95rem; }}
    .apple-health-block .small {{ margin-bottom: 0.5rem; }}
    ul.apple-health-types {{
      margin: 0;
      padding-left: 1.2rem;
      font-size: 0.88rem;
      columns: 2;
      column-gap: 1.5rem;
    }}
    @media (max-width: 700px) {{
      ul.apple-health-types {{ columns: 1; }}
    }}
    ul.apple-health-types li {{ break-inside: avoid; margin: 0.25rem 0; }}
    table.measurements, table.fields {{
      width: 100%;
      border-collapse: collapse;
      font-size: 0.82rem;
      margin: 0.5rem 0 0.75rem;
    }}
    table.measurements {{
      table-layout: fixed;
    }}
    table.measurements .col-measurement {{ width: 5.25rem; max-width: 7%; }}
    table.measurements .col-schema {{ width: 9%; min-width: 4.5rem; }}
    table.measurements .col-topics {{ width: 12%; min-width: 6rem; }}
    table.measurements .col-sample-rate {{ width: 6.5rem; }}
    table.measurements .col-unit {{ width: 4rem; }}
    table.measurements .col-processing {{ width: 6.5rem; }}
    table.measurements .col-notes {{ width: auto; min-width: 0; }}
    table.measurements th, table.measurements td {{
      border: 1px solid var(--border);
      padding: 0.45rem 0.5rem;
      vertical-align: top;
      text-align: left;
      min-width: 0;
      overflow-wrap: anywhere;
      word-wrap: break-word;
      word-break: break-word;
      hyphens: auto;
    }}
    table.fields th, table.fields td {{
      border: 1px solid var(--border);
      padding: 0.45rem 0.5rem;
      vertical-align: top;
      text-align: left;
    }}
    table.measurements thead, table.fields thead {{ background: #eef2f6; }}
    table.measurements code {{
      white-space: pre-wrap;
      word-break: break-word;
      overflow-wrap: anywhere;
    }}
    table.fields td:last-child {{
      overflow-wrap: anywhere;
      word-break: break-word;
    }}
    code {{ font-size: 0.85em; }}
    .muted {{ color: var(--muted); }}
    a {{ color: var(--accent); text-decoration: none; }}
    a:hover {{ text-decoration: underline; }}
    ul.compact {{ columns: 2; font-size: 0.85rem; }}
    .source-block.hidden {{ display: none; }}
    .visually-hidden {{
      position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px;
      overflow: hidden; clip: rect(0,0,0,0); border: 0;
    }}
  </style>
</head>
<body>
  <div class="hero">
    <div class="hero-inner">
      <h1>RADAR-base data sources</h1>
      <p>
        Each section is one <strong>data source</strong> (device family, app, or integration).
        The first table lists measurements and how they are sampled; the second merges <strong>payload fields</strong>
        from all Avro value types for that source (duplicate metadata fields are hidden).
        Questionnaire coverage uses the <strong>latest aRMT</strong> specification only.
      </p>
      <div class="stats">
        <div class="stat"><strong>{total_sources}</strong> data sources</div>
        <div class="stat"><strong>{total_specs}</strong> specification files (after aRMT deduplication)</div>
      </div>
    </div>
  </div>
  <div class="search-row">
    <label class="visually-hidden" for="q">Filter sources</label>
    <input type="search" id="q" placeholder="Filter by source, measurement, topic…" autocomplete="off"/>
  </div>
  <div class="layout">
    <nav class="toc" aria-label="Data sources">
      <strong>Data sources</strong>
      <ul>{"".join(nav_items)}<li><a href="#appendix-other-schemas">Other schemas</a></li></ul>
    </nav>
    <main>
      {"".join(sections_html)}
      {appendix}
    </main>
  </div>
  <script>
    (function () {{
      var input = document.getElementById('q');
      if (!input) return;
      function apply() {{
        var q = (input.value || '').toLowerCase().trim();
        document.querySelectorAll('.source-block').forEach(function (el) {{
          var hay = el.getAttribute('data-search') || '';
          var show = !q || hay.indexOf(q) !== -1;
          el.classList.toggle('hidden', !show);
        }});
      }}
      input.addEventListener('input', apply);
      input.addEventListener('search', apply);
    }})();
  </script>
</body>
</html>
"""

    out_dir.mkdir(parents=True, exist_ok=True)
    (out_dir / ".nojekyll").write_text("", encoding="utf-8")
    (out_dir / "index.html").write_text(html_doc, encoding="utf-8")
    print(
        f"Wrote {out_dir / 'index.html'} "
        f"({total_sources} sources, {total_specs} spec files, {len(schemas)} Avro records)"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
        help="Repository root (default: parent of scripts/)",
    )
    parser.add_argument(
        "--out",
        type=Path,
        default=Path("_site"),
        help="Output directory for static site (default: _site)",
    )
    parser.add_argument(
        "--github-repo",
        default=os.environ.get("GITHUB_REPOSITORY", "RADAR-base/RADAR-Schemas"),
        help="owner/name for GitHub source links",
    )
    args = parser.parse_args()
    repo = args.repo_root.resolve()
    generate_html(
        repo,
        repo / "commons",
        repo / "specifications",
        args.out.resolve(),
        args.github_repo,
    )


if __name__ == "__main__":
    main()
