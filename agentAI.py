# import asyncio
# from browser_use.browser.context import BrowserContext
# from browser_use import ActionResult, Agent, Browser, Controller
# from langchain_google_genai import ChatGoogleGenerativeAI
# from pydantic import SecretStr,BaseModel
# import os
# from dotenv import load_dotenv
# load_dotenv()

# class CheckoutResult(BaseModel):
#     login_status:bool
#     cart_status:str
#     checkout_status:str
#     total_update_status:str
#     confirmation_message:str
# controller=Controller(output_model=CheckoutResult)

# # @controller.action('Open website')
# # async def open_website(url: str, browser: Browser):
# #     page = browser.get_current_page()
# #     await page.goto(url)
# #     print('custom action called')
# #     return ActionResult(extracted_content='Website opened')
    

# @controller.action('Get specific email element attribute and current URL')
# async def get_attr_url(browser :BrowserContext):
#     page=await browser.get_current_page()
#     current_url=page.url
#     attr= await page.get_by_text('Alışverişe Başla').get_attribute('class') 
#     print(f'printing from controller: {current_url}')
#     return ActionResult(extracted_content=f'current url is {current_url} and att is {attr}')

# async def SiteValidation():
    
#     task= (
#         'Important: I am UI Automation tester validating the tasks, if there are modals appear that block the continuation of the test during test, I will close them. I will use custom controllers if there are.'
#         'Open trendyol website. '
#         'Click the close icon on the gender section popup. '
#         'Login with testest23451@gmail.com username and Testest23451 password. '
#         'Click the hesabım from navbar after succesfull login. '
#         'Get specific email element attribute and current URL. '
#         # 'Go to the main page, select first 2 products and add them to cart, if there is a size selection choose one of the suitable selections. '
#         # 'Then checkout and store the total value you see in the screen. '
#         # 'Increase the quantitiy of any product and check if total value updates. '
#         # 'If asks for trendyoll pass choose no. '
#         # 'Verify website navigates to get the adress section. Do not fill the values. '
#     )
#     api_key = os.getenv('GEMINI_API_KEY')
#     llm=ChatGoogleGenerativeAI(model='gemini-2.0-flash-exp',api_key=SecretStr(api_key))
#     agent = Agent(task,llm,controller=controller,use_vision=True)
#     history=await agent.run()
#     history.save_to_file('agentresults.json')
#     test_result=history.final_result()

#     validated_result = CheckoutResult.model_validate_json(test_result)
#     print(validated_result)
#     assert validated_result.login_status==True
    
# asyncio.run(SiteValidation())    