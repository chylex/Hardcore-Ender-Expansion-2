import os.path

# Setup

registry_name = input("Block registry name: ")

if not registry_name:
    raise SystemExit

root_path = "../src/main/resources/assets/hee"

if not os.path.isfile(root_path + f"/textures/block/{registry_name}.png"):
    print("Missing texture file")
    raise SystemExit

# Helpers

def create_file(relative_path, contents, title):
    formatted_path = relative_path.format(registry_name)
    
    try:
        with open(root_path + formatted_path, "x") as file:
            file.write(contents)
            
        print("Generated " + formatted_path)
    except FileExistsError:
        print("Skipping " + formatted_path)

# Contents

contents_blockstate = f"""{{
  "variants": {{
    "normal": {{
      "model": "hee:{registry_name}"
    }}
  }}
}}
"""

contents_block_model = f"""{{
  "parent": "minecraft:block/cube_all",
  "textures": {{
    "all": "hee:block/{registry_name}"
  }}
}}
"""

contents_item_model = f"""{{
  "parent": "hee:block/{registry_name}"
}}
"""

# Files

create_file("/blockstates/{}.json", contents_blockstate, "block state")
create_file("/models/block/{}.json", contents_block_model, "block model")
create_file("/models/item/{}.json", contents_item_model, "item model")
