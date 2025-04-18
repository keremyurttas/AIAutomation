import os

<<<<<<< HEAD
# from LLM.custom_llm_client import CustomAPILLM
=======
>>>>>>> c50d9d48d9b0ded89f90866981475745e54fa94a
os.environ["ANONYMIZED_TELEMETRY"] = "false"
from dotenv import load_dotenv
from pydantic import SecretStr

from browser_use import Agent, Browser, Controller, BrowserConfig
from langchain_google_genai import ChatGoogleGenerativeAI
from SystemPrompt import MySystemPrompt
from java_code_generator import JavaCodeGenerator
from langchain_openai import ChatOpenAI

load_dotenv()


class AI_TestAgent:
    
    def __init__(self, controller: Controller):
        
        self.controller = controller
        self._llm = ChatGoogleGenerativeAI(
            model="gemini-2.0-flash-exp",
            temperature=0.2,
            api_key=SecretStr(os.getenv("GEMINI_API_KEY"))
        )
        # self._llm= CustomAPILLM(
        #     api_url="https://hgqtpcr4-3000.euw.devtunnels.ms/gpt",
        #     model = "Claude 3.7",
        # )
        self.code_generator = JavaCodeGenerator(self._llm)
        self.current_test_case = None

    def send_request_to_llm(self, prompt: str) -> str:
        """Send a prompt to the LLM via the code generator."""
        return self.code_generator.send_request_to_llm(prompt)

    def generate_test_from_json(self, json_file: str, test_case=None) -> str:
        """
        Generate Java Selenium TestNG test code from a JSON file.

        Args:
            json_file (str): Path to the JSON file containing test instructions.
            test_case (optional): Specific test case object. Defaults to the current test case.

        Returns:
            str: Generated Java code as a string.
        """
        test_case_to_use = test_case or self.current_test_case

        if test_case_to_use is None:
            print("[Warning] No test case provided and no current test case is set.")
            return ""

        return self.code_generator.generate_test_from_json(json_file, test_case_to_use)

    async def run_test_and_generate_code(self, test_case, json_file: str):
        """
        Run a test case and generate the corresponding Java code.

        Args:
            test_case: The test case object.
            json_file (str): Path to the JSON file for code generation.

        Returns:
            tuple: (conversation history, generated Java code)
        """
        self.current_test_case = test_case
        history = await self.run_test(test_case)
        code = self.generate_test_from_json(json_file, test_case)
        return history, code

    async def run_test(self, test_case):
        """
        Execute the test case by interacting with the LLM agent.

        Args:
            test_case: The test case object to execute.

        Returns:
            list: History of messages or interactions from the agent.
        """
        self.current_test_case = test_case

        task_description = (
            f"Perform the following case: {test_case.description}. "
            f"Steps: {test_case.steps}"
        )

        browser = Browser(
            config=BrowserConfig(
                headless=False,
                disable_security=False,
            )
        )

        agent = Agent(
            task=task_description,
            llm=self._llm,
            controller=self.controller,
            use_vision=True,
            save_conversation_path='logs/conversation',
            system_prompt_class=MySystemPrompt,
            browser=browser
        )

        try:
            
            return await agent.run()
        
        finally:
            # Gracefully shut down the browser to avoid lingering processes
            await browser.close()
