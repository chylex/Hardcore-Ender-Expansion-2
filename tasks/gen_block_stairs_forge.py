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

contents_stairs_blockstate = f"""{{
  "forge_marker": 1,
  "defaults": {{
    "uvlock": true,
    "textures": {{
      "bottom": "hee:block/{block_registry_name}",
      "top": "hee:block/{block_registry_name}",
      "side": "hee:block/{block_registry_name}"
    }}
  }},
  "variants": {{
    "facing=east,half=bottom,shape=straight":  {{ "model": "stairs" }},
    "facing=west,half=bottom,shape=straight":  {{ "model": "stairs", "y": 180 }},
    "facing=south,half=bottom,shape=straight": {{ "model": "stairs", "y": 90 }},
    "facing=north,half=bottom,shape=straight": {{ "model": "stairs", "y": 270 }},
    "facing=east,half=bottom,shape=outer_right":  {{ "model": "outer_stairs" }},
    "facing=west,half=bottom,shape=outer_right":  {{ "model": "outer_stairs", "y": 180 }},
    "facing=south,half=bottom,shape=outer_right": {{ "model": "outer_stairs", "y": 90 }},
    "facing=north,half=bottom,shape=outer_right": {{ "model": "outer_stairs", "y": 270 }},
    "facing=east,half=bottom,shape=outer_left":  {{ "model": "outer_stairs", "y": 270 }},
    "facing=west,half=bottom,shape=outer_left":  {{ "model": "outer_stairs", "y": 90 }},
    "facing=south,half=bottom,shape=outer_left": {{ "model": "outer_stairs" }},
    "facing=north,half=bottom,shape=outer_left": {{ "model": "outer_stairs", "y": 180 }},
    "facing=east,half=bottom,shape=inner_right":  {{ "model": "inner_stairs" }},
    "facing=west,half=bottom,shape=inner_right":  {{ "model": "inner_stairs", "y": 180 }},
    "facing=south,half=bottom,shape=inner_right": {{ "model": "inner_stairs", "y": 90 }},
    "facing=north,half=bottom,shape=inner_right": {{ "model": "inner_stairs", "y": 270 }},
    "facing=east,half=bottom,shape=inner_left":  {{ "model": "inner_stairs", "y": 270 }},
    "facing=west,half=bottom,shape=inner_left":  {{ "model": "inner_stairs", "y": 90 }},
    "facing=south,half=bottom,shape=inner_left": {{ "model": "inner_stairs" }},
    "facing=north,half=bottom,shape=inner_left": {{ "model": "inner_stairs", "y": 180 }},
    "facing=east,half=top,shape=straight":  {{ "model": "stairs", "x": 180 }},
    "facing=west,half=top,shape=straight":  {{ "model": "stairs", "x": 180, "y": 180 }},
    "facing=south,half=top,shape=straight": {{ "model": "stairs", "x": 180, "y": 90 }},
    "facing=north,half=top,shape=straight": {{ "model": "stairs", "x": 180, "y": 270 }},
    "facing=east,half=top,shape=outer_right":  {{ "model": "outer_stairs", "x": 180, "y": 90 }},
    "facing=west,half=top,shape=outer_right":  {{ "model": "outer_stairs", "x": 180, "y": 270 }},
    "facing=south,half=top,shape=outer_right": {{ "model": "outer_stairs", "x": 180, "y": 180 }},
    "facing=north,half=top,shape=outer_right": {{ "model": "outer_stairs", "x": 180 }},
    "facing=east,half=top,shape=outer_left":  {{ "model": "outer_stairs", "x": 180 }},
    "facing=west,half=top,shape=outer_left":  {{ "model": "outer_stairs", "x": 180, "y": 180 }},
    "facing=south,half=top,shape=outer_left": {{ "model": "outer_stairs", "x": 180, "y": 90 }},
    "facing=north,half=top,shape=outer_left": {{ "model": "outer_stairs", "x": 180, "y": 270 }},
    "facing=east,half=top,shape=inner_right":  {{ "model": "inner_stairs", "x": 180, "y": 90 }},
    "facing=west,half=top,shape=inner_right":  {{ "model": "inner_stairs", "x": 180, "y": 270 }},
    "facing=south,half=top,shape=inner_right": {{ "model": "inner_stairs", "x": 180, "y": 180 }},
    "facing=north,half=top,shape=inner_right": {{ "model": "inner_stairs", "x": 180 }},
    "facing=east,half=top,shape=inner_left":  {{ "model": "inner_stairs", "x": 180 }},
    "facing=west,half=top,shape=inner_left":  {{ "model": "inner_stairs", "x": 180, "y": 180 }},
    "facing=south,half=top,shape=inner_left": {{ "model": "inner_stairs", "x": 180, "y": 90 }},
    "facing=north,half=top,shape=inner_left": {{ "model": "inner_stairs", "x": 180, "y": 270 }},
    "inventory": {{ "model": "stairs" }}
  }}
}}
"""

# Files

create_file("/blockstates/{}.json", contents_stairs_blockstate, "stairs forge block state")
