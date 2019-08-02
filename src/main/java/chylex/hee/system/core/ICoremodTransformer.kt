package chylex.hee.system.core
import net.minecraft.launchwrapper.IClassTransformer
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_FRAMES
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.tree.ClassNode

interface ICoremodTransformer : IClassTransformer{
	val targetClass: String
	
	@JvmDefault
	val writerFlags
		get() = COMPUTE_FRAMES or COMPUTE_MAXS
	
	@JvmDefault
	override fun transform(name: String, transformedName: String, basicClass: ByteArray): ByteArray{
		if (transformedName != targetClass){
			return basicClass
		}
		
		return ClassNode().let {
			ClassReader(basicClass).accept(it, 0)
			process(it)
			ClassWriter(writerFlags).also(it::accept).toByteArray()
		}
	}
	
	fun process(node: ClassNode)
}
