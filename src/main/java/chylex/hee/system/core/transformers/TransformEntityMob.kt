package chylex.hee.system.core.transformers
import chylex.hee.system.core.ICoremodTransformer
import chylex.hee.system.core.findMethod
import chylex.hee.system.core.insertAfter
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.IFNE
import org.objectweb.asm.Opcodes.IF_ACMPNE
import org.objectweb.asm.Opcodes.INSTANCEOF
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

class TransformEntityMob : ICoremodTransformer{
	override val targetClass = "net.minecraft.entity.monster.EntityMob"
	
	override fun process(node: ClassNode){
		injectPeacefulDespawnBypass(node)
	}
	
	private fun injectPeacefulDespawnBypass(node: ClassNode){
		val list = node.findMethod(
			"B_",       "()V",
			"onUpdate", "()V"
		).instructions
		
		lateinit var firstJump: JumpInsnNode
		
		for(insn in list.iterator()){
			if (insn.opcode == IFNE && list[list.indexOf(insn) + 5].opcode == IF_ACMPNE){
				firstJump = insn as JumpInsnNode
				break
			}
		}
		
		list.insertAfter(firstJump,
			VarInsnNode(ALOAD, 0),
			TypeInsnNode(INSTANCEOF, "chylex/hee/game/entity/IMobBypassPeacefulDespawn"),
			JumpInsnNode(IFNE, firstJump.label)
		)
	}
}
