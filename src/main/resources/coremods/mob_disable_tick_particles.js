function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    return {
        "HEE_MobDisableTickParticles_Enderman": methodTransformer("net.minecraft.entity.monster.EndermanEntity", "func_70636_d" /* livingTick */, "()V", function(method, instructions){
            for(var index = 5, count = instructions.size(); index < count; index++){
                if (instructions.get(index    ).opcode == op.ICONST_2 &&
                    instructions.get(index - 5).opcode == op.ICONST_0
                ){
                    var inserted = makeInstructions(function(node){
                        node.visitVarInsn(op.ALOAD, 0);
                        node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/entity/living/EntityMobAbstractEnderman", "livingParticleCount", "(Lnet/minecraft/entity/monster/EndermanEntity;)I");
                    });

                    instructions.set(instructions.get(index), inserted.getFirst());
                    instructions.insert(instructions.get(index), inserted.getLast());

                    return method;
                }
            }

            return null;
        }),

        "HEE_MobDisableTickParticles_Endermite": methodTransformer("net.minecraft.entity.monster.EndermiteEntity", "func_70636_d" /* livingTick */, "()V", function(method, instructions){
            for(var index = 5, count = instructions.size(); index < count; index++){
                if (instructions.get(index    ).opcode == op.ICONST_2 &&
                    instructions.get(index - 5).opcode == op.ICONST_0
                ){
                    instructions.set(instructions.get(index), makeInstructions(function(node){
                        node.visitInsn(op.ICONST_0);
                    }).getFirst());

                    return method;
                }
            }

            return null;
        })
    };
}
