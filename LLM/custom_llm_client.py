import json
from typing import Any, Dict, List, Optional
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import BaseMessage, AIMessage, HumanMessage
from langchain_core.callbacks.manager import CallbackManagerForLLMRun
import requests
import os

class CustomAPILLM(BaseChatModel):
    """Custom LLM class for your specific API that returns Claude responses."""
    
    api_url: str
    
    model: str = "Claude 3.7"
    
    def _generate(
    self,
    messages: List[BaseMessage],
    stop: Optional[List[str]] = None,
    run_manager: Optional[CallbackManagerForLLMRun] = None,
    **kwargs: Any,
) -> Dict[str, Any]:
        # Code you already have for message formatting...
        
        # Make API request with better error handling
        try:
            headers = {
            
                "Content-Type": "application/json"
            }
            
            print(f"Making request to: {self.api_url}")
            response = requests.post(self.api_url, json=payload, headers=headers)
            
            # Print status code and headers for debugging
            print(f"Response status code: {response.status_code}")
            
            # Always try to print response content even if there's an error
            try:
                print(f"Response raw content preview: {response.text[:200]}...")
            except Exception as e:
                print(f"Could not print response content: {str(e)}")
            
            # If we got an error status code, log it and raise an exception
            if response.status_code >= 400:
                print(f"Error response from API: {response.status_code}")
                return {"generations": [{"text": f"API Error: {response.status_code}", "message": AIMessage(content=f"API Error: {response.status_code}")}]}
            
            # Try to parse as JSON
            try:
                result = response.json()
                print(f"Successfully parsed JSON response")
                
                # Debug the response structure
                print(f"Response keys: {list(result.keys())}")
                if "choices" in result:
                    print(f"Choices count: {len(result['choices'])}")
                    if len(result['choices']) > 0:
                        print(f"First choice keys: {list(result['choices'][0].keys())}")
                        if "message" in result['choices'][0]:
                            print(f"Message keys: {list(result['choices'][0]['message'].keys())}")
                
                # Extract the assistant's message content
                assistant_message = ""
                if "choices" in result and len(result["choices"]) > 0:
                    if "message" in result["choices"][0] and "content" in result["choices"][0]["message"]:
                        assistant_message = result["choices"][0]["message"]["content"]
                        print(f"Extracted message (first 100 chars): {assistant_message[:100]}...")
                    else:
                        print("Could not find message.content in the response")
                else:
                    print("Could not find choices in the response")
                
                return {"generations": [{"text": assistant_message, "message": AIMessage(content=assistant_message)}]}
            
            except json.JSONDecodeError as e:
                print(f"JSON parse error: {str(e)}")
                print(f"Non-JSON response: {response.text[:500]}")
                return {"generations": [{"text": f"JSON Parse Error: {str(e)}", "message": AIMessage(content=f"JSON Parse Error: {str(e)}")}]}
                
        except requests.exceptions.RequestException as e:
            print(f"Request error: {str(e)}")
            return {"generations": [{"text": f"Request Error: {str(e)}", "message": AIMessage(content=f"Request Error: {str(e)}")}]}
        except Exception as e:
            print(f"Unexpected error: {str(e)}")
            print(traceback.format_exc())
            return {"generations": [{"text": f"Unexpected Error: {str(e)}", "message": AIMessage(content=f"Unexpected Error: {str(e)}")}]}
    @property
    def _llm_type(self) -> str:
        return "custom-claude-api"



