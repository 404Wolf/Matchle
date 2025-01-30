#!/usr/bin/env python3

import os
import openai
import json
from dotenv import load_dotenv
import glob
import sys

# Load environment variables from .env
load_dotenv()

# Get the API key
api_key = os.getenv("OPENAI_API_KEY")
if not api_key:
    print("Error: OPENAI_API_KEY is not set in .env")
    sys.exit(1)

# Configure OpenAI SDK
openai.api_key = api_key

# System message for complexity check
SYSTEM_MESSAGE = "Respond YES if any routine has complexity >4 or contains repeated code. Otherwise, respond NO."

def check_code_complexity(file_path):
    """Reads a Java file and checks for complexity using OpenAI API."""
    try:
        with open(file_path, "r", encoding="utf-8") as f:
            code_content = f.read()

        response = openai.ChatCompletion.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": SYSTEM_MESSAGE},
                {"role": "user", "content": code_content}
            ],
            temperature=0
        )

        # Extract response content
        reply = response["choices"][0]["message"]["content"].strip()

        return reply
    except Exception as e:
        print(f"Error processing {file_path}: {e}")
        return None

# Find all Java files
java_files = glob.glob("**/*.java", recursive=True)

# Check each file
for file in java_files:
    result = check_code_complexity(file)
    
    if result is None:
        print(f"Error: No response received for {file}")
        sys.exit(1)
    
    if result == "YES":
        print(f"Error in {file}: Complexity too high or repeated code detected.")
        sys.exit(1)

print("All files passed the complexity check.")

