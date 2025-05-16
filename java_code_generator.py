import json
import os
# # os.environ["CURL_CA_BUNDLE"] = ""
# # os.environ["REQUESTS_CA_BUNDLE"] = ""
# import ssl
# ssl._create_default_https_context = ssl._create_unverified_context
import httpx
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_openai import AzureChatOpenAI
from openai import AzureOpenAI
from pydantic import SecretStr
from dotenv import load_dotenv
from utils import class_name_generator
load_dotenv()
class PromptTemplates:
    """Class to manage all prompt templates used in code generation"""
    
    @staticmethod
    def get_standard_prompt_template():
        """Returns the standard prompt template for Java code generation"""
        return """
    Generate a single complete Java file named `{class_name}.java` implementing a Selenium TestNG test based on this JSON test data:

    {test_data_json}
    

Requirements:

1. Smart & Reliable Element Locator Methods:
   - Create a dedicated ElementFinder utility class with specialized locator methods following this priority:
     a) findElementByXpath(String xpath)
     b) findElementById(String id)
     c) findElementByCssSelector(String cssSelector)
     d) findElementByName(String name)
     e) findElementByLinkText(String linkText)
     f) findElementByPartialLinkText(String partialLinkText)
     g) findElementByCustomStrategy(Map<String, String> attributes)

   Each method must:
   - Use WebDriverWait with custom timeout + ExpectedConditions
   - Log the locator strategy with clear identification
   - Implement smart selector fallback (try id first, then name, etc.)
   - Return WebElement if found, with highlighted element in screenshots
   - Throw ElementNotFoundException with detailed context if not found
   - Include JavaScript fallback execution capabilities
   - Handle all common exceptions (StaleElementReferenceException, etc.)

2. Comprehensive Screenshot System:
   - Implement methods:
     * captureScreenshotOnStep(String stepName)
     * captureScreenshotOnFailure(String methodName, Throwable error)
     * captureFullPageScreenshot()
     * captureElementScreenshot(WebElement element, String elementName)
   
   Features:
   - Structured folders by test class/date/run
   - Unique filenames with timestamp + test context
   - Automatic directory creation and cleanup
   - Screenshot inclusion in test reports
   - Option to highlight interacted elements

3. Advanced WebDriver Configuration:
   - WebDriverManager with browser version control
   - Cross-browser support (Chrome, Firefox, Edge)
   - Custom ChromeOptions/FirefoxOptions for performance
   - Window size presets (desktop, mobile, tablet)
   - Network throttling simulation
   - Cookie and cache management between tests
   - User-agent configuration
   - Network request monitoring
   - Manual captcha intervention handling with wait

4. Comprehensive Modal & Overlay Handling:
   - Multi-strategy popup dismissal:
     * Close button detection by common attributes
     * Escape key simulation
     * Overlay click-away detection
     * Wait for animation completion
   - Shadow DOM penetration capabilities
   - iFrame context switching automation
   - Cookie consent handler

5. Enterprise-Grade Resilience:
   - Custom TestNG RetryAnalyzer with configurable attempts
   - Circuit breaker pattern for repetitive failures
   - Conditional waits with progressive timeouts
   - Element staleness detection and refresh
   - DOM mutation observation for dynamic content
   - Network instability compensation

6. Advanced Logging & Reporting:
   - SLF4J with custom MDC context
   - Structured JSON logging for machine parsing
   - Test context enrichment in logs
   - Visual execution timeline
   - Element interaction history
   - HTTP request/response capture

7. Enhanced Page Object Architecture:
   - BasePage with common interactions
   - Fluent interface pattern (method chaining)
   - Lazy element initialization
   - Component-based design for reusable UI elements
   - State validation methods
   - Page transition handling
   - Pre/post-condition verification

8. Internationalization & Accessibility:
   - Language-independent selectors
   - ARIA attribute support
   - RTL layout handling
   - Visual/text content verification
   - Automated contrast checking
   - Keyboard navigation support

9. Comprehensive Assertions Framework:
   - Multi-level verification:
     * Element presence/visibility assertions
     * Content validation (text, attributes, CSS properties)
     * State verification (enabled, selected, etc.)
     * Visual comparison with baseline
     * Error message inspection
     * Response code validation
     * Performance threshold verification
   - Soft assertions for multiple checks
   - Custom assertion messages with context
   - Data-driven validation using test parameters
   - API response correlation with UI state

10. Test Data Management:
    - Dynamic test data generation
    - Database state verification
    - Test data cleanup
    - API-based test data seeding



Use "interacted elements" xpaths. For elements where "interacted_element" is null, use appropriate locator strategies based on other datas on the given JSON.
Do not add any explanation, just give me the java code
    
    Test Case Steps:
    {test_case_steps}
    """
    
    @staticmethod
    def get_custom_methods_prompt_template():
        """Returns the prompt template with custom methods requirement"""
        return """
    Generate a single complete Java file named `{class_name}.java` implementing a Selenium TestNG test based on this JSON test data:

    {test_data_json}
    ## Important Instructions:
    ### ⚠ Mandatory Usage of Utility Methods
    Do not add any explanation, just give me the java code
    Instead of 'element.click' always use clickWebElementForTpath(By locator)
    Instead of 'element.sendKeys(value)' always use sendKeysElementTPath(By locator, boolean clearInput, String value)
    
    **⚠ DO NOT define `clickWebElementForTpath` and `sendKeysElementTPath`. These are already implemented in `SmaClickUtilities` and `SmaSendKeyUtilites`. Simply import and use them in the click and sendKey events!!.**  
    If you redefine these methods, the implementation will be considered incorrect.

    ### 🔹 Required Imports (Ensure this is in the file)
    ```java
    import static SmaClickUtilities.clickWebElementForTpath;
    import static SmaSendKeyUtilites.sendKeysElementTPath;
    
    DO NOT use element.click() or element.sendKeys() directly. Instead, replace them with the corresponding method calls from SmaClickUtilities and SmaSendKeyUtilites.

    1. Smart & Reliable Element Locator Methods:
   - Create a dedicated ElementFinder utility class with specialized locator methods following this priority:
     a) findElementById(String id)
     b) findElementByXpath(String xpath)
     c) findElementByCssSelector(String cssSelector)
     d) findElementByName(String name)
     e) findElementByLinkText(String linkText)
     f) findElementByPartialLinkText(String partialLinkText)
     g) findElementByCustomStrategy(Map<String, String> attributes)

   Each method must:
   - Use WebDriverWait with custom timeout + ExpectedConditions
   - Log the locator strategy with clear identification
   - Implement smart selector fallback (try id first, then name, etc.)
   - Return WebElement if found, with highlighted element in screenshots
   - Throw ElementNotFoundException with detailed context if not found
   - Include JavaScript fallback execution capabilities
   - Handle all common exceptions (StaleElementReferenceException, etc.)

2. Comprehensive Screenshot System:
   - Implement methods:
     * captureScreenshotOnStep(String stepName)
     * captureScreenshotOnFailure(String methodName, Throwable error)
     * captureFullPageScreenshot()
     * captureElementScreenshot(WebElement element, String elementName)
   
   Features:
   - Structured folders by test class/date/run
   - Unique filenames with timestamp + test context
   - Automatic directory creation and cleanup
   - Screenshot inclusion in test reports
   - Option to highlight interacted elements

3. Advanced WebDriver Configuration:
   - WebDriverManager with browser version control
   - Cross-browser support (Chrome, Firefox, Edge)
   - Custom ChromeOptions/FirefoxOptions for performance
   - Window size presets (desktop, mobile, tablet)
   - Network throttling simulation
   - Cookie and cache management between tests
   - User-agent configuration
   - Network request monitoring
   - Manual captcha intervention handling with wait

4. Comprehensive Modal & Overlay Handling:
   - Multi-strategy popup dismissal:
     * Close button detection by common attributes
     * Escape key simulation
     * Overlay click-away detection
     * Wait for animation completion
   - Shadow DOM penetration capabilities
   - iFrame context switching automation
   - Cookie consent handler

5. Enterprise-Grade Resilience:
   - Custom TestNG RetryAnalyzer with configurable attempts
   - Circuit breaker pattern for repetitive failures
   - Conditional waits with progressive timeouts
   - Element staleness detection and refresh
   - DOM mutation observation for dynamic content
   - Network instability compensation

6. Advanced Logging & Reporting:
   - SLF4J with custom MDC context
   - Structured JSON logging for machine parsing
   - Test context enrichment in logs
   - Visual execution timeline
   - Element interaction history
   - HTTP request/response capture

7. Enhanced Page Object Architecture:
   - BasePage with common interactions
   - Fluent interface pattern (method chaining)
   - Lazy element initialization
   - Component-based design for reusable UI elements
   - State validation methods
   - Page transition handling
   - Pre/post-condition verification

8. Internationalization & Accessibility:
   - Language-independent selectors
   - ARIA attribute support
   - RTL layout handling
   - Visual/text content verification
   - Automated contrast checking
   - Keyboard navigation support

9. Comprehensive Assertions Framework:
   - Multi-level verification:
     * Element presence/visibility assertions
     * Content validation (text, attributes, CSS properties)
     * State verification (enabled, selected, etc.)
     * Visual comparison with baseline
     * Error message inspection
     * Response code validation
     * Performance threshold verification
   - Soft assertions for multiple checks
   - Custom assertion messages with context
   - Data-driven validation using test parameters
   - API response correlation with UI state

10. Test Data Management:
    - Dynamic test data generation
    - Database state verification
    - Test data cleanup
    - API-based test data seeding

IMPORTANT: When using element locators from the JSON data:
1. ALWAYS use the EXACT xpath values from the "interacted_element" fields in the JSON
2. For any element with an "interacted_element" value, extract the full xpath from the "xpath" field in that object
3. DO NOT simplify, modify, or create alternative XPaths - use the complete paths exactly as provided
4. Example: If JSON contains `"xpath": 'html/body/ytd-app/div/div[2]/ytd-masthead/div[4]/div[2]/yt-searchbox/div/form/input'`, use exactly that string in your findElementByXpath() method
5. Do not attempt to optimize or shorten the XPaths as they're specifically designed for the application structure

For elements where "interacted_element" is null, use appropriate locator strategies based on context.
    Test Case Steps Implementation:
    {test_case_steps}
    """
    @staticmethod
    def get_playwright_prompt_template():
        """Returns the prompt template with custom methods requirement"""
        return """
    Generate a single complete TypeScript file named {class_name}.spec.ts implementing a Playwright test based on this JSON test data:
    {test_data_json}
    ## Important Instructions:
    ### ⚠ Mandatory Usage of Utility Methods
    Do not add any explanation, just give me the TypeScript code
    Instead of 'element.click()' always use clickElement(page, locator)
    Instead of 'page.fill()' always use fillElement(page, locator, value, clearInput)

    **⚠ DO NOT define `clickElement` and `fillElement`. These are already implemented in `PageElementUtils`. Simply import and use them in the click and fill events!!.**  
    If you redefine these methods, the implementation will be considered incorrect.

    ### 🔹 Required Imports (Ensure this is in the file)
    ```typescript
    import { test, expect } from '@playwright/test';
    import { clickElement, fillElement } from './PageElementUtils';
    ```

    DO NOT use page.click() or page.fill() directly. Instead, replace them with the corresponding method calls from PageElementUtils.

    1. Smart & Reliable Element Locator Methods:

    Create a dedicated ElementFinder utility class with specialized locator methods following this priority:
    a) findElementById(page, id)
    b) findElementByTestId(page, testId)
    c) findElementByXPath(page, xpath)
    d) findElementByCSS(page, cssSelector)
    e) findElementByText(page, text)
    f) findElementByRole(page, role, options)
    g) findElementByCustomStrategy(page, attributes)

    Each method must:

    Use page.waitForSelector with custom timeout
    Log the locator strategy with clear identification
    Implement smart selector fallback (try test-id first, then CSS, etc.)
    Return Playwright Locator if found
    Handle retries and timeouts gracefully
    Include JavaScript evaluation capabilities
    Handle all common exceptions with detailed error messaging


    Comprehensive Screenshot System:

    Implement methods:

    captureScreenshot(page, stepName)
    captureScreenshotOnFailure(page, methodName, error)
    captureFullPageScreenshot(page)
    captureElementScreenshot(page, locator, elementName)



    Features:

    Structured folders by test class/date/run
    Unique filenames with timestamp + test context
    Automatic directory creation and cleanup
    Screenshot inclusion in test reports
    Option to highlight interacted elements


    Advanced Browser Configuration:

    Playwright project configuration with browser options
    Cross-browser support (Chromium, Firefox, WebKit)
    Custom browser context options for performance
    Viewport presets (desktop, mobile, tablet)
    Network throttling simulation
    Cookie and storage state management between tests
    User-agent configuration
    Network request interception and monitoring
    Manual captcha intervention handling with wait


    Comprehensive Modal & Overlay Handling:

    Multi-strategy popup dismissal:

    Close button detection by common attributes
    Escape key simulation
    Overlay click-away detection
    Wait for animation completion


    Shadow DOM handling
    Frame context switching automation
    Cookie consent handler


    Enterprise-Grade Resilience:

    Custom test retry logic with configurable attempts
    Circuit breaker pattern for repetitive failures
    Conditional waits with progressive timeouts
    Element staleness detection and retry
    DOM mutation observation for dynamic content
    Network instability compensation


    Advanced Logging & Reporting:

    Structured logging with test context
    JSON logging for machine parsing
    Test context enrichment in logs
    Visual execution timeline
    Element interaction history
    HTTP request/response capture


    Enhanced Page Object Architecture:

    BasePage with common interactions
    Fluent interface pattern (method chaining)
    Lazy element initialization
    Component-based design for reusable UI elements
    State validation methods
    Page transition handling
    Pre/post-condition verification


    Internationalization & Accessibility:

    Language-independent selectors
    ARIA attribute support
    RTL layout handling
    Visual/text content verification
    Automated contrast checking
    Keyboard navigation support


    Comprehensive Assertions Framework:

    Multi-level verification:

    Element presence/visibility assertions
    Content validation (text, attributes, CSS properties)
    State verification (enabled, selected, etc.)
    Visual comparison with baseline
    Error message inspection
    Network response validation
    Performance threshold verification


    Soft assertions for multiple checks
    Custom assertion messages with context
    Data-driven validation using test parameters
    API response correlation with UI state


    Test Data Management:

    Dynamic test data generation
    Database state verification
    Test data cleanup
    API-based test data seeding



    IMPORTANT: When using element locators from the JSON data:

    ALWAYS use the EXACT selector values from the "interacted_element" fields in the JSON
    For any element with an "interacted_element" value, extract the full selector from the "selector" field in that object
    DO NOT simplify, modify, or create alternative selectors - use the complete paths exactly as provided
    Example: If JSON contains "selector": 'div.form-container input#username', use exactly that string in your findElementByCSS() method
    Do not attempt to optimize or shorten the selectors as they're specifically designed for the application structure

    For elements where "interacted_element" is null, use appropriate locator strategies based on context.
    Test Case Steps Implementation:
    {test_case_steps}
    """


class JavaCodeGenerator:
    
    def __init__(self, llm=None):
        """Initialize the Java code generator with an optional LLM"""
        self.llm = AzureOpenAI(
            api_version="2024-12-01-preview",
            azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT"),
            api_key=os.getenv("AZURE_OPENAI_KEY"),
            http_client=httpx.Client(verify=False),
            
        )
        self.prompt_templates=PromptTemplates()
        
    
    def send_request_to_llm(self, prompt: str):
        """Send a request to the LLM and return the code content"""
        print("prompt is", prompt)
        try:
            response = self.llm.chat.completions.create(
                model="gpt-4o",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            if content.startswith("```java"):
                content = content.removeprefix("```java").removesuffix("```").strip()
            return content
        except Exception as e:
            print(f"Error sending request: {e}")
            return None

    def generate_code_from_data(self, test_data: dict, test_case=None):
        # Build the prompt using your template
        formatted_class_name = (
            "".join(word.capitalize() for word in test_case.name.split())
            if test_case and hasattr(test_case, 'name')
            else "GeneratedTest"
        )
        test_case_steps = "\n".join(f"- {step}" for step in test_case.steps) if test_case and hasattr(test_case, 'steps') else ""
        template = self.prompt_templates.get_standard_prompt_template()
        prompt = template.format(
            class_name=formatted_class_name,
            test_data_json=json.dumps(test_data, indent=4, ensure_ascii=False),
            test_case_steps=test_case_steps
        )
        return self.send_request_to_llm(prompt)

    def generate_test_from_json(self, json_file: str, test_case=None):
        """Generate Java Selenium TestNG tests from JSON and save to a file"""
        try:
            with open(json_file, 'r', encoding='utf-8') as file:
                test_data = json.load(file)

            java_test_code = self.generate_code_from_data(test_data, test_case)
            print("new test code is", java_test_code)
            generated_codes_dir = 'generated_codes'
            if isinstance(java_test_code, str) and java_test_code:
                formatted_name = "".join(word.capitalize() for word in test_case.name.split()) + ".java"
                os.makedirs(generated_codes_dir, exist_ok=True)
                with open(f"{generated_codes_dir}/{formatted_name}", "w", encoding='utf-8') as test_file:
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
        # Build the prompt using your template
        formatted_class_name = (
            "".join(word.capitalize() for word in test_case.name.split())
            if test_case and hasattr(test_case, 'name')
            else "GeneratedTest"
        )
        test_case_steps = "\n".join(f"- {step}" for step in test_case.steps) if test_case and hasattr(test_case, 'steps') else ""
        template = self.prompt_templates.get_standard_prompt_template()
        prompt = template.format(
            class_name=formatted_class_name,
            test_data_json=json.dumps(test_data, indent=4, ensure_ascii=False),
            test_case_steps=test_case_steps
        )
        return self.send_request_to_llm(prompt)

    # def create_prompt_from_json(self, test_data: dict, test_case=None, use_playwright_prompt=  False):
    #     """Create a prompt to guide the LLM to generate a Playwright test file from JSON test data."""
        
    #     # Determine the TypeScript class name
    #     formatted_class_name = (
    #         "".join(word.capitalize() for word in test_case.name.split())
    #         if test_case and hasattr(test_case, 'name')
    #         else "GeneratedTest"
    #     )
        
    #     # Prepare the steps string
    #     test_case_steps = "\n".join(f"- {step}" for step in test_case.steps) if test_case and hasattr(test_case, 'steps') else ""
        
    #     # Get the appropriate template
    #     if use_playwright_prompt:
    #         template = PromptTemplates.get_playwright_prompt_template()
    #     else:
    #         template = PromptTemplates.get_standard_prompt_template()  # Fallback to Java template if needed
        
    #     # Format the template with the test data
    #     formatted_prompt = template.format(
    #         class_name=formatted_class_name,
    #         test_data_json=json.dumps(test_data, indent=4, ensure_ascii=False),
    #         test_case_steps=test_case_steps
    #     )
        
    #     return formatted_prompt
   
# if __name__ == "__main__":
#         client=AzureOpenAI(
#             api_version="2024-12-01-preview",
#             azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT"),
#             api_key=os.getenv("AZURE_OPENAI_KEY"),
#             http_client=httpx.Client(verify=False),
            
#         )
#         response= client.chat.completions.create(model="gpt-4o",messages=[{"role":"user", "content":"Hey are you there"}])
#         print(response)
        