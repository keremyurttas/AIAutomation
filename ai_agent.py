import json
import os
from browser_use import Agent, Controller
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import SecretStr
from test_cases.test_cases import GOOGLE_TEST_CASES,initial_actions
from pydantic import ValidationError
import asyncio

load_dotenv()

class AI_TestAgent:
    def __init__(self, controller: Controller):
        self.controller = controller
        self.llm = ChatGoogleGenerativeAI(
            model="gemini-2.0-flash-exp",
            temperature=0.2,
            api_key=SecretStr(os.getenv("GEMINI_API_KEY"))
        )

    async def run_test(self, test_case):
        task = f"Perform the following case: {test_case.description}. Steps:{test_case.steps}"    
        agent = Agent(task, self.llm, controller=self.controller, use_vision=True,initial_actions=initial_actions)
        history = await agent.run()

        # Convert the result from `history.final_result()` into a dictionary
        result_data = history.final_result()  # This will be a string, you need to ensure it's in a dict format
        return result_data

        

async def main():
    for test in GOOGLE_TEST_CASES:
        controller = Controller(output_model=type(test.expected_result))
        agent = AI_TestAgent(controller)  
        result = await agent.run_test(test)
        print(f"test '{test.name}' Result: ", result)

if __name__ == "__main__":
    asyncio.run(main())
