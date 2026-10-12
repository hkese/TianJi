import argparse
import json
from pathlib import Path
from .data import dataset, import_file
from .experiment import evaluate
from .registry import register
from .synthetic import synthetic_events

def main():
    parser = argparse.ArgumentParser(description="TianJi exploratory research; no validated predictions")
    sub = parser.add_subparsers(dest="command", required=True)
    for command in ("demo", "import", "evaluate"):
        p = sub.add_parser(command)
        p.add_argument("--output", type=Path, default=Path("output"))
        p.add_argument("--seed", type=int, default=42)
        if command != "demo":
            p.add_argument("file", type=Path)
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    if args.command == "demo":
        events = synthetic_events(seed=args.seed)
        _, body = dataset(events)
        (args.output / "SYNTHETIC-events.jsonl").write_text(body)
    else:
        digest, events = import_file(args.file, args.output / "datasets")
        if args.command == "import":
            print(json.dumps({"dataset_hash": digest, "records": len(events)}))
            return
    report = evaluate(events, args.seed)
    (args.output / "report.json").write_text(json.dumps(report, indent=2))
    register(report, args.output / "models")
    print(json.dumps({"run_id": report["run_id"], "synthetic": report["synthetic"], "deployable": False}))

if __name__ == "__main__":
    main()
