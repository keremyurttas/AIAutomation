import asyncio
from ai_agent import AI_TestAgent
from browser_actions import controller
from test_cases.test_cases import GOOGLE_TEST_CASES
from result_validator import validate_result
import json
import os

async def run_test_and_generate_code(test_case):
    """Run a single test, save results in the results folder, and generate Java code"""
    agent = AI_TestAgent(controller)
    
    # Run the test
    actual_result = await agent.run_test(test_case)
    
    # Save the test results to a JSON file inside the results folder
    formatted_output = actual_result.model_actions()  # This should be a dictionary or list
    print(actual_result.action_names())

    # Ensure the "results" folder exists
    results_dir = "case_actions"
    os.makedirs(results_dir, exist_ok=True)
    
    json_file_path = os.path.join(results_dir, f"{test_case.name}.json")

    # Write the formatted_output to a JSON file with UTF-8 encoding
    with open(json_file_path, "w", encoding="utf-8") as json_file:
        json.dump(formatted_output, json_file, indent=4, ensure_ascii=False, default=str)  # Convert unknown objects to strings

    print(f"{test_case.name}.json file successfully written in {results_dir}/.")

    # Generate Java code using the same agent instance (which has the current_test_case set)
    try:
        result = agent.generate_test_from_json(json_file_path)
        print(result)
    except Exception as e:
        print(f"Error generating test from JSON: {e}")
    
    return actual_result  # or validated_result if validation is needed

async def run_tests():
    """Run tests concurrently"""
    tasks = [run_test_and_generate_code(test) for test in GOOGLE_TEST_CASES]
    results = await asyncio.gather(*tasks)
    
    print("\nAll tests executed. Here are the results:\n")
    print("=" * 50)    
    
    # Additional reporting or processing of results can be done here

if __name__=="__main__":
    asyncio.run(run_tests())