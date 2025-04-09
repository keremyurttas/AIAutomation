import json
import os
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import SecretStr
from test_runner import run_tests
import asyncio




class CaseGenerator:
    def __init__ (self,llm=None):
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

            # Ensure response is a string
            if isinstance(response, str):
                response = response.strip("```json").strip("```").strip()
                
                # Debugging: Print raw response
                print("🔹 Raw Response from LLM:\n", response)

                return response
            else:
                print(f"❌ Unexpected response type: {type(response)}")
                return None
        except Exception as e:
            print(f"❌ Error sending request: {e}")
            return None        

async def get_case_details_from_user():
        """Get case details from user"""
        print("Test case generator has started.")
        url=input("Write the url of website:")
        brief=input(f"Describe the needed test cases shortly:")
        prompt=get_case_generator_prompt(url,brief)
        generator=CaseGenerator()
        save_output_as_json(generator.send_request_to_llm(prompt=prompt),"")
        user_input = input("Do you want to perform the first three tests and generate selenium codes? (yes/no): ")
        if user_input.lower() == "yes":
            await run_tests()
        else:
            print("Exiting...")


def save_output_as_json(output, filename):
    """Save the output to a JSON file"""
    try:
        if not output:
            print("❌ No response received from LLM.")
            return
        
        test_cases = json.loads(output)

        filename = "test_cases.json"  # Set a default filename
        with open(filename, "w", encoding="utf-8") as f:
            json.dump(test_cases, f, indent=4, ensure_ascii=False)

        print(f"✅ Test cases saved to {filename}")
    except json.JSONDecodeError as e:
        print(f"❌ Failed to parse response as JSON: {e}")
 

def get_case_generator_prompt(website_url, brief):
    prompt = f"""
    I am tasked with generating comprehensive test cases for a website. 
    The user provides the website URL and a brief description of the test focus areas (e.g., functionality, usability, performance, security, compatibility). 
    Using this input, generate **detailed test cases** that cover these areas thoroughly.

    **Test Case Format:**
    - **Test Case ID**: A unique identifier (e.g., TC001).
    - **Test Name**: Descriptive title of the test case.
    - **Description**: The purpose of the test.
    - **Preconditions**: Any setup or requirements before execution.
    - **Test Steps**: Step-by-step instructions with precise actions, including interactions with the given URL.
    - **Expected Result**: The expected behavior of the system if it functions correctly.
    
    **Test Case Guidelines:**
    - Each test case must **start with navigating to the URL**: "{website_url}".
    - Include **positive, negative, and edge cases** for thorough validation.
    - Cover elements such as **navigation, forms, buttons, links, media, responsiveness, and performance**.
    - If security or performance tests are required, note any special tools needed.
    - Do not use special characters like './-_' on case names.
    - Use **clear, structured JSON format** for output.

    **Example JSON Output:**
    ```json
    [
        {{
            "test_case_id": "TC001",
            "name": "Login Functionality Test",
            "description": "Verify that a registered user can log in successfully.",
            "preconditions": "User must be registered and have valid login credentials.",
            "steps": [
                "1. Open the browser and go to '{website_url}'",
                "2. Click on the 'Login' button in the top-right corner.",
                "3. Enter a valid email and password in the respective fields.",
                "4. Click the 'Login' button.",
                "5. Verify that the user is redirected to the dashboard page."
            ],
            "expected_result": "User is successfully logged in and redirected to the dashboard.",
            "url": "{website_url}"
        }},
        {{
            "test_case_id": "TC002",
            "name": "Invalid Login Attempt",
            "description": "Check behavior when logging in with incorrect credentials.",
            "preconditions": "User account must exist but use incorrect credentials.",
            "steps": [
                "1. Open the browser and go to '{website_url}'",
                "2. Click on the 'Login' button.",
                "3. Enter an invalid email or password.",
                "4. Click the 'Login' button.",
                "5. Verify that an error message appears, indicating incorrect login details."
            ],
            "expected_result": "An error message is displayed, preventing login with invalid credentials.",
            "url": "{website_url}"
        }}
    ]
    ```

    **User Input:**
    - Website URL: {website_url}
    - Test Case Focus: {brief}

    Generate test cases in **structured JSON format**.
    """
    return prompt
     
if __name__=="__main__":
        asyncio.run(get_case_details_from_user())    