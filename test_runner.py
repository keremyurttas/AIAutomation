import asyncio
from ai_agent import AI_TestAgent
from browser_actions import controller
from test_cases.test_cases import GOOGLE_TEST_CASES
from result_validator import validate_result

async def run_test(test_case):
    """Run a single test and validate the result"""
    agent = AI_TestAgent(controller)
    actual_result = await agent.run_test(test_case)
    # validated_result = validate_result(test_case, actual_result)
    
    return actual_result  # or validated_result if validation is needed
async def run_tests():
    """Run tests concurrently"""
    tasks=[run_test(test) for test in GOOGLE_TEST_CASES]
    results= await asyncio.gather(*tasks)
    print("\nAll tests executed. Here are the results:\n")
    print("=" * 50)    
    for test, res in zip(GOOGLE_TEST_CASES, results):
        print(f"{test.name}: {res}")  # Assuming test cases have a 'name' attribute

if __name__=="__main__":
    asyncio.run(run_tests())        
    
    # agent = AI_TestAgent(controller)
    # results=[]
    
    
    # for test in TEST_CASES:
    #     actual_result = await agent.run_test(test)
    #     # validated_result =validate_result(test,actual_result)
    #     # results.append(validated_result)
        
    # for res in results:
    #     print(f"{res.test_name}:{res.status}-{res.details}")        
