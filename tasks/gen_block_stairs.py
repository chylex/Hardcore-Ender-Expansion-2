import os.path

# Setup

stairs_registry_name = input("Stairs registry name: ")

if not stairs_registry_name:
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
    formatted_path = relative_path.format(stairs_registry_name)
    
    try:
        with open(root_path + formatted_path, "x") as file:
            file.write(contents)
            
        print("Generated " + formatted_path)
    except FileExistsError:
        print("Skipping " + formatted_path)

# Contents

contents_blockstate = f"""{{
  "variants": {{
    "facing=east,half=bottom,shape=straight":  {{ "model": "hee:block/{stairs_registry_name}" }},
    "facing=west,half=bottom,shape=straight":  {{ "model": "hee:block/{stairs_registry_name}", "y": 180, "uvlock": true }},
    "facing=south,half=bottom,shape=straight": {{ "model": "hee:block/{stairs_registry_name}", "y": 90, "uvlock": true }},
    "facing=north,half=bottom,shape=straight": {{ "model": "hee:block/{stairs_registry_name}", "y": 270, "uvlock": true }},
    "facing=east,half=bottom,shape=outer_right":  {{ "model": "hee:block/{stairs_registry_name}_outer" }},
    "facing=west,half=bottom,shape=outer_right":  {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 180, "uvlock": true }},
    "facing=south,half=bottom,shape=outer_right": {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 90, "uvlock": true }},
    "facing=north,half=bottom,shape=outer_right": {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 270, "uvlock": true }},
    "facing=east,half=bottom,shape=outer_left":  {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 270, "uvlock": true }},
    "facing=west,half=bottom,shape=outer_left":  {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 90, "uvlock": true }},
    "facing=south,half=bottom,shape=outer_left": {{ "model": "hee:block/{stairs_registry_name}_outer" }},
    "facing=north,half=bottom,shape=outer_left": {{ "model": "hee:block/{stairs_registry_name}_outer", "y": 180, "uvlock": true }},
    "facing=east,half=bottom,shape=inner_right":  {{ "model": "hee:block/{stairs_registry_name}_inner" }},
    "facing=west,half=bottom,shape=inner_right":  {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 180, "uvlock": true }},
    "facing=south,half=bottom,shape=inner_right": {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 90, "uvlock": true }},
    "facing=north,half=bottom,shape=inner_right": {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 270, "uvlock": true }},
    "facing=east,half=bottom,shape=inner_left":  {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 270, "uvlock": true }},
    "facing=west,half=bottom,shape=inner_left":  {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 90, "uvlock": true }},
    "facing=south,half=bottom,shape=inner_left": {{ "model": "hee:block/{stairs_registry_name}_inner" }},
    "facing=north,half=bottom,shape=inner_left": {{ "model": "hee:block/{stairs_registry_name}_inner", "y": 180, "uvlock": true }},
    "facing=east,half=top,shape=straight":  {{ "model": "hee:block/{stairs_registry_name}", "x": 180, "uvlock": true }},
    "facing=west,half=top,shape=straight":  {{ "model": "hee:block/{stairs_registry_name}", "x": 180, "y": 180, "uvlock": true }},
    "facing=south,half=top,shape=straight": {{ "model": "hee:block/{stairs_registry_name}", "x": 180, "y": 90, "uvlock": true }},
    "facing=north,half=top,shape=straight": {{ "model": "hee:block/{stairs_registry_name}", "x": 180, "y": 270, "uvlock": true }},
    "facing=east,half=top,shape=outer_right":  {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 90, "uvlock": true }},
    "facing=west,half=top,shape=outer_right":  {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 270, "uvlock": true }},
    "facing=south,half=top,shape=outer_right": {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 180, "uvlock": true }},
    "facing=north,half=top,shape=outer_right": {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "uvlock": true }},
    "facing=east,half=top,shape=outer_left":  {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "uvlock": true }},
    "facing=west,half=top,shape=outer_left":  {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 180, "uvlock": true }},
    "facing=south,half=top,shape=outer_left": {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 90, "uvlock": true }},
    "facing=north,half=top,shape=outer_left": {{ "model": "hee:block/{stairs_registry_name}_outer", "x": 180, "y": 270, "uvlock": true }},
    "facing=east,half=top,shape=inner_right":  {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 90, "uvlock": true }},
    "facing=west,half=top,shape=inner_right":  {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 270, "uvlock": true }},
    "facing=south,half=top,shape=inner_right": {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 180, "uvlock": true }},
    "facing=north,half=top,shape=inner_right": {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "uvlock": true }},
    "facing=east,half=top,shape=inner_left":  {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "uvlock": true }},
    "facing=west,half=top,shape=inner_left":  {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 180, "uvlock": true }},
    "facing=south,half=top,shape=inner_left": {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 90, "uvlock": true }},
    "facing=north,half=top,shape=inner_left": {{ "model": "hee:block/{stairs_registry_name}_inner", "x": 180, "y": 270, "uvlock": true }}
  }}
}}
"""

contents_block_straight_model = f"""{{
  "parent": "block/stairs",
  "textures": {{
    "bottom": "hee:block/{block_registry_name}",
    "top": "hee:block/{block_registry_name}",
    "side": "hee:block/{block_registry_name}"
  }}
}}
"""

contents_block_inner_model = f"""{{
  "parent": "block/inner_stairs",
  "textures": {{
    "bottom": "hee:block/{block_registry_name}",
    "top": "hee:block/{block_registry_name}",
    "side": "hee:block/{block_registry_name}"
  }}
}}
"""

contents_block_outer_model = f"""{{
  "parent": "block/outer_stairs",
  "textures": {{
    "bottom": "hee:block/{block_registry_name}",
    "top": "hee:block/{block_registry_name}",
    "side": "hee:block/{block_registry_name}"
  }}
}}
"""

contents_item_model = f"""{{
  "parent": "hee:block/{stairs_registry_name}"
}}
"""

# Files

create_file("/blockstates/{}.json", contents_blockstate, "block state")
create_file("/models/block/{}.json", contents_block_straight_model, "block straight model")
create_file("/models/block/{}_inner.json", contents_block_inner_model, "block inner model")
create_file("/models/block/{}_outer.json", contents_block_outer_model, "block outer model")
create_file("/models/item/{}.json", contents_item_model, "item model")
