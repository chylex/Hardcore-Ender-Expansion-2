function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    var attackTargetEntityWithCurrentItemName = "func_71059_n";
    var attackTargetEntityWithCurrentItemDesc = "(Lnet/minecraft/entity/Entity;)V";

    var attackEntityAsMobName = "func_70652_k";
    var attackEntityAsMobDesc = "(Lnet/minecraft/entity/Entity;)Z";

    var attackDamageField = api.mapField("field_233823_f_");

    var applyHook = function(method, instructions){
        for(var index = 0, count = instructions.size() - 3; index < count; index++){
            var instr = instructions.get(index);

            if (instr.opcode == op.GETSTATIC && instr.name == attackDamageField){
                instr = instructions.get(index + 2);

                if (instr.opcode == op.D2F){
                    instructions.insert(instr, makeInstructions(function(node){
                        node.visitVarInsn(op.ALOAD, 0);
                        node.visitVarInsn(op.ALOAD, 1);
                        node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/mechanics/damage/VanillaDamageHooks", "getDamageMultiplier", "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/Entity;)F");
                        node.visitInsn(op.FMUL);
                    }));

                    return method;
                }
            }
        }

        return null;
    };

    return {
        "HEE_DamageMultiplierHook_Player": methodTransformer("net.minecraft.entity.player.PlayerEntity", attackTargetEntityWithCurrentItemName, attackTargetEntityWithCurrentItemDesc, applyHook),
        "HEE_DamageMultiplierHook_Mob": methodTransformer("net.minecraft.entity.MobEntity", attackEntityAsMobName, attackEntityAsMobDesc, applyHook)
    };
}
