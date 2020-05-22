function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    var tryCatchFireName = "tryCatchFire"
    var tryCatchFireDesc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILjava/util/Random;ILnet/minecraft/util/Direction;)V"

    return {
        "HEE_FireFixInfusedTNTTileEntity": methodTransformer("net.minecraft.block.FireBlock", tryCatchFireName, tryCatchFireDesc, function(method, instructions){
            var start = instructions.get(0);

            instructions.insertBefore(start, makeInstructions(function(node){
                node.visitVarInsn(op.ALOAD, 1);
                node.visitVarInsn(op.ALOAD, 2);
                node.visitVarInsn(op.ILOAD, 3);
                node.visitVarInsn(op.ALOAD, 4);
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/block/util/IBlockFireCatchOverride", "tryCatchFire", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILjava/util/Random;)Z");
                node.visitJumpInsn(op.IFEQ, getSkipInst(start));
                node.visitInsn(op.RETURN);
            }));

            return method;
        })
    };
}
