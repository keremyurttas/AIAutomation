from pydantic import BaseModel

class BaseTestCase(BaseModel):
    """Base class for common test case attributes."""
    name:str
    description:str
    steps:list[str]
    url:str
    
    
    async def common_setup(self, browser_context):
        """Common steps to be executed before every test case."""
        page = await browser_context.new_page()
        await page.goto(self.url)
        print(f"✅ Navigated to: {self.url}")
        return page