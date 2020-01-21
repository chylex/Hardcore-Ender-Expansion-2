function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    var spawnDropsName = "func_220054_a";
    var spawnDropsDesc = "(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V";

    return {
        "HEE_VoidMinerPreventDrops": methodTransformer("net.minecraft.block.Block", spawnDropsName, spawnDropsDesc, function(method, instructions){
            var start = instructions.get(0);

            instructions.insertBefore(start, makeInstructions(function(node){
                node.visitVarInsn(op.ALOAD, 0);
                node.visitVarInsn(op.ALOAD, 1);
                node.visitVarInsn(op.ALOAD, 2);
                node.visitVarInsn(op.ALOAD, 5);
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/block/util/IBlockHarvestDropsOverride", "checkHarvest", "(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)Z");
                node.visitJumpInsn(op.IFEQ, getSkipInst(start));
                node.visitInsn(op.RETURN);
            }));

            printInstructions(instructions);
            return method;
        })
    };
}
