function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    return {
        "HEE_MobBypassPeacefulDespawn": methodTransformer("net.minecraft.entity.monster.MonsterEntity", "func_70071_h_" /* tick */, "()V", function(method, instructions){
            for(var index = 0, count = instructions.size(); index < count; index++){
                if (instructions.get(index    ).opcode == op.IFNE &&
                    instructions.get(index + 5).opcode == op.IF_ACMPNE
                ){
                    var firstJump = instructions.get(index);

                    instructions.insert(firstJump, makeInstructions(function(node){
                        node.visitVarInsn(op.ALOAD, 0);
                        node.visitTypeInsn(op.INSTANCEOF, "chylex/hee/game/entity/IMobBypassPeacefulDespawn");
                        node.visitJumpInsn(op.IFNE, getSkipInst(firstJump.label));
                    }));

                    return method;
                }
            }

            return null;
        })
    };
}
