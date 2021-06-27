package chylex.hee.game.world.feature.energyshrine

import chylex.hee.HEE
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.ENERGY_SHRINE_GENERATOR
import chylex.hee.game.world.blocksMovement
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.feature.OverworldFeatures
import chylex.hee.game.world.feature.OverworldFeatures.GeneratorTriggerBase
import chylex.hee.game.world.feature.OverworldFeatures.OverworldFeature
import chylex.hee.game.world.feature.OverworldFeatures.preloadChunks
import chylex.hee.game.world.feature.stronghold.StrongholdGenerator
import chylex.hee.game.world.generation.WorldToStructureWorldAdapter
import chylex.hee.game.world.math.BoundingBox
import chylex.hee.game.world.math.PosXZ
import chylex.hee.game.world.math.Size.Alignment.CENTER
import chylex.hee.game.world.math.Size.Alignment.MAX
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.structure.piece.IStructureBuild
import chylex.hee.game.world.xz
import chylex.hee.system.math.square
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.random.nextInt
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ISeedReader
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES
import net.minecraft.world.gen.Heightmap.Type.OCEAN_FLOOR
import net.minecraft.world.server.ServerWorld
import java.util.Random
import kotlin.math.min

object EnergyShrineGenerator : OverworldFeature() {
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
	
	private fun findSpawnAtMaybe(world: ServerWorld, chunkX: Int, chunkZ: Int): PosXZ? {
		val (startChunkX, startChunkZ) = OverworldFeatures.findStartChunkInGrid(GRID_CHUNKS, chunkX, chunkZ)
		
		val xChunkColumn = startChunkX / GRID_CHUNKS
		val zChunkMod = (if (xChunkColumn < 0) (xChunkColumn % 3 + 3) else xChunkColumn) % 3
		val zChunkOffset = (zChunkMod - 1) * GRID_Z_OFFSET
		
		val centerChunkX = startChunkX + (GRID_CHUNKS / 2)
		val centerChunkZ = startChunkZ + (GRID_CHUNKS / 2) + zChunkOffset // causes the need to check positions at chunkZ +- 1
		
		val rand = Random((centerChunkX * 2750159L) + (centerChunkZ * 35926307L) + world.seed)
		
		if (rand.nextInt(9) == 0) {
			return null
		}
		
		val centerPos = PosXZ(
			(centerChunkX + rand.nextInt(-DIST_CHUNKS_X, DIST_CHUNKS_X)) * 16 + rand.nextInt(16) + 8,
			(centerChunkZ + rand.nextInt(-DIST_CHUNKS_Z, DIST_CHUNKS_Z)) * 16 + rand.nextInt(16) + 8
		)
		
		if (StrongholdGenerator.findNearest(world, centerPos)?.takeIf { it.xz.distanceSqTo(centerPos) < square(160) } != null) {
			return null
		}
		
		return centerPos
	}
	
	fun findNearest(world: ServerWorld, xz: PosXZ): BlockPos? { // does not guarantee a correct find
		val chunkX = xz.chunkX
		val chunkZ = xz.chunkZ
		
		val found = mutableListOf<PosXZ>()
		
		for(offX in -2..2) for(offZ in -3..3) { // extra z checks to maybe make it more reliable
			val testChunkX = chunkX + (GRID_CHUNKS * offX)
			val testChunkZ = chunkZ + (GRID_CHUNKS * offZ)
			
			findSpawnAtMaybe(world, testChunkX, testChunkZ)?.let(found::add)
		}
		
		return found.minByOrNull { it.distanceSqTo(xz) }?.withY(0)
	}
	
	// Helpers
	
	private fun buildStructure(rand: Random): IStructureBuild? {
		for(attempt in 1..50) {
			return EnergyShrineBuilder.build(rand) ?: continue
		}
		
		return null
	}
	
	private fun sampleBoundingBox(boundingBox: BoundingBox, rand: Random): Array<BlockPos> {
		val size = boundingBox.size
		val offset = boundingBox.min
		
		val amount = size.x + size.y + size.z
		
		return Array(amount) {
			when(rand.nextInt(6)) {
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
	
	private fun getTopSolidNonLeavesBlock(world: World, xz: PosXZ): Int {
		val topBlockingNonLeaves = xz.getTopBlock(world, MOTION_BLOCKING_NO_LEAVES) // includes fluids so it cannot be used alone
		val topUnderwater = xz.getTopBlock(world, OCEAN_FLOOR)
		
		return min(topBlockingNonLeaves.y, topUnderwater.y)
	}
	
	private fun findStructureTop(world: World, rand: Random, build: IStructureBuild, topPos: BlockPos, structureOffset: BlockPos, structureHeight: Int): BlockPos? {
		val posSample = build.boundingBoxes.flatMap { sampleBoundingBox(it, rand).asIterable() }
		val posSampleXZ = posSample.map(::PosXZ).toSet()
		
		val topPosBelowGround = topPos.offsetUntil(DOWN, 0 until (topPos.y - structureHeight - MIN_STRUCTURE_Y)) {
			val origin = it.add(structureOffset)
			posSampleXZ.all { sample -> getTopSolidNonLeavesBlock(world, sample.add(origin.x, origin.z)) - it.y >= MIN_GROUND_LAYERS }
		}
		
		val minNonAir = (posSample.size * 9) / 10
		
		return topPosBelowGround?.offsetUntil(DOWN, 0 until (topPosBelowGround.y - structureHeight - MIN_STRUCTURE_Y)) {
			val origin = it.add(structureOffset)
			posSample.count { sample -> sample.add(origin).blocksMovement(world) } >= minNonAir
		}
	}
	
	private fun findSurfaceAbove(world: World, bottomPos: BlockPos): BlockPos? {
		val surfaceAirRange = 1..7
		
		return bottomPos.offsetUntil(UP, 0 until (world.height - bottomPos.y)) {
			var allowedFailures = 3
			
			for(xOffset in -1..1) for(zOffset in -1..1) {
				val testPos = it.add(xOffset, 0, zOffset)
				
				if (!surfaceAirRange.all { yOffset -> EnergyShrinePillars.isReplaceable(world, testPos.up(yOffset)) } && --allowedFailures < 0) {
					return@offsetUntil false
				}
			}
			
			return@offsetUntil true
		}
	}
	
	// Generation
	
	private fun findSpawnMatchingChunk(world: ServerWorld, chunkX: Int, chunkZ: Int): PosXZ? {
		for(zOffset in GRID_Z_OFFSET_CHECKS) {
			val foundPos = findSpawnAtMaybe(world, chunkX, chunkZ + GRID_CHUNKS * zOffset)
			
			if (foundPos != null && foundPos.chunkX == chunkX && foundPos.chunkZ == chunkZ) {
				return foundPos
			}
		}
		
		return null
	}
	
	override fun place(world: ISeedReader, rand: Random, pos: BlockPos, chunkX: Int, chunkZ: Int): Boolean {
		val wrld = world.world as ServerWorld
		val centerXZ = findSpawnMatchingChunk(wrld, chunkX, chunkZ) ?: return false
		
		EntityTechnicalTrigger(wrld, ENERGY_SHRINE_GENERATOR).apply {
			setLocationAndAngles(centerXZ.x + 0.5, 0.5, centerXZ.z + 0.5, 0F, 0F)
			world.addEntity(this)
		}
		
		return true
	}
	
	object GeneratorTrigger : GeneratorTriggerBase() {
		override fun place(world: ServerWorld, rand: Random, pos: BlockPos) {
			val xz = pos.xz
			val chunkX = xz.chunkX
			val chunkZ = xz.chunkZ
			
			val build = buildStructure(rand)
			
			if (build == null) {
				HEE.log.error("[EnergyShrineGenerator] failed all attempts at generating (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
				return
			}
			
			val boundingBoxes = build.boundingBoxes
			val structureHeight = 1 + boundingBoxes.maxOf { it.max.y } - boundingBoxes.minOf { it.min.y }
			
			val topY = getTopSolidNonLeavesBlock(world, xz)
			val topOffset = MIN_GROUND_LAYERS + rand.nextInt(0, 2)
			
			if (topY - topOffset - structureHeight <= MIN_STRUCTURE_Y) {
				HEE.log.error("[EnergyShrineGenerator] topmost block is too low (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
				return
			}
			
			val structureOffset = BlockPos.ZERO.subtract(STRUCTURE_SIZE.getPos(CENTER, MAX, CENTER))
			val structurePos = findStructureTop(world, rand, build, xz.withY(topY - topOffset), structureOffset, structureHeight)
			
			if (structurePos == null) {
				HEE.log.error("[EnergyShrineGenerator] failed finding good altitude for structure (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
				return
			}
			
			val surfacePos = findSurfaceAbove(world, structurePos.up(topOffset))
			
			if (surfacePos == null) {
				HEE.log.error("[EnergyShrineGenerator] failed finding good altitude for surface pillars (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
				return
			}
			
			if (!EnergyShrinePillars.tryGenerate(world, rand, surfacePos)) {
				HEE.log.error("[EnergyShrineGenerator] failed generating surface pillars (chunkX = $chunkX, chunkZ = $chunkZ, seed = ${world.seed})")
				return
			}
			
			preloadChunks(world, chunkX, chunkZ, (STRUCTURE_SIZE.centerX / 16) - 1, (STRUCTURE_SIZE.centerZ / 16) - 1)
			WorldToStructureWorldAdapter(world, rand, structurePos.add(structureOffset)).apply(build::generate).finalize()
		}
	}
}
