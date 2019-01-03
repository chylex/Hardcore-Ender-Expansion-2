package chylex.hee.game.world.provider
import chylex.hee.game.world.territory.TerritoryInstance
import net.minecraft.block.BlockColored
import net.minecraft.entity.EnumCreatureType
import net.minecraft.init.Biomes
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.biome.Biome
import net.minecraft.world.biome.Biome.SpawnListEntry
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkPrimer
import net.minecraft.world.gen.IChunkGenerator

class ChunkGeneratorEndDebug(private val world: World) : IChunkGenerator{
	override fun generateChunk(x: Int, z: Int): Chunk{
		val primer = primeChunk(x, z)
		
		return Chunk(world, primer, x, z).apply {
			biomeArray.fill(Biome.getIdForBiome(Biomes.SKY).toByte())
			generateSkylightMap()
		}
	}
	
	private fun primeChunk(x: Int, z: Int) = ChunkPrimer().apply {
		generateDebugBlocks(x, z)
	}
	
	private fun ChunkPrimer.generateDebugBlocks(chunkX: Int, chunkZ: Int){
		val chunkStartX = chunkX * 16
		val chunkStartZ = chunkZ * 16
		
		val instance = TerritoryInstance.fromPos(chunkStartX + 8, chunkStartZ + 8) ?: return
		
		if (instance.generatesChunk(chunkX, chunkZ)){
			val territory = instance.territory
			val bottomCenter = instance.bottomCenterPos
			
			val centerX = bottomCenter.x - chunkStartX
			val bottomY = bottomCenter.y
			val centerZ = bottomCenter.z - chunkStartZ
			
			val block = Blocks.WOOL.defaultState.withProperty(BlockColored.COLOR, EnumDyeColor.values()[territory.ordinal % 16])
			
			for(blockX in 0..15){
				for(blockZ in 0..15){
					if (blockX == centerX || blockZ == centerZ){
						setBlockState(blockX, bottomY + 1, blockZ, block)
					}
					
					setBlockState(blockX, bottomY, blockZ, block)
				}
			}
		}
	}
	
	override fun populate(x: Int, z: Int){}
	
	override fun getPossibleCreatures(creatureType: EnumCreatureType, pos: BlockPos): List<SpawnListEntry>{
		return emptyList()
	}
	
	override fun generateStructures(chunk: Chunk, x: Int, z: Int): Boolean{
		return false
	}
	
	override fun recreateStructures(chunk: Chunk, x: Int, z: Int){}
	
	override fun isInsideStructure(world: World, structureName: String, pos: BlockPos): Boolean{
		return false
	}
	
	override fun getNearestStructurePos(world: World, structureName: String, position: BlockPos, findUnexplored: Boolean): BlockPos?{
		return null
	}
}
