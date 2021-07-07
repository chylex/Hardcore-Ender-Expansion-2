package chylex.hee.game.world.generation.structure.file

import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.generation.IBlockPicker.Fallback
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.structure.IStructureGenerator
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.palette.Palette
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import chylex.hee.util.nbt.NBTObjectList
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getListOfStrings
import chylex.hee.util.nbt.putList
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks

class StructureFile(nbt: TagCompound) {
	private val palette: Array<String>
	private val blocks = IntArrayList()
	
	val size: Size
	
	init {
		val tagPalette = nbt.getListOfStrings(PALETTE_TAG)
		val tagBlocks = nbt.getIntArray(BLOCKS_TAG)
		val tagSize = nbt.getIntArray(SIZE_TAG)
		
		palette = tagPalette.toList().toTypedArray()
		
		blocks.addElements(0, tagBlocks)
		blocks.trim()
		
		size = Size(tagSize[0], tagSize[1], tagSize[2])
	}
	
	inner class Generator(paletteMapping: Map<String, IBlockPicker>) : IStructureGenerator {
		private val generators = palette.map { paletteMapping[it] ?: Fallback("[StructureFile] skipping unknown palette identifier: $it", Blocks.AIR) }.toTypedArray()
		
		override val size = this@StructureFile.size
		
		override fun generate(world: IStructureWorld) {
			val rand = world.rand
			
			for (block in blocks.elements()) {
				val x = (block shr 24) and 255
				val y = (block shr 16) and 255
				val z = (block shr 8) and 255
				val id = block and 255
				
				world.setState(Pos(x, y, z), generators[id].pick(rand))
			}
		}
	}
	
	companion object {
		private const val PALETTE_TAG = "Palette"
		private const val BLOCKS_TAG = "Blocks"
		private const val SIZE_TAG = "Size"
		
		fun spawn(world: IStructureWorld, generator: IStructurePieceFromFile, palette: Palette, scaffolding: Block) {
			val path = generator.path
			val size = generator.size
			
			world.placeCube(size.minPos, size.maxPos, Single(scaffolding))
			world.apply(StructureFiles.loadSkipCache(path).Generator(palette.mappingForDevelopment)::generate)
			world.finalize()
		}
		
		fun save(world: IStructureWorld, size: Size, palette: Palette, scaffolding: Block): Pair<TagCompound, Set<BlockState>> {
			require(size.x <= 256 && size.y <= 256 && size.z <= 256) { "structure files can only contain structures up to 256x256x256 blocks" }
			
			val missingMappings = mutableSetOf<BlockState>()
			
			val paletteMapping = palette.lookupForDevelopment
			val blockMapping = Int2ObjectArrayMap<String>()
			
			for (x in 0..size.maxX) for (y in 0..size.maxY) for (z in 0..size.maxZ) {
				val pos = Pos(x, y, z)
				val state = world.getState(pos)
				
				if (state.block === scaffolding) {
					continue
				}
				
				var mapping = paletteMapping[state]
				
				if (mapping == null) {
					missingMappings.add(state)
					mapping = paletteMapping[Blocks.AIR.defaultState] ?: continue
				}
				
				val key = (
					((pos.x and 255) shl 24) or
					((pos.y and 255) shl 16) or
					((pos.z and 255) shl  8)
				)
				
				@Suppress("ReplacePutWithAssignment")
				blockMapping.put(key, mapping) // kotlin indexer boxes the values
			}
			
			val generatedBlocks = IntArrayList(blockMapping.size)
			val generatedPalette = blockMapping.values.distinct().sorted()
			
			require(generatedPalette.size <= 256) { "structure files can only contain up to 256 different palette mappings" }
			
			for ((key, mapping) in blockMapping) {
				generatedBlocks.add(key or generatedPalette.indexOf(mapping))
			}
			
			val nbt = TagCompound().also {
				it.putList(PALETTE_TAG, NBTObjectList.of(generatedPalette.asIterable()))
				it.putIntArray(BLOCKS_TAG, generatedBlocks.toIntArray())
				it.putIntArray(SIZE_TAG, intArrayOf(size.x, size.y, size.z))
			}
			
			return nbt to missingMappings
		}
	}
}
