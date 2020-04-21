function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    // TODO remove or move to another mod..

    return {
        "HEE_UnfuckAltGr": methodTransformer("net.minecraft.client.gui.screen.Screen", "hasControlDown", "()Z", function(method, instructions){
            var start = instructions.get(0);

            instructions.insertBefore(start, makeInstructions(function(node){
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/system/Debug", "cancelControlKey", "()Z");
                node.visitJumpInsn(op.IFEQ, getSkipInst(start));
                node.visitInsn(op.ICONST_0);
                node.visitInsn(op.IRETURN);
            }));

            return method;
        })
    };
}
