import re

def class_name_generator(name):
    # Convert to title case and remove spaces
    class_name = name.title().replace(" ", "")
    # Remove non-alphanumeric characters
    return re.sub(r'[^a-zA-Z0-9]', '', class_name)