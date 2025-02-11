#!/usr/bin/env python3

import os
import openai
import glob
import json
import concurrent.futures
from dotenv import load_dotenv
from pydantic import BaseModel

load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")

if not api_key:
    print(json.dumps({"ok": False, "reason": "Missing API key"}))
    exit(1)

client = openai.OpenAI(api_key=api_key)


class Offender(BaseModel):
    method: str
    complexity: int


class ComplexityCheckResult(BaseModel):
    ok: bool
    offenders: list[Offender]


SYSTEM_MESSAGE = """
Analyze Java code for cyclomatic complexity per method and return JSON:
{"ok": true/false, "offenders": [{"method": "methodName", "complexity": int}, ...]}

Start count at 0, reset at the start of each method.

### Complexity Calculation Rules:
- Control flow constructs (`if`, `else`, `switch`, `case`, loops, `catch`, `?:`, recursion): **+1**
- Nested control flow (within another `if`, loop, etc.): **+1 per level**
- Methods with a **complexity of 4 or less** **do not** get flagged.
- Streams (`.stream()`) & method references (`::`) **do NOT add complexity** unless they contain explicit branching (e.g., `if` inside a `.map()`).
- Methods with **no branching constructs** have **0 complexity** and should not be flagged.

### Additional Considerations:
- Assertion statements (`assert`) do **NOT** add complexity.
- Stream methods like `.reduce()`, `.map()`, `.filter()` do **NOT** add complexity
- Methods like orElse do **NOT** add complexity.
- != == do not add complexity, only && and ||
- If no methods exceed the complexity threshold, return: `{"ok": true, "offenders": []}`.
- Only flag methods that **exceed** the complexity threshold.

Ensure strict adherence to these rules to minimize false positives. Return only a structured JSON response.
"""


def check_complexity(file):
    try:
        with open(file, "r", encoding="utf-8") as f:
            code = f.read()

        response = client.beta.chat.completions.parse(
            model="gpt-4o",
            messages=[
                {"role": "system", "content": SYSTEM_MESSAGE},
                {"role": "user", "content": code},
            ],
            response_format=ComplexityCheckResult,
        )

        return response.choices[0].message.parsed
    except Exception as e:
        return ComplexityCheckResult(
            ok=False, offenders=[Offender(method="Unknown", complexity=0)]
        )


java_files = glob.glob("**/*.java", recursive=True)
java_files = filter(lambda f: "Test" not in f, java_files)

results = {}
with concurrent.futures.ThreadPoolExecutor() as executor:
    future_to_file = {
        executor.submit(check_complexity, file): file for file in java_files
    }
    for future in concurrent.futures.as_completed(future_to_file):
        file = future_to_file[future]
        try:
            results[file] = future.result().model_dump()
        except Exception as e:
            results[file] = ComplexityCheckResult(
                ok=False, offenders=[Offender(method="Unknown", complexity=0)]
            ).model_dump()

print(json.dumps(results, indent=2))

if any(len(res["offenders"]) > 0 for res in results.values()):
    exit(1)
