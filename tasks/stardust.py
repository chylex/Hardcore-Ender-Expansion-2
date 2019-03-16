import random

values = range(1, 12)
picks = 6
runs = 20
attempts = 200000

item = """
      {{
        "textures": {{
          "down": "hee:block/stardust_ore_{}",
          "up": "hee:block/stardust_ore_{}",
          "north": "hee:block/stardust_ore_{}",
          "south": "hee:block/stardust_ore_{}",
          "west": "hee:block/stardust_ore_{}",
          "east": "hee:block/stardust_ore_{}"
        }}
      }},"""

blocked = { 0: [], 1: [], 2: [], 3: [], 4: [], 5: [] }
superblocked = { 0: [], 1: [], 2: [], 3: [], 4: [], 5: [] }

generated = []

for attempt in range(attempts):
    gen_picks = random.sample(values, picks)
    same = 0
    
    for index in range(len(gen_picks)):
        if gen_picks[index] in superblocked[index]:
            same = 99
        elif gen_picks[index] in blocked[index]:
            same += 1
            break
    
    if (attempt < 100000 and same > 0) or (same > 1):
        continue
    
    generated.append(item.format(*gen_picks))
    
    for index in range(len(gen_picks)):
        if gen_picks[index] in blocked[index]:
            superblocked[index].append(gen_picks[index])
        else:
            blocked[index].append(gen_picks[index])
    
    if (len(generated) == runs):
        break

print("Generated: " + str(len(generated)))
print("")
print("".join(generated))
