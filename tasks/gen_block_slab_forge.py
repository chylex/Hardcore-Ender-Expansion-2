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
  "forge_marker": 1,
  "defaults": {{
    "textures": {{
      "bottom": "hee:block/{block_registry_name}",
      "top": "hee:block/{block_registry_name}",
      "side": "hee:block/{block_registry_name}"
    }}
  }},
  "variants": {{
    "variant": {{
      "default": {{}}
    }},
    "half": {{
      "bottom": {{
        "model": "half_slab"
      }},
      "top": {{
        "model": "upper_slab"
      }}
    }},
    "inventory": {{
      "model": "half_slab"
    }}
  }}
}}
"""

contents_double_slab_blockstate = f"""{{
  "forge_marker": 1,
  "defaults": {{
    "model": "cube_all",
    "textures": {{
      "all": "hee:block/{block_registry_name}"
    }}
  }},
  "variants": {{
    "variant=default": [{{}}]
  }}
}}
"""

# Files

create_file("/blockstates/{}.json", contents_half_slab_blockstate, "half slab forge block state")
create_file("/blockstates/{}_double.json", contents_double_slab_blockstate, "double slab forge block state")
