import json
import os
from pydantic import BaseModel
from typing import List

class TestCase(BaseModel):
    name: str
    description: str
    steps: list[str]
    url: str
    expected_result: str

DEFAULT_FILENAME = "test_cases.json"

# Ensure file exists and contains valid JSON
def ensure_valid_json_file(filename: str = DEFAULT_FILENAME):
    if not os.path.exists(filename) or os.stat(filename).st_size == 0:
        print("[Info] test_cases.json is missing or empty. Creating a new one.")
        with open(filename, "w", encoding="utf-8") as f:
            json.dump([], f, indent=4)
    else:
        try:
            with open(filename, "r", encoding="utf-8") as f:
                json.load(f)
        except json.JSONDecodeError:
            print("[Warning] Invalid JSON. Reinitializing test_cases.json.")
            with open(filename, "w", encoding="utf-8") as f:
                json.dump([], f, indent=4)

# Call it once at the top
ensure_valid_json_file()

# Now it's safe to load
def load_test_cases(filename: str = DEFAULT_FILENAME) -> List[TestCase]:
    """Load test cases from a JSON file dynamically."""
    with open(filename, "r", encoding="utf-8") as file:
        data = json.load(file)
    return [TestCase(**item) for item in data]