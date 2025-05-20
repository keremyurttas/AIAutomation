import os

import httpx

os.environ["ANONYMIZED_TELEMETRY"] = "false"
from dotenv import load_dotenv
from pydantic import SecretStr

from browser_use import Agent, Browser, Controller, BrowserConfig
from langchain_google_genai import ChatGoogleGenerativeAI
from SystemPrompt import MySystemPrompt
from java_code_generator import JavaCodeGenerator
from langchain_openai import AzureChatOpenAI, ChatOpenAI

load_dotenv()


class AI_TestAgent:
    
    def __init__(self, controller: Controller):
        self.controller = controller
        self._llm = AzureChatOpenAI(
            model_name="gpt-4o",
            api_version="2024-12-01-preview",
            azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT"),
            openai_api_key=os.getenv("AZURE_OPENAI_KEY"),
            http_async_client=httpx.AsyncClient(verify=False),
            deployment_name="gpt-4o",

        )
        # self._llm=ChatGoogleGenerativeAI(
        #     model="gemini-2.0-flash-exp",
        #     temperature=0.2,
        #     # api_key=SecretStr(os.getenv("GEMINI_API_KEY"))

        # )
        self._planner_llm=AzureChatOpenAI(
            model="gpt-4o",
            api_version="2024-12-01-preview",
            azure_endpoint=os.getenv("AZURE_OPENAI_ENDPOINT"),
            api_key=os.getenv("AZURE_OPENAI_KEY"),
            http_async_client=httpx.AsyncClient(verify=False), 
        )
        
       
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
        new_rules = """IMPORTANT INSTRUCTIONS: I am an automation agent executing test scenarios exactly as written. I will focus on completing each test case step by step, following the exact instructions provided.

As a human user interacting with the website, I will:

1) Execute each test step EXACTLY as written without any deviation or interpretation
2) Handle all modals or popups immediately when they appear
3) Ignore any advertisements or promotional content
4) Wait for pages to fully load before taking any action
5) Use scrolling to find elements that are not immediately visible
6) Interact with elements exactly as a human would:
   - Always click on input fields before typing text
   - If a button is not immediately clickable, move the mouse over it, then click
   - Use tab navigation when appropriate
   - Scroll smoothly before interacting with elements
   - When clicking on any <li> element inside a <ul>, ALWAYS click the parent <ul> element first to expand it
   - When interacting with dropdown menus, always click to open the dropdown before selecting any option
7) Take actions deliberately and carefully, following human timing
8) Pause briefly between actions to simulate natural human behavior
9) Always verify actions have the expected result before proceeding to the next step
10) Never add my own interpretation or logic to the test steps
11) Never improvise or expand on what is written in the test steps
12) Follow the literal instructions word-for-word without making assumptions
13) When navigating between pages, verify the new page has loaded correctly before proceeding
14) Never skip a step or combine steps - execute each step individually as written
15) If a step cannot be completed exactly as written, report the issue without trying alternative approaches
16) If any step cannot be completed exactly as written (e.g., element not found, button does not work, unexpected page layout), STOP execution immediately and report the step that failed, along with the reason. Do NOT restart the test case or repeat steps unless explicitly instructed.
17) Never repeat a step unless instructed. If you fail a step once, do not retry. Report and halt.
18) If authentication fails or UI elements are different from expected, do not guess or continue. Report the issue and stop.
19) Each test step must be executed in complete isolation from the others.
20) Do NOT merge or combine multiple steps into a single action, even if they appear related (e.g., entering username and password must be two separate actions).
21) Each step must be executed in order, one at a time. Do NOT anticipate or perform future steps early.
22) If a step is already complete due to a previous one (e.g., page already loaded), still repeat it as written.
23) Treat each step as a strict atomic instruction. Finish it completely and verify its completion before starting the next.


My primary goal is to follow the provided test steps with absolute precision, executing them exactly as written without adding any interpretations or additional logic.
"""
        agent = Agent(
            task=task_description,
            llm=self._llm,
            controller=self.controller,
            use_vision=True,
            save_conversation_path='logs/conversation',
            extend_system_message=new_rules,
            browser=browser,
            max_actions_per_step=30,
            max_failures=10,
            # save_playwright_script_path='playwright/'
            # planner_llm=self._planner_llm,
            tool_calling_method="function_calling",

            
            
        )

        try:
            
            return await agent.run()
        
        finally:
            # Gracefully shut down the browser to avoid lingering processes
            await browser.close()
