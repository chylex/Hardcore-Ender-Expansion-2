package chylex.hee.game.world.structure.file
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Fallback
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureGenerator
import chylex.hee.game.world.structure.IStructureGeneratorFromFile
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getListOfStrings
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class StructureFile(nbt: NBTTagCompound){
	private val palette: Array<String>
	private val blocks = IntArrayList()
	
	val size: Size
	
	init{
		val tagPalette = nbt.getListOfStrings("Palette")
		val tagBlocks = nbt.getIntArray("Blocks")
		val tagSize = nbt.getIntArray("Size")
		
		palette = tagPalette.toList().toTypedArray()
		
		blocks.addElements(0, tagBlocks)
		blocks.trim()
		
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
	
	companion object{
		private val SKIP_BLOCK_STATE = ModBlocks.SCAFFOLDING.defaultState
		
		fun spawn(world: World, offset: BlockPos, piece: IStructureGeneratorFromFile, palette: Palette){
			return spawn(WorldToStructureWorldAdapter(world, world.rand, offset), piece, palette)
		}
		
		fun spawn(world: IStructureWorld, generator: IStructureGeneratorFromFile, palette: Palette){
			val path = generator.path
			val size = generator.size
			
			world.placeCube(size.minPos, size.maxPos, Single(SKIP_BLOCK_STATE))
			world.apply(StructureFiles.loadSkipCache(path).Generator(palette.mappingForDevelopment)::generate)
			world.finalize()
		}
		
		fun save(world: World, box: BoundingBox, palette: Palette): Pair<NBTTagCompound, Set<IBlockState>>{
			return save(WorldToStructureWorldAdapter(world, world.rand, box.min), box.size, palette)
		}
		
		fun save(world: IStructureWorld, size: Size, palette: Palette): Pair<NBTTagCompound, Set<IBlockState>>{
			if (size.x > 256 || size.y > 256 || size.z > 256){
				throw IllegalArgumentException("structure files can only contain structures up to 256x256x256 blocks")
			}
			
			val missingMappings = mutableSetOf<IBlockState>()
			
			val paletteMapping = palette.lookupForDevelopment
			val blockMapping = Int2ObjectArrayMap<String>()
			
			for(x in 0..size.maxX) for(y in 0..size.maxY) for(z in 0..size.maxZ){
				val pos = Pos(x, y, z)
				val state = world.getState(pos)
				
				if (state === SKIP_BLOCK_STATE){
					continue
				}
				
				val mapping = paletteMapping[state]
				
				if (mapping == null){
					missingMappings.add(state)
					continue
				}
				
				val key = (
					((pos.x and 255) shl 24) or
					((pos.y and 255) shl 16) or
					((pos.z and 255) shl  8)
				)
				
				blockMapping[key] = mapping // kotlin indexer boxes the values
			}
			
			val generatedBlocks = IntArrayList(blockMapping.size)
			val generatedPalette = blockMapping.values.distinct().sorted()
			
			if (generatedPalette.size > 256){
				throw IllegalArgumentException("structure files can only contain up to 256 different palette mappings")
			}
			
			for((key, mapping) in blockMapping){
				generatedBlocks.add(key or generatedPalette.indexOf(mapping))
			}
			
			val nbt = NBTTagCompound().also {
				it.setList("Palette", NBTObjectList.of(generatedPalette.asIterable()))
				it.setIntArray("Blocks", generatedBlocks.toIntArray())
				it.setIntArray("Size", intArrayOf(size.x, size.y, size.z))
			}
			
			return nbt to missingMappings
		}
	}
}
