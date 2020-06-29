function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    var endStoneField = api.mapField("field_150377_bs"); // END_STONE

    var translateReplacements = function(replacements){
        var translated = {};

        for(var key in replacements){
            translated[api.mapMethod(key)] = replacements[key];
        }

        return translated;
    };

    var performReplacement = function(instructions){
        var replaced = 0;

        for(var index = 0, count = instructions.size(); index < count; index++){
            var instruction = instructions.get(index);

            if (instruction.opcode == op.GETSTATIC && instruction.name == endStoneField){
                instruction.owner = "chylex/hee/init/ModBlocks";
                instruction.name = "HUMUS";
                instruction.desc = "Lchylex/hee/game/block/BlockHumus;";
                ++replaced;
            }
        }

        return replaced;
    };

    var performReplacements = function(cls, replacements){
        var fail = false;
        var methods = cls.methods;

        for(var index = 0, count = methods.size(); index < count; index++){
            var method = methods.get(index);
            var key = method.name;

            if (replacements[key]){
                var instructions = method.instructions;

                var target = replacements[key];
                var replaced = performReplacement(instructions);

                if (replaced != target){
                    printInstructions(instructions);
                    api.log("ERROR", "Wrong amount of END_STONE -> HUMUS replacements in method " + key + " (" + replaced + " != " + target + ")");
                    fail = true;
                }

                replacements[key] = null;
            }
        }

        for(key in replacements){
            if (replacements[key] != null){
                api.log("ERROR", "Missed replacing END_STONE -> HUMUS in method " + key);
                fail = true;
            }
        }

        return fail ? null : cls;
    };

    return {
        "HEE_ChorusPlantsGrowOnHumus_Flower": classTransformer("net.minecraft.block.ChorusFlowerBlock", function(cls){
            return performReplacements(cls, translateReplacements({
                "func_227033_a_": 2, // tick
                "func_196260_a": 1, // isValidPosition
            }));
        }),

        "HEE_ChorusPlantsGrowOnHumus_Plant": classTransformer("net.minecraft.block.ChorusPlantBlock", function(cls){
            return performReplacements(cls, translateReplacements({
                "func_196497_a": 1, // makeConnections
                "func_196271_a": 1, // updatePostPlacement
                "func_196260_a": 2, // isValidPosition
            }));
        })
    };
}
