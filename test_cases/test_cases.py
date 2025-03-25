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
    """IMPORTANT INSTRUCTIONS: As a human user interacting with the website, I will:
    1) Perform each action as naturally as possible, mimicking real user behavior
    2) If a modal or popup appears, handle every modal
    3) Ignore any advertisements or promotional content
    4) Wait for pages to fully load before taking any action
    5) Use scrolling to find elements that are not immediately visible
    6) Interact with elements exactly as a human would:
       - If I need to type, first click on the input field
       - If a button is not immediately clickable, move the mouse over it, then click
       - Use tab navigation if needed
       - Scroll smoothly before interacting with elements
    7) Take actions deliberately and carefully
    8) Pause briefly between actions to simulate human timing
    9) Always verify my actions have the expected result before proceeding"""

]    

TRENDYOL_TEST_CASES = [
#     TestCase(
#         name="Login Test",
#         description="Verifies user login with valid credentials",
#         steps= COMMON_CASE_STEPS + [
#             'Close the gender selection modal if present.',
#             'If on any other page, navigate to the main page.',
#             'Look for the "Giriş Yap" link in the top navigation and click it. Try multiple selectors including: ".link.account-user > p.link-text", "[data-testid=\'login-button\']", or any element containing "Giriş".',
#             'Verify the URL contains "giris" to confirm navigation to the login page.',
#             'Enter "testest23451@gmail.com" in the email field and "Testest23451" in the password field.',
#             'Click the login/submit button.',
#             'Verify login success by checking if the "Hesabım" text is visible in the navigation area.'
#         ],
#         url="https://www.trendyol.com/",
#         expected_result="Successfully logged in and verified 'Hesabım' is visible"
#     ),
# 

    TestCase(
    name="Add to Cart Test",
    description="Adds items to cart and verifies checkout flow",
    steps=COMMON_CASE_STEPS+[
        "Close the gender selection modal if present by clicking the X button or using ESC key. Modal do not present every time only in initial executions. If there is no modal proceed the next step.",
        "Search for 'tshirt' by typing in the search box and pressing Enter.",
        "Verify search results appear by confirming product listings are visible.",
        "Select a product by clicking on any item from the search results.",
        "If a size selection is presented, select an available size. If there are color options as well, select both color and size options that are available.",
        "Click the 'Sepete Ekle' (Add to Cart) button and wait for confirmation.",
        "Verify the item was successfully added by checking for a success message or cart count indicator.",
        "Click on the cart icon or 'Sepetim' button to proceed to checkout.",
        "On the cart page, verify the product details and total amount are displayed correctly."
    ],
    url="https://www.trendyol.com/",
    expected_result="Successfully searched for products, selected available options, added items to cart, and verified cart details"
)]

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

# TestCase(
#     name="Google Image Search Test",
#     description="Verifies image search functionality",
#     steps=COMMON_CASE_STEPS + [
#         'Open Google homepage. '
#         'Click on "Images" link in the navigation. '
#         'Verify you are on the Google Images page. '
#         'Type "mountain landscape" in the search box. '
#         'Press Enter or click the search button. '
#         'Verify image results titles are relevant. '
#         'Consider the first images that presents on the google search doesnt have a content on their alt tag. They are just category indicators.'
#     ],
#     url="https://www.google.com/",
#     expected_result="Image search results for 'mountain landscape' are displayed"
# ),

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
#     name="Google Account Signin Test",
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