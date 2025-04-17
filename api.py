import asyncio
from types import SimpleNamespace
from flask import Flask, request, jsonify
import json
from case_generator import CaseGenerator, get_case_generator_prompt
from test_runner import run_test_and_generate_code
from flask_cors import CORS
app = Flask(__name__)
CORS(app, supports_credentials=True)

@app.route('/api/generate-test-cases', methods=['POST'])
def generate_test_cases():
    data = request.get_json()
    url = data.get('url')
    brief = data.get('brief')
    number_of_cases = data.get('number_of_cases', 1)
    
    if not url:
        return jsonify({'error': 'Missing required parameters'}), 400
        
    prompt = get_case_generator_prompt(url, brief, number_of_cases)
    generator = CaseGenerator()
    response = generator.send_request_to_llm(prompt=prompt)
    
    try:
        test_cases = json.loads(response)
        return jsonify(test_cases), 200
    except json.JSONDecodeError:
        return jsonify({'error': 'Failed to parse response'}), 500
    

@app.route('/api/run-test', methods=['POST'])
def runtest():
    data= request.get_json()
    test_case_data = data.get('test_case')
  

    if not test_case_data:
        return jsonify({'error': 'Missing required parameters'}), 400
    try:
        test_case_obj = SimpleNamespace(**test_case_data)

        result = asyncio.run(run_test_and_generate_code(test_case_obj))
        print(result)
        return jsonify(result), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)