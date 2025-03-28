
import os
from browser_use import Agent, Controller
from dotenv import load_dotenv
from langchain_google_genai import ChatGoogleGenerativeAI
from pydantic import SecretStr
from java_code_generator import JavaCodeGenerator

load_dotenv()

class AI_TestAgent:
    def __init__(self, controller: Controller):
        self.controller = controller
        self.llm = ChatGoogleGenerativeAI(
            model="gemini-2.0-flash-exp",
            temperature=0.2,
            api_key=SecretStr(os.getenv("GEMINI_API_KEY"))
        )
        # Initialize the Java code generator with the same LLM instance
        self.code_generator = JavaCodeGenerator(self.llm)
        self.current_test_case = None
    
    def send_request_to_llm(self, prompt: str):
        """Send a request to the LLM and return the response"""
        return self.code_generator.send_request_to_llm(prompt)

    def generate_test_from_json(self, json_file: str, test_case=None):
        """Generate Java Selenium TestNG tests from JSON
        
        Args:
            json_file: Path to the JSON file with test actions
            test_case: Optional test case object. If None, uses the current_test_case
        """
        # Use the provided test_case if available, otherwise use the current_test_case
        test_case_to_use = test_case if test_case is not None else self.current_test_case
        
        if test_case_to_use is None:
            print("Warning: No test case provided and no current test case set.")
        
        # Delegate to the JavaCodeGenerator with the test case
        return self.code_generator.generate_test_from_json(json_file, test_case_to_use)
    
    async def run_test_and_generate_code(self, test_case, json_file: str):
        """Run the test and then generate code using the same test case"""
        # Run the test first
        self.current_test_case = test_case
        history = await self.run_test(test_case)
        
        # Then generate the code using the same test case
        result = self.generate_test_from_json(json_file, test_case,test_case.name)
        
        return history, result
            
    async def run_test(self, test_case):
        """Run a single test case"""
        # Store the current test case for use in code generation
        self.current_test_case = test_case
        
        task = f"Perform the following case: {test_case.description}. Steps:{test_case.steps}"    
        agent = Agent(task, self.llm, controller=self.controller, use_vision=True, save_conversation_path='logs/conversation')
        history = await agent.run()
        
        return history