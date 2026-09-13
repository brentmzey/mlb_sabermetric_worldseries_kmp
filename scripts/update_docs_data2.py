import os

md_files = ["README.md"]
for root, _, files in os.walk("docs"):
    for file in files:
        if file.endswith(".md"):
            md_files.append(os.path.join(root, file))
            
for filepath in md_files:
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
        
    new_content = content.replace("21.05%", "28.12%")
    
    if new_content != content:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(new_content)
        print(f"Updated {filepath}")
