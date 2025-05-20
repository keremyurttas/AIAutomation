import json
import os
import httpx
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_openai import AzureOpenAI
from pydantic import SecretStr
from test_runner import run_tests
import asyncio




class CaseGenerator:
    def __init__ (self,llm=None):
        """Initialize the Java code generator with an optional LLM"""
        self.llm = llm
        if not llm:
            # Create a default LLM if none is provided
            self.llm = AzureOpenAI(
            api_version="2024-12-01-preview",
            azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT"),
            api_key=os.getenv("AZURE_OPENAI_KEY"),
            http_client=httpx.Client(verify=False),
            
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
        number_of_cases=input("How many test cases do you want to generate? :")
        prompt=get_case_generator_prompt(url,brief,number_of_cases)
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
 

def get_case_generator_prompt(website_url, brief, number_of_cases=1):
    prompt = f"""
    I need EXACTLY {number_of_cases} test case(s) that specifically test this requirement:
    
    "{brief}"
    
    The test(s) must be for the website: {website_url}
    
    CRITICAL INSTRUCTIONS:
    - Your ONLY task is to create test case(s) that verify the specific requirement in the brief - nothing else
    - Each step must directly contribute to testing the requirement in the brief
    - DO NOT generate general or unrelated test cases - focus solely on testing "{brief}"
    - Create {number_of_cases} test case(s) that would be executed by a QA tester to verify this specific requirement works correctly
    - If the prompt provides any directions, follow them in your response
    Test case JSON format:
    ```json
    [
        {{
            "test_case_id": "TC001",
            "name": "[Brief-specific test name]",
            "description": "[How this test verifies the brief requirement]",
            "preconditions": "[Specific setup needed for this test]",
            "steps": [
                "Open the browser and go to '{website_url}'",
                "[Brief-specific step 2]",
                "[Brief-specific step 3]",
                "[...]"
            ],
            "expected_result": "[Expected outcome that confirms the brief requirement works]",
            "url": "{website_url}"
        }}
    ]
    ```
    
    Return EXACTLY {number_of_cases} test case(s) in the JSON format shown above, with each test directly verifying: "{brief}"
    """
    print("🔹 Prompt sent to LLM:\n", prompt)
    return prompt
    
if __name__=="__main__":
        asyncio.run(get_case_details_from_user())    