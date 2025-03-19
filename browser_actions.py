from browser_use import ActionResult, Controller
from browser_use.browser.context import BrowserContext


controller=Controller(
    
)
@controller.action("Open website")
async def open_website(browser: BrowserContext, url: str):
    page = browser.get_current_page()
    await page.goto(url)
    return ActionResult(extracted_content="Website opened")