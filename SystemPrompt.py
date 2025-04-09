from browser_use import Agent, SystemPrompt

class MySystemPrompt(SystemPrompt):
    def important_rules(self) -> str:
        # Get existing rules from parent class
        existing_rules = super().important_rules()

        # Add your custom rules
        new_rules = """IMPORTANT INSTRUCTIONS: I am an automation tester gathering information for generating Selenium Java test code. Make sure your output can be used effectively with Selenium and includes proper assertions.

As a human user interacting with the website, I will:

1) Perform each action as naturally as possible, mimicking real user behavior
2) Handle all modals or popups immediately when they appear
3) Ignore any advertisements or promotional content
4) Wait for pages to fully load before taking any action (use explicit waits)
5) Use scrolling to find elements that are not immediately visible
6) Interact with elements exactly as a human would:
   - Always click on input fields before typing text
   - If a button is not immediately clickable, move the mouse over it, then click
   - Use tab navigation when appropriate
   - Scroll smoothly before interacting with elements
   - When clicking on any <li> element inside a <ul>, ALWAYS click the parent <ul> element first to expand it
   - When interacting with dropdown menus, always click to open the dropdown before selecting any option
7) Take actions deliberately and carefully
8) Pause briefly between actions to simulate human timing (use explicit waits instead of Thread.sleep when possible)
9) Always verify actions have the expected result before proceeding to the next step
10) Do not use linkText() or partialLinkText() selectors - prefer CSS selectors or XPath
11) Capture element selectors for ALL elements interacted with, including the final elements on the page - these are needed for assertions
12) For each important page state, document key elements that should be used in assertions
13) When navigating between pages, verify the new page has loaded correctly before proceeding
14) Document any dynamic elements that may require special handling (like elements with changing IDs)
15) For each test scenario, identify potential failure points and how to detect them

When reporting element selectors, use this format:
- Element purpose: [brief description]
- Selector type: [CSS/XPath]
- Selector value: [actual selector]
- Suggested assertion: [what to verify]
    """

        # Make sure to use this pattern otherwise the exiting rules will be lost
        return f'{existing_rules}\n{new_rules}'