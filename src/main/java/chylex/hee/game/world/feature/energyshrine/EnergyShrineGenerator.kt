package chylex.hee.game.world.feature.energyshrine
import chylex.hee.HEE
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.feature.OverworldFeatures.preloadChunks
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MAX
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.util.blocksMovement
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.square
import chylex.hee.system.util.xz
import net.minecraft.util.math.BlockPos
import net.minecraft.world.DimensionType.OVERWORLD
import net.minecraft.world.World
import net.minecraft.world.chunk.IChunkProvider
import net.minecraft.world.gen.IChunkGenerator
import net.minecraftforge.fml.common.IWorldGenerator
import java.util.Random

object EnergyShrineGenerator : IWorldGenerator{
	private const val GRID_CHUNKS = 27
	private const val GRID_Z_OFFSET = 9
	private val GRID_Z_OFFSET_CHECKS = intArrayOf(0, -1, 1)
	
	private const val DIST_CHUNKS_X = 9
	private const val DIST_CHUNKS_Z = 5
	
	private const val MIN_STRUCTURE_Y = 5
	private const val MIN_GROUND_LAYERS = 5
	
	private val STRUCTURE_SIZE
		get() = EnergyShrinePieces.STRUCTURE_SIZE
	
	// Search
	
	private fun findSpawnAtMaybe(world: World, chunkX: Int, chunkZ: Int): PosXZ?{
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		
		val xChunkColumn = startChunkX / GRID_CHUNKS
		val zChunkMod = (if (xChunkColumn < 0) (xChunkColumn % 3 + 3) else xChunkColumn) % 3
		val zChunkOffset = (zChunkMod - 1) * GRID_Z_OFFSET
		
		val centerChunkX = startChunkX + (GRID_CHUNKS / 2)
		val centerChunkZ = startChunkZ + (GRID_CHUNKS / 2) + zChunkOffset // causes the need to check positions at chunkZ +- 1
		
		val rand = Random((centerChunkX * 2750159L) + (centerChunkZ * 35926307L) + world.seed)
		
		if (rand.nextInt(9) == 0){
			return null
		}
		
		val centerPos = PosXZ(
			(centerChunkX + rand.nextInt(-DIST_CHUNKS_X, DIST_CHUNKS_X)) * 16 + rand.nextInt(16) + 8,
			(centerChunkZ + rand.nextInt(-DIST_CHUNKS_Z, DIST_CHUNKS_Z)) * 16 + rand.nextInt(16) + 8
		)
		
		if (StrongholdGenerator.findNearest(world, centerPos)?.takeIf { it.xz.distanceSqTo(centerPos) < square(160) } != null){
			return null
		}
		
		return centerPos
	}
	
	fun findNearest(world: World, xz: PosXZ): BlockPos?{ // does not guarantee a correct find
		val chunkX = xz.chunkX
		val chunkZ = xz.chunkZ
		
		val found = mutableListOf<PosXZ>()
		
		for(offX in -2..2) for(offZ in -3..3){ // extra z checks to maybe make it more reliable
			val testChunkX = chunkX + (GRID_CHUNKS * offX)
			val testChunkZ = chunkZ + (GRID_CHUNKS * offZ)
			
			findSpawnAtMaybe(world, testChunkX, testChunkZ)?.let(found::add)
		}
		
		return found.minBy { it.distanceSqTo(xz) }?.withY(0)
	}
	
	// Helpers
	
	private fun buildStructure(rand: Random): IStructureBuild?{
		for(attempt in 1..50){
			return EnergyShrineBuilder.build(rand) ?: continue
		}
		
		return null
	}
	
	private fun sampleBoundingBox(boundingBox: BoundingBox, rand: Random): Array<BlockPos>{
		val size = boundingBox.size
		val offset = boundingBox.min
		
		val amount = size.x + size.y + size.z
		
		return Array(amount){
			when(rand.nextInt(6)){
				0 -> offset.add(        0, rand.nextInt(size.y), rand.nextInt(size.z))
				1 -> offset.add(size.maxX, rand.nextInt(size.y), rand.nextInt(size.z))
				
				2 -> offset.add(rand.nextInt(size.x),         0, rand.nextInt(size.z))
				3 -> offset.add(rand.nextInt(size.x), size.maxY, rand.nextInt(size.z))
				
				4 -> offset.add(rand.nextInt(size.x), rand.nextInt(size.y), 0)
				5 -> offset.add(rand.nextInt(size.x), rand.nextInt(size.y), size.maxZ)
				
				else -> throw IllegalStateException()
			}
		}
	}
	
	private fun findStructureTop(world: World, rand: Random, build: IStructureBuild, topPos: BlockPos, structureOffset: BlockPos, structureHeight: Int): BlockPos?{
		val posSample = build.boundingBoxes.toList().flatMap { sampleBoundingBox(it, rand).asIterable() }
		val posSampleXZ = posSample.map(::PosXZ).toSet()
		
		val topPosBelowGround = topPos.offsetUntil(DOWN, 0 until (topPos.y - structureHeight - MIN_STRUCTURE_Y)){
			val origin = it.add(structureOffset)
			posSampleXZ.all { sample -> sample.add(origin.x, origin.z).getTopSolidOrLiquidBlock(world).y - it.y >= MIN_GROUND_LAYERS }
		}
		
		val minNonAir = (posSample.size * 9) / 10
		
		return topPosBelowGround?.offsetUntil(DOWN, 0 until (topPosBelowGround.y - structureHeight - MIN_STRUCTURE_Y)){
			val origin = it.add(structureOffset)
			posSample.count { sample -> sample.add(origin).blocksMovement(world) } >= minNonAir
		}
	}
	
	private fun findSurfaceAbove(world: World, bottomPos: BlockPos): BlockPos?{
		val surfaceAirRange = 1..7
		
		return bottomPos.offsetUntil(UP, 0 until (world.height - bottomPos.y)){
			var allowedFailures = 3
			
			for(xOffset in -1..1) for(zOffset in -1..1){
				val testPos = it.add(xOffset, 0, zOffset)
				
				if (!surfaceAirRange.all { yOffset -> EnergyShrinePillars.isReplaceable(world, testPos.up(yOffset)) } && --allowedFailures < 0){
					return@offsetUntil false
				}
			}
			
			return@offsetUntil true
		}
	}
	
	// Generation
	
	private fun findSpawnMatchingChunk(world: World, chunkX: Int, chunkZ: Int): PosXZ?{
		for(zOffset in GRID_Z_OFFSET_CHECKS){
			val foundPos = findSpawnAtMaybe(world, chunkX, chunkZ + GRID_CHUNKS * zOffset)
			
			if (foundPos != null && foundPos.chunkX == chunkX && foundPos.chunkZ == chunkZ){
				return foundPos
			}
		}
		
		return null
	}
	
	override fun generate(rand: Random, chunkX: Int, chunkZ: Int, world: World, generator: IChunkGenerator, provider: IChunkProvider){
		if (world.provider.dimensionType != OVERWORLD){
			return
		}
		
		val centerXZ = findSpawnMatchingChunk(world, chunkX, chunkZ) ?: return
		val build = buildStructure(rand)
		
		if (build == null){
			HEE.log.error("[EnergyShrineGenerator] failed all attempts at generating (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		val structureHeight = 1 + build.boundingBoxes.map { it.max.y }.max()!! - build.boundingBoxes.map { it.min.y }.min()!!
		
		val topPos = centerXZ.getTopSolidOrLiquidBlock(world)
		val topOffset = MIN_GROUND_LAYERS + rand.nextInt(0, 2)
		
		if (topPos.y - topOffset - structureHeight <= MIN_STRUCTURE_Y){
			HEE.log.error("[EnergyShrineGenerator] topmost block is too low (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		val structureOffset = BlockPos.ORIGIN.subtract(STRUCTURE_SIZE.getPos(CENTER, MAX, CENTER))
		val structurePos = findStructureTop(world, rand, build, topPos.down(topOffset), structureOffset, structureHeight)
		
		if (structurePos == null){
			HEE.log.error("[EnergyShrineGenerator] failed finding good altitude for structure (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		val surfacePos = findSurfaceAbove(world, structurePos.up(topOffset))
		
		if (surfacePos == null){
			HEE.log.error("[EnergyShrineGenerator] failed finding good altitude for surface pillars (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		if (!EnergyShrinePillars.tryGenerate(world, rand, surfacePos)){
			HEE.log.error("[EnergyShrineGenerator] failed generating surface pillars (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
			return
		}
		
		preloadChunks(world, chunkX, chunkZ, (STRUCTURE_SIZE.centerX / 16) - 1, (STRUCTURE_SIZE.centerZ / 16) - 1)
		WorldToStructureWorldAdapter(world, rand, structurePos.add(structureOffset)).apply(build::generate).finalize()
	}
}
