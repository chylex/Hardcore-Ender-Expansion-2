function initializeCoreMod() {
	Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");
	
	var attackTargetEntityWithCurrentItemName = "func_71059_n";
	var attackTargetEntityWithCurrentItemDesc = "(Lnet/minecraft/entity/Entity;)V";
	
	var getSweepingDamageRatioMethod = api.mapMethod("func_191526_e");
	
	return {
		"HEE_SwordHookSweep": methodTransformer("net.minecraft.entity.player.PlayerEntity", attackTargetEntityWithCurrentItemName, attackTargetEntityWithCurrentItemDesc, function(method, instructions) {
			for (var index = 5, count = instructions.size(); index < count; index++) {
				var instr = instructions.get(index);
				
				if (instr.opcode == op.INVOKESTATIC && instr.name == getSweepingDamageRatioMethod) {
					instr = instructions.get(index - 5);
					
					if (instr.opcode == op.IFEQ) {
						instructions.insert(instr, makeInstructions(function(node) {
							node.visitVarInsn(op.ALOAD, 0);
							node.visitVarInsn(op.ALOAD, 1);
							node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/entity/VanillaDamageHooks", "shouldDisableSweep", "(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)Z");
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
