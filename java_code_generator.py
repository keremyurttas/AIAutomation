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
        
        # Enhanced prompt with more detailed element handling and screenshot capture
        prompt = f"""
    Generate a single complete Java file named `{formatted_class_name}.java` implementing a Selenium TestNG test based on this JSON test data:

    {json.dumps(test_data, indent=4, ensure_ascii=False)}
    ## Important Instructions:
    ### ⚠ Mandatory Usage of Utility Methods
    Instead of 'element.click' always use clickWebElementForTpath(By locator)
    Instead of 'element.sendKeys(value)' always use sendKeysElementTPath(By locator, boolean clearInput, String value)
    
    **⚠ DO NOT define `clickWebElementForTpath` and `sendKeysElementTPath`. These are already implemented in `SmaClickUtilities` and `SmaSendKeyUtilites`. Simply import and use them in the click and sendKey events!!.**  
    If you redefine these methods, the implementation will be considered incorrect.

    ### 🔹 Required Imports (Ensure this is in the file)
    ```java
    import static SmaClickUtilities.clickWebElementForTpath;
    import static SmaSendKeyUtilites.sendKeysElementTPath;
    
    DO NOT use element.click() or element.sendKeys() directly. Instead, replace them with the corresponding method calls from SmaClickUtilities and SmaSendKeyUtilites.

    Requirements:
    1. Comprehensive Element Locator Methods:
    - Create multiple specialized element locator methods:
        a) findElementById(String id)
        b) findElementByName(String name)
        c) findElementByCssSelector(String cssSelector)
        d) findElementByXpath(String xpath)
        e) findElementByLinkText(String linkText)
        f) findElementByPartialLinkText(String partialLinkText)

    Each method should:
    - Log the locator strategy being used
    - Implement explicit waits with detailed logging
    - Include error handling with specific exception messages
    - Return the WebElement if found
    - Throw a custom, informative exception if element not found

    2. Screenshot Capture Methods:
    - Create methods to capture screenshots at different stages:
        a) captureScreenshotOnStep(String stepName)
        b) captureScreenshotOnFailure(String methodName)
        c) captureFullPageScreenshot()

    Each screenshot method should:
    - Generate unique filename with timestamp
    - Include step or failure context in filename
    - Log the screenshot location
    - Create a screenshots directory if it doesn't exist

    3. Enhanced Browser and Test Setup:
    - Use WebDriverManager for Chrome initialization
    - Set browser window size to 1920x1080
    - Clear cookies before test
    - Implement comprehensive logging
    - Create a method to log each test step with timestamp

    These methods should:
    - Log the action being performed
    - Use JavaScript click/scroll as fallback
    - Handle StaleElementReferenceException
    - Capture screenshot after successful interaction

    5. Modal and Overlay Handling:
    - Create comprehensive modal dismissal methods
    - Log modal interaction details
    - Capture screenshot after modal dismissal
    - Implement multiple strategies for modal closure

    6. Error Handling and Logging:
    - Implement detailed logging for every action
    - Use java.util.logging or SLF4J
    - Log method entry, exit, and any exceptions
    - Create a custom logger method that:
        * Logs to console
        * Optionally writes to a log file
        * Includes timestamp and context

    7. Retry and Resilience:
    - Implement TestNG retry analyzer
    - Add methods to handle dynamic content
    - Create fallback strategies for element interactions

    8. Page Object Model Integration:
    - Organize methods into logical page classes
    - Implement methods for each page/component
    - Use consistent naming conventions

    Additional Specific Instructions:
    - Capture a screenshot after EVERY successful step
    - Log detailed information about each interaction
    - Provide meaningful error messages
    - Ensure test is language and locale independent

    Test Case Steps Implementation:
    """
        
        # Add test steps if provided
        if test_case and hasattr(test_case, 'steps') and test_case.steps:
            prompt += "\nImplement these specific steps with detailed logging and screenshot capture:\n" + \
                    "\n".join(f"- {step}" for step in test_case.steps)
        
        return prompt
    
    
    
    
    