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
                api_key='AIzaSyBj2ERhNURny5-CQ6r9hx_49J9zKiqXwjA'
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
    I am tasked with generating all possible test cases for a website. 
    The user will provide the website URL and a brief description of the tests they want to focus on (e.g., functionality, usability, performance, security, compatibility, or a combination). 
    Using this information, create a detailed list of test cases that cover the specified areas comprehensively.

    **Test Case Format:**
    - **Test Case ID**: A unique identifier (e.g., TC001).
    - **Test Objective**: What the test aims to verify.
    - **Preconditions**: Any setup or conditions required before testing.
    - **Test Steps**: Clear, step-by-step instructions to execute the test.
    - **Expected Result**: The anticipated outcome if the website functions correctly.

    **Considerations:**
    - Analyze the website’s purpose and features based on the URL and brief.
    - Include positive tests (valid inputs), negative tests (invalid inputs), and edge cases.
    - Cover common website elements like navigation, forms, links, media, responsiveness, and load times.
    - If specific tools or expertise are needed (e.g., security scanning tools), note them as part of the preconditions.
    - If the brief is vague, assume a broad scope and include a mix of functional, non-functional, and exploratory tests.

    
    **Example JSON Format:**
    ```json
    [
        {{
            "test_case_id": "TC001",
            "name":"Login Verify Test"
            "description": "Verify login functionality",
            "preconditions": "User must be registered",
            "steps": ["Go to login page", "Enter valid credentials", "Click login button"],
            "expected_result": "User is successfully logged in"
            "url":{website_url}
        }}
    ]
    ```
    **User Input:**
    - Website URL: {website_url}
    - Test Case Focus: {brief}

    Generate the test cases in a **structured JSON format**.
    """ 

      return prompt           

if __name__=="__main__":
        asyncio.run(get_case_details_from_user())    