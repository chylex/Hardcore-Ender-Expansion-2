package chylex.hee.system.core.transformers
import chylex.hee.system.core.ICoremodTransformer
import chylex.hee.system.core.findMethod
import chylex.hee.system.core.insertAfter
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.IFNE
import org.objectweb.asm.Opcodes.INSTANCEOF
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class TransformChunk : ICoremodTransformer{
	override val targetClass = "net.minecraft.world.chunk.Chunk"
	
	override fun process(node: ClassNode){
		disableForgeWorldgenInEnd(node)
	}
	
	private fun disableForgeWorldgenInEnd(node: ClassNode){
		val list = node.findMethod(
			"a",        "(Laxq;)V",
			"populate", "(Lnet/minecraft/world/gen/IChunkGenerator;)V"
		).instructions
		
		lateinit var beforeHook: LabelNode
		lateinit var afterHook: LabelNode
		
		for(insn in list.iterator()){
			if (insn.opcode == INVOKESTATIC && (insn as MethodInsnNode).name == "generateWorld"){
				beforeHook = list[list.indexOf(insn) - 12] as LabelNode
				afterHook = list[list.indexOf(insn) + 1] as LabelNode
				break
			}
		}
		
		list.insertAfter(beforeHook,
			VarInsnNode(ALOAD, 1),
			TypeInsnNode(INSTANCEOF, "chylex/hee/game/world/ChunkGeneratorEndCustom"),
			JumpInsnNode(IFNE, afterHook)
		)
	}
}
