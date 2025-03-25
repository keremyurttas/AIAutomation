import json
import os
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import SecretStr

from utils import class_name_generator

class JavaCodeGenerator:
    def __init__(self, llm=None):
        """Initialize the Java code generator with an optional LLM"""
        self.llm = llm
        if not llm:
            # Create a default LLM if none is provided
            self.llm = ChatGoogleGenerativeAI(
                model="gemini-2.0-flash-exp",
                temperature=0.2,
                api_key=SecretStr(os.getenv("GEMINI_API_KEY"))
            )
    
    def send_request_to_llm(self, prompt: str):
        """Send a request to the LLM and return the response"""
        try:
            response = self.llm.invoke(prompt)

            # Extract content if it's in an object
            if hasattr(response, 'content'):
                response = response.content
            elif hasattr(response, 'text'):
                response = response.text
            elif isinstance(response, list) and len(response) > 0:
                first_message = response[0]
                if hasattr(first_message, 'content'):
                    response = first_message.content
                elif hasattr(first_message, 'text'):
                    response = first_message.text

            # Ensure it's a string
            if isinstance(response, str):
                # Remove Markdown Java code block markers (```java ... ```)
                response = response.strip("```java").strip("```").strip()

                return response
            else:
                print(f"Unexpected response type: {type(response)}")
                return str(response)
        except Exception as e:
            print(f"Error sending request: {e}")
            return None


    def generate_test_from_json(self, json_file: str, test_case=None):
        """Generate Java Selenium TestNG tests from JSON and save to a file"""
        try:
            with open(json_file, 'r', encoding='utf-8') as file:
                test_data = json.load(file)

            # Create the prompt for generating Java code
            prompt = self.create_prompt_from_json(test_data, test_case)

            # Generate Java code (Assuming you have an LLM function that processes the prompt)
            java_test_code = self.generate_code_from_data(prompt,test_case)

            if isinstance(java_test_code, str) and java_test_code:
                formatted_name = "".join(word.capitalize() for word in test_case.name.split()) + ".java"

                # Save the generated Java test file
                with open(f"generated_codes/{formatted_name}", "w", encoding='utf-8') as test_file:
                    test_file.write(java_test_code)

                print(f"Java test generated successfully and saved to generated_codes/{formatted_name}")
                return "Java test generated successfully."
            else:
                error_msg = f"Failed to generate the Java test: Received {type(java_test_code)} response."
                print(error_msg)
                return error_msg

        except Exception as e:
            error_msg = f"Error in generate_test_from_json: {str(e)}"
            print(error_msg)
            return error_msg
    
    def generate_code_from_data(self, test_data: dict, test_case=None):
        """Generate Java test code from test data dictionary"""
        # Create the prompt
        prompt = self.create_prompt_from_json(test_data, test_case)
        
        # Get the code from the LLM
        return self.send_request_to_llm(prompt)

 

    def create_prompt_from_json(self, test_data: dict, test_case=None):
        """Create a prompt to guide the LLM to generate a highly robust Java Selenium TestNG test file from JSON test data."""
        
        # Determine the Java class name
        formatted_class_name = (
            "".join(word.capitalize() for word in test_case.name.split())
            if test_case and hasattr(test_case, 'name')
            else "GeneratedTest"
        )
        
        # General prompt for any website testing
        prompt = f"""
        Generate a single complete Java file named `{formatted_class_name}.java` implementing a Selenium TestNG test based on this JSON test data:

        {json.dumps(test_data, indent=4, ensure_ascii=False)}

        Requirements:
        1. Include all necessary imports: Selenium, TestNG, WebDriverManager.
        2. Use WebDriverManager for Chrome initialization and set the browser window size to 1920x1080 for consistency.
        3. Clear cookies before each test to ensure independence.
        4. Implement a custom findElement method that tries to locate elements using id, then css, then xpath, and throws a meaningful exception if not found.
        5. For critical actions (like clicking important buttons), use multiple specific selectors in order, and fallback to JavaScript click if the element is not interactable.
        6. Handle modals and overlays:
        - Before interactions, check for and dismiss any modals or overlays that might block the action.
        - After dismissing, wait a short period (e.g., 2 seconds) and verify that the modal or overlay is no longer present.
        - If necessary, scroll to the relevant section after closing modals.
        7. Use explicit waits with WebDriverWait and appropriate expected conditions (e.g., presence_of_element_located, visibility_of_element_located, element_to_be_clickable) with a reasonable timeout (e.g., 15 seconds).
        8. Add resilience features:
        - Use JavaScript executor fallbacks for failed interactions.
        - Capture screenshots on failure using a TestNG listener.
        - Implement TestNG retry analyzer for 3 attempts to handle flaky tests.
        - Include comprehensive error handling and logging for debugging.
        9. Handle language differences:
        - Ensure all expected strings in assertions match the website's language as specified in the JSON data or by default.
        - If the website supports multiple languages, set the language appropriately before testing.
        10. Handle captcha validation:
            - Check for the presence of a captcha element.
            - If a captcha is detected, log a message, have a timeout, tester will solve it manually.
        11. Utilize the Page Object Model:
            - Create page classes for actions specified in the JSON data.
            - Each method should use the custom findElement and appropriate waits.
        12. Verify expected results after each action in the JSON data.
        13. Ensure the code is complete and executable without modifications.

        The generated test should handle dynamic content and ensure reliable execution for any website.
        The folowwings are test case steps:
        """
        
        # Add test steps if provided
        if test_case and hasattr(test_case, 'steps') and test_case.steps:
            prompt += "\nImplement these specific steps:\n" + \
                    "\n".join(f"- {step}" for step in test_case.steps)
        
        return prompt
    
    
    
    
    