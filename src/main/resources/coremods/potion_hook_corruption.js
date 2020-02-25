function initializeCoreMod(){
    Java.type("net.minecraftforge.coremod.api.ASMAPI").loadFile("coremods/utils/bootstrap.js");

    function hookStart(instructions, setup){
        var start = instructions.get(0);

        instructions.insertBefore(start, makeInstructions(function(node){
            setup(node, getSkipInst(start));
        }));
    }

    function hookAttributeUpdate(instructions){
        hookStart(instructions, function(node, skipInst){
            node.visitVarInsn(op.ALOAD, 0);
            node.visitVarInsn(op.ALOAD, 1);
            node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/mechanics/potion/PotionCorruption", "shouldSkipAttributeChange", "(Lnet/minecraft/potion/Effect;Lnet/minecraft/entity/LivingEntity;)Z");
            node.visitJumpInsn(op.IFEQ, skipInst);
            node.visitInsn(op.RETURN);
        });
    }

    return {
        "HEE_PotionHookCorruption_Effect": methodTransformer("net.minecraft.potion.Effect", "func_180793_a" /* affectEntity */, "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/LivingEntity;ID)V", function(method, instructions){
            hookStart(instructions, function(node, skipInst){
                node.visitVarInsn(op.ALOAD, 0);
                node.visitVarInsn(op.ALOAD, 3);
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/mechanics/potion/PotionCorruption", "shouldCorrupt", "(Lnet/minecraft/potion/Effect;Lnet/minecraft/entity/LivingEntity;)Z");
                node.visitJumpInsn(op.IFEQ, skipInst);
                node.visitInsn(op.RETURN);
            });

            return method;
        }),

        "HEE_PotionHookCorruption_EffectInstance": methodTransformer("net.minecraft.potion.EffectInstance", "func_76457_b" /* performEffect */, "(Lnet/minecraft/entity/LivingEntity;)V", function(method, instructions){
            hookStart(instructions, function(node, skipInst){
                node.visitVarInsn(op.ALOAD, 0);
                node.visitVarInsn(op.ALOAD, 1);
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/mechanics/potion/PotionCorruption", "shouldCorrupt", "(Lnet/minecraft/potion/EffectInstance;Lnet/minecraft/entity/LivingEntity;)Z");
                node.visitJumpInsn(op.IFEQ, skipInst);
                node.visitInsn(op.RETURN);
            });

            return method;
        }),

        "HEE_PotionHookCorruption_LivingEntity": methodTransformer("net.minecraft.entity.LivingEntity", "func_70644_a" /* isPotionActive */, "(Lnet/minecraft/potion/Effect;)Z", function(method, instructions){
            hookStart(instructions, function(node, skipInst){
                node.visitVarInsn(op.ALOAD, 1);
                node.visitVarInsn(op.ALOAD, 0);
                node.visitMethodInsn(op.INVOKESTATIC, "chylex/hee/game/mechanics/potion/PotionCorruption", "shouldCorrupt", "(Lnet/minecraft/potion/Effect;Lnet/minecraft/entity/LivingEntity;)Z");
                node.visitJumpInsn(op.IFEQ, skipInst);
                node.visitInsn(op.ICONST_0);
                node.visitInsn(op.IRETURN);
            });

            return method;
        }),

        "HEE_PotionHookCorruption_Effect_AttrApply": methodTransformer("net.minecraft.potion.Effect", "func_111185_a" /* applyAttributesModifiersToEntity */, "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/ai/attributes/AbstractAttributeMap;I)V", function(method, instructions){
            hookAttributeUpdate(instructions);
            return method;
        }),

        "HEE_PotionHookCorruption_Effect_AttrRemove": methodTransformer("net.minecraft.potion.Effect", "func_111187_a" /* removeAttributesModifiersFromEntity */, "(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/ai/attributes/AbstractAttributeMap;I)V", function(method, instructions){
            hookAttributeUpdate(instructions);
            return method;
        })
    };
}
