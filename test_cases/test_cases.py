from pydantic import BaseModel
from typing import Union,List
class TestCase(BaseModel):
    name:str
    description:str
    steps: list[str]
    url:str
    expected_result:str
    
initial_actions=[
    {'open_tab': {'url': 'https://www.google.com'}}
]
        

COMMON_CASE_STEPS=[
'IMPORTANT INSTRUCTIONS: I am a UI Automation tester. My goal is only to complete the test steps below. If any modals or popups appear, I will immediately close them by clicking the X/close button and continue with the test. I will not click on any advertisements, promotions, or other irrelevant links. I will only interact with elements directly related to completing the test steps. I will wait for pages to fully load before performing actions. If I encounter elements that are not immediately visible, I will scroll to find them rather than clicking elsewhere.'    
]    
TRENDYOL_TEST_CASES = [
    TestCase(
        name="Login Test",
        description="Verifies user login with valid credentials",
        steps= COMMON_CASE_STEPS+ [
            'Click the giriş yap from navbar '
            'Verify you navigated to the login page'
            'Login with testest23451@gmail.com username and Testest23451 password. '
            'Check if hesabım section is visible on the top navbar. It indicates a succesfull login.'
        ],
        url="https://www.trendyol.com/",
        expected_result="I succesfully logged in"
    ),
    TestCase(
        name="Add to Cart Test",
        description="Adds items to cart and verifies checkout flow",
        steps= COMMON_CASE_STEPS+[
            "Search for a product. ",
            
            "Verify items are listed. "
            "Select a size if available, Also there might be  both color and size selection. Select all of them. ",
            "Click 'Add to Cart'. Make sure you added to item to the cart. ",
            "Proceed to checkout. ",
            "Verify total amount. "
        ],
        url="https://www.trendyol.com/",
        expected_result="I succesfully search some products and add them to the cart"
    )
]  
GOOGLE_TEST_CASES=[
    TestCase(
    name="Google Search Test",
    description="Verifies basic search functionality works correctly",
    steps=COMMON_CASE_STEPS + [
        'Open Google homepage. '
        'Verify the search input field is visible. '
        'Type "automation testing" in the search box. '
        'Press Enter or click the search button. '
        'Verify search results are displayed with relevant links. '
    ],
    url="https://www.google.com/",
    expected_result="Search results for 'automation testing' are displayed"
),

TestCase(
    name="Google Image Search Test",
    description="Verifies image search functionality",
    steps=COMMON_CASE_STEPS + [
        'Open Google homepage. '
        'Click on "Images" link in the navigation. '
        'Verify you are on the Google Images page. '
        'Type "mountain landscape" in the search box. '
        'Press Enter or click the search button. '
        'Verify image results titles are relevant '
    ],
    url="https://www.google.com/",
    expected_result="Image search results for 'mountain landscape' are displayed"
),

# TestCase(
#     name="Google Maps Test", 
#     description="Verifies Google Maps functionality",
#     steps=COMMON_CASE_STEPS + [
#         'Open Google homepage. '
#         'Click on the Google Apps menu (grid icon). '
#         'Click on the Maps app icon. '
#         'Verify Google Maps has loaded. '
#         'Type "coffee shops near me" in the search box. '
#         'Press Enter or click the search button. '
#         'Verify map displays markers for coffee shops in the vicinity. '
#     ],
#     url="https://www.google.com/",
#     expected_result="Map shows coffee shop locations"
# ),

# TestCase(
#     name="Google Account Sign-in Test",
#     description="Verifies sign-in functionality",
#     steps=COMMON_CASE_STEPS + [
#         'Open Google homepage. '
#         'Click on the "Sign in" button in the top-right corner. '
#         'Verify you are redirected to the Google sign-in page. '
#         'Enter a test email address. '
#         'Click Next. '
#         'Verify the password field appears. '
#     ],
#     url="https://www.google.com/",
#     expected_result="Password entry field is displayed after email submission"
# ),

# TestCase(
#     name="Google Advanced Search Test",
#     description="Verifies advanced search options",
#     steps=COMMON_CASE_STEPS + [
#         'Open Google homepage. '
#         'Click on Settings near the bottom-right. '
#         'Click on "Advanced search" option. '
#         'Verify advanced search page has loaded. '
#         'Fill in "all these words" with "automation testing". '
#         'Fill in "site or domain" with "github.com". '
#         'Click the Advanced Search button. '
#         'Verify search results are from github.com and contain "automation testing". '
#     ],
#     url="https://www.google.com/",
#     expected_result="Filtered search results from github.com containing 'automation testing' are displayed"
# )
]