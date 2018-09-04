import os.path

# Setup

slab_registry_name = input("Slab registry name: ")

if not slab_registry_name:
    raise SystemExit

block_registry_name = input("Block registry name: ")

if not block_registry_name:
    raise SystemExit

root_path = "../src/main/resources/assets/hee"

if not os.path.isfile(root_path + f"/textures/block/{block_registry_name}.png"):
    print("Missing texture file")
    raise SystemExit

# Helpers

def create_file(relative_path, contents, title):
    formatted_path = relative_path.format(slab_registry_name)
    
    try:
        with open(root_path + formatted_path, "x") as file:
            file.write(contents)
            
        print("Generated " + formatted_path)
    except FileExistsError:
        print("Skipping " + formatted_path)

# Contents

contents_half_slab_blockstate = f"""{{
  "variants": {{
    "half=bottom,variant=default": {{ "model": "hee:{slab_registry_name}_bottom" }},
    "half=top,variant=default": {{ "model": "hee:{slab_registry_name}_top" }}
  }}
}}
"""

contents_double_slab_blockstate = f"""{{
  "variants": {{
    "variant=default": {{ "model": "hee:{block_registry_name}" }}
  }}
}}
"""

contents_block_slab_bottom_model = f"""{{
  "parent": "minecraft:block/half_slab",
  "textures": {{
    "bottom": "hee:block/{block_registry_name}",
    "top": "hee:block/{block_registry_name}",
    "side": "hee:block/{block_registry_name}"
  }}
}}
"""

contents_block_slab_top_model = f"""{{
  "parent": "minecraft:block/upper_slab",
  "textures": {{
    "bottom": "hee:block/{block_registry_name}",
    "top": "hee:block/{block_registry_name}",
    "side": "hee:block/{block_registry_name}"
  }}
}}
"""

contents_item_model = f"""{{
  "parent": "hee:block/{slab_registry_name}_bottom"
}}
"""

# Files

create_file("/blockstates/{}.json", contents_half_slab_blockstate, "half slab block state")
create_file("/blockstates/{}_double.json", contents_double_slab_blockstate, "double slab block state")
create_file("/models/block/{}_bottom.json", contents_block_slab_bottom_model, "slab bottom model")
create_file("/models/block/{}_top.json", contents_block_slab_top_model, "slab top model")
create_file("/models/item/{}.json", contents_item_model, "item model")
