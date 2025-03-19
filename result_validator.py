from pydantic import BaseModel

class TestResult(BaseModel):
    test_name:str
    status:str
    details:str
    
def validate_result(test_case, actual_result):
    expected= test_case.expected_result.lower()
    actual=actual_result.lower()
    print(f"expected result is {expected} but got : {actual}")
    
    status = "PASSED" if expected in actual else "FAILED"
    return TestResult(test_name=test_case.name,status=status,details=actual)     