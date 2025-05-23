# 🤖 AIAutomation

An intelligent automation framework leveraging AI to generate robust Selenium TestNG test cases from JSON test data.

## ✨ Features

- 🧠 AI-powered test case generation
- 🔄 Dynamic Java Selenium TestNG code creation
- 📋 JSON-based test data support
- 🛠️ Enterprise-grade testing utilities
- 🔍 Smart element locator strategies

## 🚀 Getting Started

### Prerequisites

- Python 3.8+
- Java Development Kit (JDK) 11+
- Maven or Gradle for Java dependencies

### 📥 Installation

Clone the repository:

```bash
git clone https://github.com/yourusername/AIAutomation.git
cd AIAutomation
```

Install Python dependencies:

```bash
pip install -r requirements.txt
```

## 🔧 Configuration

1. Set up your Google Gemini API key as an environment variable:
   ```bash
   export GEMINI_API_KEY="your-api-key-here"
   ```

2. Prepare your test data in JSON format (see [examples directory](./examples))

## 📋 Usage

```python
from java_code_generator import JavaCodeGenerator
from test_case import TestCase

# Define a test case
test_case = TestCase(
    name="login_workflow",
    steps=["Navigate to login page", "Enter credentials", "Submit form", "Verify dashboard"]
)

# Generate test code from JSON data
generator = JavaCodeGenerator()
generator.generate_test_from_json("path/to/test_data.json", test_case)
```

## 🏗️ Project Structure

```
AIAutomation/
├── java_code_generator.py  # Main code generation logic
├── utils.py                # Utility functions
├── test_case.py            # Test case models
├── examples/               # Example JSON test data
├── generated_codes/        # Output directory for generated tests
└── requirements.txt        # Python dependencies
```

## 🛠️ Generated Test Features

The AI generates Java test files with:

- Smart element locator methods with fallback strategies
- Comprehensive screenshot and logging systems
- Advanced WebDriver configuration
- Modal and overlay handling
- Enterprise-grade test resilience
- Enhanced Page Object Architecture
- Internationalization & Accessibility support
- Comprehensive assertions framework
- Efficient test data management

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Contact

For questions or feedback, please [open an issue](https://github.com/yourusername/AIAutomation/issues) on this repository.
