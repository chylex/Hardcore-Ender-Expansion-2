package chylex.hee.game.world.generation.structure.file

import chylex.hee.HEE
import net.minecraft.nbt.CompressedStreamTools
import java.io.DataInputStream

object StructureFiles {
	private val cache = mutableMapOf<String, StructureFile>()
	
	fun loadSkipCache(path: String) = loadFromJar(path)
	fun loadWithCache(path: String) = cache.getOrPut(path) { loadFromJar(path) }
	
	private fun loadFromJar(path: String): StructureFile {
		val stream = javaClass.getResourceAsStream("/data/${HEE.ID}/structure/$path")
		val nbt = DataInputStream(stream!!).use(CompressedStreamTools::read)
		return StructureFile(nbt)
	}
	
	fun resetCache() {
		cache.clear()
	}
}
