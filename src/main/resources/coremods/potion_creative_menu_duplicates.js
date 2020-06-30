function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    var fillItemGroupName = "func_150895_a";
    var fillItemGroupDesc = "(Lnet/minecraft/item/ItemGroup;Lnet/minecraft/util/NonNullList;)V";

    return {
        "HEE_PotionCreativeMenuDuplicates": methodTransformer("net.minecraft.item.PotionItem", fillItemGroupName, fillItemGroupDesc, function(method, instructions){
            for(var index = 2, count = instructions.size(); index < count; index++){
                var instr = instructions.get(index);

                if (instr.opcode == op.IF_ACMPEQ){
                    var aload = instructions.get(index - 2);

                    if (aload.opcode == op.ALOAD){
                        instructions.insert(instr, makeInstructions(function(node){
                            node.visitVarInsn(op.ALOAD, aload.var);
                            node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/init/ModPotions", "excludeFromCreativeMenu", "(Lnet/minecraft/potion/Potion;)Z");
                            node.visitJumpInsn(op.IFNE, getSkipInst(instr.label));
                        }));

                        return method;
                    }
                }
            }

            return null;
        })
    };
}
