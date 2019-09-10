package chylex.hee.system.core.transformers
import chylex.hee.system.core.ICoremodTransformer
import chylex.hee.system.core.fieldMatches
import chylex.hee.system.core.findMethod
import org.objectweb.asm.Opcodes.GETSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode

class TransformBlockChorusFlower : ICoremodTransformer{
	override val targetClass = "net.minecraft.block.BlockChorusFlower"
	
	override fun process(node: ClassNode){
		val updateTick = node.findMethod(
			"b",          "(Lamu;Let;Lawt;Ljava/util/Random;)V",
			"updateTick", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"
		)
		
		val canSurvive = node.findMethod(
			"b",          "(Lamu;Let;)Z",
			"canSurvive", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z"
		)
		
		replaceEndStoneGetters(updateTick, 2)
		replaceEndStoneGetters(canSurvive, 1)
	}
	
	private fun replaceEndStoneGetters(method: MethodNode, replacementCount: Int){
		val iterator = method.instructions.iterator()
		var replaced = 0
		
		for(insn in iterator){
			if (insn.opcode == GETSTATIC && insn.fieldMatches("aox", "bH", "net/minecraft/init/Blocks", "END_STONE")){
				iterator.set(FieldInsnNode(insn.opcode, "chylex/hee/init/ModBlocks", "HUMUS", "Lchylex/hee/game/block/BlockHumus;"))
				++replaced
			}
		}
		
		check(replaced == replacementCount){ "expected to do $replacementCount instruction replacement(s), did $replaced instead" }
	}
}
