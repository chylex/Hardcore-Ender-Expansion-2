package chylex.hee.system.core
import org.apache.logging.log4j.LogManager
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceMethodVisitor
import kotlin.contracts.contract

fun MethodNode.printInstructions(){
	val logger = LogManager.getLogger("HEE")
	val visitor = TraceMethodVisitor(Textifier())
	
	for(instruction in instructions){
		instruction.accept(visitor)
	}
	
	for((index, obj) in visitor.p.text.withIndex()){
		logger.info("${(index + 1).toString().padStart(3, ' ')} : ${obj.toString().trimEnd()}")
	}
}

fun ClassNode.findMethod(name1: String, desc1: String, name2: String, desc2: String): MethodNode{
	return methods.first {
		(it.name == name1 && it.desc == desc1) ||
		(it.name == name2 && it.desc == desc2)
	}
}

fun AbstractInsnNode.fieldMatches(owner1: String, name1: String, owner2: String, name2: String): Boolean{
	contract { returns() implies (this@fieldMatches is FieldInsnNode) }
	
	if (this !is FieldInsnNode){
		throw UnsupportedOperationException("attempting to match field info on non-field instruction")
	}
	
	return (
		(owner == owner1 && name == name1) ||
		(owner == owner2 && name == name2)
	)
}

fun AbstractInsnNode.methodMatches(owner1: String, name1: String, desc1: String, owner2: String, name2: String, desc2: String): Boolean{
	contract { returns() implies (this@methodMatches is MethodInsnNode) }
	
	if (this !is MethodInsnNode){
		throw UnsupportedOperationException("attempting to match method info on non-method instruction")
	}
	
	return (
		(owner == owner1 && name == name1 && desc == desc1) ||
		(owner == owner2 && name == name2 && desc == desc2)
	)
}

fun InsnList.insertBefore(target: AbstractInsnNode, vararg instructions: AbstractInsnNode){
	this.insertBefore(target, InsnList().apply { instructions.forEach(::add) })
}

fun InsnList.insertAfter(target: AbstractInsnNode, vararg instructions: AbstractInsnNode){
	this.insert(target, InsnList().apply { instructions.forEach(::add) })
}
