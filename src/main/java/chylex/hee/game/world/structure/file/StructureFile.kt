package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Fallback
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Size
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getListOfStrings
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound

class StructureFile(nbt: NBTTagCompound){
	private val blocks = IntArrayList()
	private val palette: Array<String>
	
	val size: Size
	
	init{
		val tagPalette = nbt.getListOfStrings("Palette")
		val tagBlocks = nbt.getIntArray("Blocks")
		val tagSize = nbt.getIntArray("Size")
		
		blocks.addElements(0, tagBlocks)
		blocks.trim()
		
		palette = tagPalette.toList().toTypedArray()
		
		size = Size(tagSize[0], tagSize[1], tagSize[2])
	}
	
	inner class Generator(paletteMapping: Map<String, IBlockPicker>) : IStructureGenerator{
		private val generators = palette.map { paletteMapping[it] ?: Fallback("[StructureFile] skipping unknown palette identifier: $it", Blocks.AIR) }.toTypedArray()
		
		override val size = this@StructureFile.size
		
		override fun generate(world: IStructureWorld){
			val rand = world.rand
			
			for(block in blocks.elements()){
				val x = (block shr 24) and 255
				val y = (block shr 16) and 255
				val z = (block shr  8) and 255
				val id = block and 255
				
				world.setState(Pos(x, y, z), generators[id].pick(rand))
			}
		}
	}
}
