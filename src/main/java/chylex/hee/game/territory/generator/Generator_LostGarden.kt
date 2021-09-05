package chylex.hee.game.territory.generator

import chylex.hee.game.block.BlockSimpleMergingBottom
import chylex.hee.game.block.BlockWhitebarkLeaves
import chylex.hee.game.block.util.CHORUS_FLOWER_AGE
import chylex.hee.game.block.util.LEAVES_PERSISTENT
import chylex.hee.game.block.util.VINE_UP
import chylex.hee.game.block.util.with
import chylex.hee.game.territory.system.ITerritoryGenerator
import chylex.hee.game.territory.system.TerritoryGenerationInfo
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPlacer.BlockPlacer
import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.BlobPattern
import chylex.hee.game.world.generation.blob.BlobSmoothing
import chylex.hee.game.world.generation.blob.IBlobPopulator
import chylex.hee.game.world.generation.blob.PopulatorBuilder
import chylex.hee.game.world.generation.blob.layouts.BlobLayoutSingle
import chylex.hee.game.world.generation.blob.populators.BlobPopulatorCover
import chylex.hee.game.world.generation.blob.populators.BlobPopulatorShaveTop
import chylex.hee.game.world.generation.cave.CaveGenerator
import chylex.hee.game.world.generation.cave.impl.CaveCarverEllipsoid
import chylex.hee.game.world.generation.cave.impl.CavePatherRotatingBase
import chylex.hee.game.world.generation.cave.impl.CaveRadiusSine
import chylex.hee.game.world.generation.feature.basic.AutumnTreeGenerator
import chylex.hee.game.world.generation.feature.basic.PortalGenerator
import chylex.hee.game.world.generation.noise.NoiseGenerator
import chylex.hee.game.world.generation.noise.NoiseValue
import chylex.hee.game.world.generation.structure.world.ScaffoldedWorld
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.game.world.generation.trigger.FluidStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.allInCenteredBoxMutable
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.floodFill
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.offsetUntilExcept
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextVector
import chylex.hee.system.random.nextVector2
import chylex.hee.system.random.removeItem
import chylex.hee.system.random.removeItemOrNull
import chylex.hee.util.collection.WeightedList
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MAX
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.range
import chylex.hee.util.math.remapRange
import chylex.hee.util.math.scale
import chylex.hee.util.math.scaleY
import chylex.hee.util.math.square
import chylex.hee.util.math.xz
import chylex.hee.util.random.RandomDouble.Companion.Exp
import chylex.hee.util.random.RandomDouble.Companion.Linear
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SixWayBlock
import net.minecraft.block.VineBlock
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import java.util.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Generator_LostGarden : ITerritoryGenerator {
	override val segmentSize = Size(32, 8, 32)
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo {
		val rand = world.rand
		val size = world.worldSize
		
		val center = size.centerPos
		val islands = ArrayList<Island>(2)
		
		if (rand.nextBoolean()) {
			islands.add(Island(size.centerPos, radius = rand.nextFloat(253.0, 260.0), height = rand.nextFloat(55.0, 58.0)))
		}
		else {
			val offsetYaw1 = rand.nextFloat(0F, 360F)
			val offsetYaw2 = offsetYaw1 + rand.nextFloat(155F, 205F)
			
			val offsetDist = rand.nextFloat(163F, 165F)
			val heightDiff = rand.nextInt(3, 6) * (if (rand.nextBoolean()) -1 else 1)
			
			val center1 = center.add(Pos(Vec3.fromYaw(offsetYaw1).scale(offsetDist - 15F))).up(heightDiff + rand.nextInt(0, 1))
			val center2 = center.add(Pos(Vec3.fromYaw(offsetYaw2).scale(offsetDist + 25F))).down(heightDiff)
			
			islands.add(Island(center1, radius = rand.nextFloat(190.0, 194.0), height = rand.nextFloat(54.0, 56.0)))
			islands.add(Island(center2, radius = rand.nextFloat(150.0, 153.0), height = rand.nextFloat(54.0, 56.0)))
		}
		
		for (island in islands) {
			island.generate(world, rand)
		}
		
		CaveSystem().generate(world, size, rand, islands)
		
		repeat(rand.nextInt(4, rand.nextInt(6, 12))) {
			EnderGoo.generate(world, rand, size)
		}
		
		val blobs = mutableListOf<BlockPos>()
		
		repeat(rand.nextInt(3, 4)) {
			EndstoneBlob.generateNear(world, rand, size)?.let { blobs.add(it) }
		}
		
		repeat(rand.nextInt(7, rand.nextInt(9, 13))) {
			EndstoneBlob.generateFar(world, rand, size)
		}
		
		repeat(170) {
			Decorations.generateVineGroup(world, rand, size)
		}
		
		repeat(rand.nextInt(37, 42)) {
			Trees.generate(world, rand, size)
		}
		
		repeat(15) {
			Decorations.generateVineGroup(world, rand, size)
		}
		
		val spawnPoint = pickSpawnBlob(world, blobs) ?: pickSpawnFallback(world, size, rand, rand.nextItem(islands)) ?: center
		PortalGenerator.VoidPortalReturnActive.place(world, spawnPoint)
		
		return TerritoryGenerationInfo(spawnPoint)
	}
	
	private fun pickRandomPos(rand: Random, size: Size, y: Int, edge: Int): BlockPos {
		return Pos(
			rand.nextInt(edge, size.maxX - edge),
			y,
			rand.nextInt(edge, size.maxZ - edge)
		)
	}
	
	private fun pickSpawnBlob(world: SegmentedWorld, blobs: List<BlockPos>): BlockPos? {
		return blobs.sortedBy(world.worldSize.getPos(CENTER, MAX, CENTER)::distanceSqTo).firstOrNull { it.up().allInCenteredBoxMutable(2, 0, 2).all(world::isAir) }
	}
	
	private fun pickSpawnFallback(world: SegmentedWorld, size: Size, rand: Random, island: Island): BlockPos? {
		val xz = island.offset.xz
		val offsetRange = 1 until size.centerY
		
		for (attempt in 1..1000) {
			val pos = xz.add(rand.nextInt(-130, 130), rand.nextInt(-130, 130)).withY(size.maxY)
			
			val bottom = pos
				.allInCenteredBox(2, 0, 2)
				.mapNotNull { top -> top.offsetUntil(DOWN, offsetRange) { !world.isAir(it) } }
				.minByOrNull { it.y }
			
			if (bottom != null) {
				val testPos = Pos(pos.x, bottom.y, pos.z)
				
				if (testPos.allInCenteredBoxMutable(3, 0, 3).all { world.getBlock(it).let { block -> block === ModBlocks.ENDERSOL || block === Blocks.END_STONE } } &&
				    testPos.up(2).allInCenteredBoxMutable(2, 1, 2).all(world::isAir)
				) {
					return testPos
				}
			}
		}
		
		return null
	}
	
	private class Island(val offset: BlockPos, val radius: Double, val height: Double) {
		fun generate(world: SegmentedWorld, rand: Random) {
			val noiseXZ = NoiseGenerator.OldPerlinNormalized(rand, scale = 80.0, octaves = 3)
			val noiseXY = NoiseGenerator.OldPerlinNormalized(rand, scale = 32.0, octaves = 2)
			val noiseZY = NoiseGenerator.OldPerlinNormalized(rand, scale = 32.0, octaves = 2)
			
			val noiseValley = NoiseGenerator.OldPerlinNormalized(rand, scale = 136.0, octaves = 1)
			val noiseThreshold = NoiseGenerator.OldPerlinNormalized(rand, scale = 44.0, octaves = 2)
			val noiseEndersol = NoiseGenerator.OldPerlinNormalized(rand, scale = 32.0, octaves = 3)
			
			val maxDistXZ = radius.ceilToInt()
			val baseDistY = (height * 0.32).ceilToInt()
			val minDistY = -baseDistY
			val maxDistY = baseDistY * 2
			
			for (x in -maxDistXZ..maxDistXZ) for (z in -maxDistXZ..maxDistXZ) {
				val distRatioXZ = square(sqrt((square(x) + square(z)).toDouble()) / radius)
				
				if (distRatioXZ > 1.0) {
					continue
				}
				
				val edgeMpXZ = if (distRatioXZ > 0.86)
					remapRange(distRatioXZ.coerceAtMost(1.0), range(0.86F, 1F), range(1F, 0.86F * noiseXZ.getRawValue(-x * 3, -z * 3).toFloat()))
				else
					1.0
				
				val valueXZ = noiseXZ.getValue(x, z) {
					distanceReshapeXZ(distRatioXZ)
					multiply(edgeMpXZ)
				}
				
				val valueValley = 1.0 - noiseValley.getValue(x, z) {
					remap(range(0.5F, 1F), range(0F, 1F))
					coerce()
					redistribute(0.5)
					remap(range(0F, 0.75F))
					
					if (valueXZ < 0.6) {
						multiply(valueXZ / 0.6)
					}
				}
				
				val valueThreshold = noiseThreshold.getValue(x, z) {
					remap(range(0.14F, 0.29F))
				}
				
				val valueTotalXZ = valueXZ * valueValley
				
				val edgeMpY = (0.5 - (1.0 - edgeMpXZ))
				val endersolY = -0.125 + (0.575 * noiseEndersol.getValue(x, z) {
					if (value > 0.6) {
						remap(range(0.6F, 1F), range(0.6F, 5F))
					}
				})
				
				for (y in minDistY..maxDistY) {
					val distRatioY = (y / baseDistY.toDouble())
					val distRatioSqY = distRatioY.pow(2)
					
					val offsetEdge = (edgeMpY * square(1.0 - abs(distRatioSqY).coerceAtMost(1.0)))
					val offsetCliffs = if (distRatioY < 1.0) 0.4 else (0.4 - ((distRatioY - 1.0) * 0.4).coerceAtLeast(0.0))
					val offsetY = offsetEdge + offsetCliffs
					
					val valueXY = offsetY + noiseXY.getValue(x, y * 3) {
						distanceReshapeY(distRatioY)
						multiply(0.3)
					}
					
					val valueZY = offsetY + noiseZY.getValue(z, y * 3) {
						distanceReshapeY(distRatioY)
						multiply(0.3)
					}
					
					val valueTotalY = (max(valueXY, valueZY) + (valueXY + valueZY) * 0.5) * 0.5 + (0.1 - abs(distRatioY)).coerceIn(0.0, 0.1)
					
					if (valueTotalXZ * valueTotalY > valueThreshold) {
						val pos = offset.add(x, y, z)
						
						if (distRatioY < endersolY) {
							world.setBlock(pos, Blocks.END_STONE)
						}
						else {
							world.setState(pos, ModBlocks.ENDERSOL.with(BlockSimpleMergingBottom.MERGE, world.getBlock(pos.down()) === Blocks.END_STONE))
						}
					}
				}
			}
		}
		
		private fun NoiseValue.distanceReshapeXZ(distance: Double) {
			value = when (distance) {
				in (0.00)..(0.40) -> value * remapRange(distance, range(0F, 0.4F), range(0.8F, 1F))
				in (0.40)..(0.85) -> value
				in (0.85)..(1.00) -> value * remapRange(distance, range(0.85F, 1F), range(1F, 0F))
				else              -> 0.0
			}
		}
		
		private fun NoiseValue.distanceReshapeY(distance: Double) {
			value = when (distance) {
				in (-1.0)..(-0.6) -> value * square(remapRange(distance, range(-1F, -0.5F), range(0F, 1F)))
				in (-0.6)..( 0.5) -> value
				in ( 0.5)..( 0.8) -> value * remapRange(distance, range(0.5F, 0.8F), range(1F, 0.5F))
				in ( 0.8)..( 1.4) -> value * 0.5
				in ( 1.4)..( 2.0) -> value * remapRange(distance, range(1.4F, 2F), range(0.5F, 0.1F))
				else -> 0.0
			}
		}
	}
	
	private class CaveSystem {
		private companion object {
			private const val STEP_SIZE = 0.4
			
			private val PLACER = object : BlockPlacer(Blocks.AIR) {
				override fun place(world: SegmentedWorld, pos: BlockPos): Boolean {
					if (!super.place(world, pos)) {
						return false
					}
					
					val posAbove = pos.up()
					val stateAbove = world.getState(posAbove)
					
					if (stateAbove.block === ModBlocks.ENDERSOL && stateAbove[BlockSimpleMergingBottom.MERGE]) {
						world.setState(posAbove, stateAbove.with(BlockSimpleMergingBottom.MERGE, false))
					}
					
					return true
				}
			}
			
			private val CAVE_MAJOR = CaveGenerator(
				CaveCarverEllipsoid(radiusMpY = 0.75, maxRandomRadiusReduction = 0.1F),
				CaveRadiusSine(3.75, deviation = 0.35, frequency = 0.07, iterations = 4),
				PLACER,
				STEP_SIZE,
				maxConsecutiveFails = 13
			)
			
			private val CAVE_MINOR = CaveGenerator(
				CaveCarverEllipsoid(radiusMpY = 0.75, maxRandomRadiusReduction = 0.08F),
				CaveRadiusSine(3.25, deviation = 0.25, frequency = 0.07, iterations = 3),
				PLACER,
				STEP_SIZE,
				maxConsecutiveFails = 2
			)
		}
		
		private val majorCaveStarts = mutableListOf<Vector3d>()
		private val randomMinorCaveStarts = mutableListOf<Pair<BlockPos, Island>>()
		
		private val randomLargeHoleCandidates = mutableListOf<BlockPos>()
		private val randomHoles = mutableListOf<BlockPos>()
		
		fun generate(world: SegmentedWorld, size: Size, rand: Random, islands: List<Island>) {
			var maxCaveSteps = rand.nextInt(7, 8) * (210.0 / STEP_SIZE).floorToInt()
			
			while (maxCaveSteps > 0) {
				maxCaveSteps -= generateMajorCave(world, rand, rand.nextItem(islands))
				maxCaveSteps -= 1
			}
			
			maxCaveSteps = rand.nextInt(14, 17) * (130.0 / STEP_SIZE).floorToInt()
			
			while (maxCaveSteps > 0 && randomMinorCaveStarts.isNotEmpty()) {
				maxCaveSteps -= generateMinorCave(world, rand, rand.removeItem(randomMinorCaveStarts))
				maxCaveSteps -= 1
			}
			
			repeat(rand.nextInt(3, 4)) {
				generateLargeHole(world, size, rand)
			}
		}
		
		private fun generateMajorCave(world: SegmentedWorld, rand: Random, island: Island): Int {
			val radius = island.radius.floorToInt()
			val height = (island.height * 0.8).floorToInt()
			
			for (attempt in 1..100) {
				val startPos = island.offset.add(
					rand.nextInt(-radius, radius),
					rand.nextInt(-10, height),
					rand.nextInt(-radius, radius)
				).offsetUntil(UP, 0..20) {
					world.isInside(it) && world.isAir(it)
				} ?: continue
				
				if ((0 until (startPos.y)).count { !world.isAir(startPos.down(it)) } < 30) {
					continue
				}
				
				val start = startPos.center
				
				if (majorCaveStarts.any { start.squareDistanceTo(it) < square(36) }) {
					continue
				}
				
				val dir = rand.nextVector2(xz = 1.0, y = -rand.nextFloat(0.1, 0.8)).normalize()
				
				if (doubleArrayOf(4.0, 10.0, 16.0).all { world.isAir(Pos(start.add(dir.scale(it)))) }) {
					continue
				}
				
				val pather = Pather(dir, island)
				val steps = CAVE_MAJOR.generate(world, start, rand.nextFloat(160.0, 280.0), pather)
				val length = steps * STEP_SIZE
				
				if (length > 60.0) {
					repeat(rand.nextInt(0, (length / 120.0).roundToInt().coerceIn(1, 2))) {
						val radiusMpY = rand.nextFloat(0.75, 1.0)
						val randomPos = rand.nextItem(pather.randomPositions).center
						val randomRad = rand.nextFloat(5.1, 7.6)
						
						CaveCarverEllipsoid(radiusMpY = radiusMpY, powerXYZ = 2.5, maxRandomRadiusReduction = 0.05F).carve(world, randomPos, randomRad, PLACER)
					}
				}
				
				if (length > 40.0) {
					repeat(1 + (rand.nextInt(0, 2) * rand.nextInt(1, 3))) {
						val candidate1 = rand.nextItemOrNull(pather.randomPositions)
						val candidate2 = rand.nextItemOrNull(pather.randomPositions)
						
						if (candidate1 != null && candidate2 != null) {
							randomMinorCaveStarts.add((if (candidate1.y < candidate2.y) candidate1 else candidate2) to island)
						}
					}
				}
				
				majorCaveStarts.add(start)
				randomLargeHoleCandidates.addAll(pather.randomPositions)
				
				return steps
			}
			
			return 0
		}
		
		private fun generateMinorCave(world: SegmentedWorld, rand: Random, place: Pair<BlockPos, Island>): Int {
			val start = place.first.center
			val island = place.second
			
			for (attempt in 1..10) {
				val dir = rand.nextVector2(xz = 1.0, y = rand.nextFloat(-0.14, 0.07)).normalize()
				
				if (world.isAir(Pos(start.add(dir.scale(8.0))))) {
					continue
				}
				
				val pather = Pather(dir, island)
				val steps = CAVE_MINOR.generate(world, start.add(dir), rand.nextFloat(90.0, 160.0), pather)
				
				if (steps > (20.0 / STEP_SIZE)) {
					rand.nextItemOrNull(pather.randomPositions)?.let { randomMinorCaveStarts.add(it to island) }
				}
				
				randomLargeHoleCandidates.addAll(pather.randomPositions)
				return steps
			}
			
			return 0
		}
		
		private fun generateLargeHole(world: SegmentedWorld, size: Size, rand: Random) {
			val offsetRange = 0 until size.y
			
			for (attempt in 1..50) {
				val center = rand.removeItemOrNull(randomLargeHoleCandidates) ?: break
				
				if (randomHoles.any { it.distanceSqTo(center) < square(110) }) {
					continue
				}
				
				if (Facing4.any { world.isAir(center.offset(it, 12)) }) {
					continue
				}
				
				val xz = center.xz
				val bottom = xz.withY(0).offsetUntil(UP, offsetRange) { !world.isAir(it) } ?: continue
				val top = xz.withY(size.maxY).offsetUntil(DOWN, offsetRange) { !world.isAir(it) } ?: continue
				
				if (center.y - bottom.y > 12 && top.y - center.y > 16) {
					val noise1 = NoiseGenerator.OldPerlinNormalized(rand, scale = 8.0, octaves = 2)
					val noise2 = NoiseGenerator.OldPerlinNormalized(rand, xScale = 8.0, zScale = 4.0, octaves = 2)
					
					val radiusX = rand.nextFloat(11.0, 16.0)
					val radiusY = rand.nextFloat(7.2, 9.4)
					val radiusZ = rand.nextFloat(11.0, 16.0)
					
					for (offset in BlockPos.ZERO.allInCenteredBoxMutable(radiusX, radiusY, radiusZ)) {
						val distX = (abs(offset.x) / radiusX).pow(4.0)
						val distY = (abs(offset.y) / radiusY).pow(4.0)
						val distZ = (abs(offset.z) / radiusZ).pow(4.0)
						
						val noise =
							noise1.getValue(offset.x, offset.z) { multiply(0.2) } +
							noise2.getValue(offset.x, offset.y) { multiply(0.1) } +
							noise2.getValue(63 + offset.z, offset.y) { multiply(0.1) }
						
						if (distX + distY + distZ <= 1F - noise) {
							PLACER.place(world, center.add(offset))
							randomHoles.add(center)
						}
					}
					
					break
				}
			}
		}
		
		private class Pather(initialDirection: Vector3d, island: Island) : CavePatherRotatingBase(initialDirection) {
			val randomPositions = mutableListOf<BlockPos>()
			private var waitRandomPositions = 7
			
			private val thresholdMinY = island.offset.y - (island.height * 0.3).floorToInt()
			private val thresholdMaxY = island.offset.y + (island.height * 0.4).floorToInt()
			
			override fun update(rand: Random, point: Vector3d) {
				val off = rand.nextVector(square(rand.nextFloat(0.1, 0.8)).coerceAtLeast(0.1)).scaleY(0.8)
				rotation = rotation.add(off).normalize()
				
				if (abs(rotation.y) > 0.7) {
					rotation = rotation.scaleY(0.9)
				}
				
				if (rand.nextInt(16) == 0) {
					rotation = rotation.scale(0.1).normalize()
				}
				
				if (rand.nextBoolean()) {
					if (point.y < thresholdMinY) {
						rotation = rotation.addY(0.3)
					}
					else if (point.y > thresholdMaxY) {
						rotation = rotation.addY(-0.3)
					}
				}
				
				if (--waitRandomPositions < 0) {
					waitRandomPositions = rand.nextInt(21, 55)
					randomPositions.add(Pos(point))
				}
			}
		}
	}
	
	private object EnderGoo {
		private val FLUID = FluidStructureTrigger(ModBlocks.PURIFIED_ENDER_GOO)
		
		fun generate(world: SegmentedWorld, rand: Random, size: Size) {
			val offsetRange = 1 until (size.maxY / 3)
			
			attempts@ for (attempt in 1..5000) {
				val pos = pickRandomPos(rand, size, y = rand.nextInt(size.centerY, size.maxY), edge = 32)
					          .offsetUntil(DOWN, offsetRange) { !world.isAir(it) }
					          ?.takeUnless { world.getBlock(it) === ModBlocks.PURIFIED_ENDER_GOO }
					          ?.up() ?: continue
				
				if (!Facing4.all { pos.offsetUntil(it, 1..10) { testPos -> !world.isAir(testPos) } != null }) {
					continue
				}
				
				if (generatePool(world, rand, pos)) {
					break
				}
			}
		}
		
		private fun generatePool(world: SegmentedWorld, rand: Random, pos: BlockPos): Boolean {
			lateinit var topPlane: List<BlockPos>
			
			for (y in 0..4) {
				val limit = 125 * (y + 1)
				val plane = pos.up(y).floodFill(Facing4, limit) { world.isInside(it) && world.isAir(it) }
				
				if (plane.size !in 32 until limit || (y == 0 && plane.any { world.isAir(it.down()) })) {
					if (y == 0) {
						return false
					}
					else {
						break
					}
				}
				
				plane.forEach { world.addTrigger(it, FLUID) }
				topPlane = plane
			}
			
			repeat(rand.nextInt(1, 3)) {
				for (attempt in 1..250) {
					val streamPos = findTopStream(world, rand.nextItem(topPlane))
					
					if (streamPos != null) {
						world.addTrigger(streamPos, FLUID)
						break
					}
				}
			}
			
			repeat(rand.nextInt(0, rand.nextInt(1, 2))) {
				for (attempt in 1..100) {
					val streamPos = findSideStream(world, rand, rand.nextItem(topPlane))
					
					if (streamPos != null) {
						world.addTrigger(streamPos, FLUID)
						break
					}
				}
			}
			
			return true
		}
		
		private fun isGoo(world: SegmentedWorld, pos: BlockPos): Boolean {
			return world.getBlock(pos) === ModBlocks.PURIFIED_ENDER_GOO
		}
		
		private fun blocksGoo(world: SegmentedWorld, pos: BlockPos): Boolean {
			return !world.isAir(pos) && !isGoo(world, pos)
		}
		
		private fun findTopStream(world: SegmentedWorld, pos: BlockPos): BlockPos? {
			return pos
				.offsetUntil(UP, 1..9) { !world.isAir(it) }
				?.takeIf { testPos -> !isGoo(world, testPos) && blocksGoo(world, testPos.up()) && Facing4.all { blocksGoo(world, testPos.offset(it)) } }
		}
		
		private fun findSideStream(world: SegmentedWorld, rand: Random, pos: BlockPos): BlockPos? {
			return pos
				.up(rand.nextInt(1, 3))
				.takeIf(world::isAir)
				?.offsetUntil(rand.nextItem(Facing4), 1..10) { !world.isAir(it) }
				?.takeIf { testPos -> !isGoo(world, testPos) && blocksGoo(world, testPos.up()) && Facing4.count { !blocksGoo(world, testPos.offset(it)) } == 1 }
		}
	}
	
	private object Trees {
		fun generate(world: SegmentedWorld, rand: Random, size: Size) {
			val topY = size.maxY
			val offsetRange = 1 until size.centerY
			
			for (attempt in 1..200) {
				val pos = pickRandomPos(rand, size, topY, edge = 16).offsetUntil(DOWN, offsetRange) { canPlantOnTop(world, it) } ?: continue
				
				val options = mutableListOf(
					AutumnTreeGenerator.Red,
					AutumnTreeGenerator.Orange,
					AutumnTreeGenerator.Brown,
					AutumnTreeGenerator.YellowGreen
				)
				
				val weights = mutableListOf(
					10 to rand.removeItem(options)
				)
				
				repeat(2) {
					if (rand.nextInt(6) == 0) {
						weights.add(rand.nextInt(1, 7) to rand.removeItem(options))
					}
				}
				
				generateGroup(world, rand, pos.up(), WeightedList(weights))
				break
			}
		}
		
		private fun generateGroup(world: SegmentedWorld, rand: Random, firstPos: BlockPos, weights: WeightedList<AutumnTreeGenerator>) {
			weights.generateItem(rand).generate(world, firstPos)
			
			val positions = mutableListOf(firstPos)
			val density = rand.nextInt(3, 9)
			val heights = rand.nextInt(6, 9).let { it..(it + rand.nextInt(1, 2)) }
			
			repeat(rand.nextInt(4, 24)) {
				val basePos = rand.nextItem(positions)
				
				for (attempt in 1..50) {
					val testPos = basePos.add(
						rand.nextInt(density, 11) * (if (rand.nextBoolean()) -1 else 1),
						4,
						rand.nextInt(density, 11) * (if (rand.nextBoolean()) -1 else 1)
					).offsetUntilExcept(DOWN, 0..8) {
						canPlantOnTop(world, it)
					}
					
					if (testPos != null && world.isAir(testPos)) {
						val generator = weights.generateItem(rand)
						
						if (rand.nextInt(71) == 0) {
							world.setState(testPos, generator.leafBlock.with(LEAVES_PERSISTENT, true))
						}
						else {
							generator.generate(world, testPos, heights)
						}
						
						positions.add(testPos)
						
						if (rand.nextInt(3) != 0) {
							positions.remove(basePos)
						}
					}
				}
			}
		}
		
		private fun canPlantOnTop(world: SegmentedWorld, pos: BlockPos): Boolean {
			return world.getBlock(pos).let { it === Blocks.END_STONE || it === ModBlocks.ENDERSOL }
		}
	}
	
	private object EndstoneBlob {
		private val GENERATOR = BlobGenerator(ModBlocks.ENDERSOL)
		
		private val PATTERN = BlobPattern(
			BlobLayoutSingle(Exp(4.7, 5.8, exp = 1.25)),
			PopulatorBuilder().apply {
				guarantee(
					BlobPopulatorShaveTop(
						height = Linear(0.32, 0.46)
					),
					BlobPopulatorCover(
						Single(ModBlocks.HUMUS.with(BlockSimpleMergingBottom.MERGE, true)),
						replace = true
					),
					BlobPopulatorExtendHumus
				)
			}
		)
		
		private object BlobPopulatorExtendHumus : IBlobPopulator {
			override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator) {
				val size = world.worldSize
				val y = size.getPos(CENTER, MAX, CENTER).offsetUntil(DOWN, 0..size.maxY) { world.getBlock(it) === ModBlocks.HUMUS }?.y ?: return
				
				for (x in 0..size.maxX) for (z in 0..size.maxZ) {
					val pos = Pos(x, y, z)
					
					if (world.getBlock(pos) === ModBlocks.HUMUS && world.getBlock(pos.down(2)) !== ModBlocks.ENDERSOL) {
						return
					}
				}
				
				for (x in 0..size.maxX) for (z in 0..size.maxZ) {
					val pos = Pos(x, y, z)
					
					if (world.getBlock(pos) === ModBlocks.HUMUS) {
						world.setState(pos, ModBlocks.HUMUS.with(BlockSimpleMergingBottom.MERGE, false))
						world.setState(pos.down(), ModBlocks.HUMUS.with(BlockSimpleMergingBottom.MERGE, true))
					}
				}
			}
		}
		
		fun generateNear(world: SegmentedWorld, rand: Random, size: Size): BlockPos? {
			val edge = min(size.maxX, size.maxZ) / 4
			
			for (attempt in 1..500) {
				val pos = pickRandomPos(rand, size, y = rand.nextInt(size.centerY - 9, size.centerY + 9), edge = (edge - (attempt / 4)).coerceAtLeast(20))
				val posBelow = pos.down(5)
				val posAbove = pos.up(3)
				
				if (world.isAir(pos) &&
				    Facing6.all { checkAir(world, pos, it) } &&
				    Facing4.any { checkBlocks(world, posBelow, it) && checkBlocks(world, posAbove, it) } &&
				    checkAround(world, rand, pos)
				) {
					generateIsland(world, rand, pos)
					return pos
				}
			}
			
			return null
		}
		
		fun generateFar(world: SegmentedWorld, rand: Random, size: Size) {
			for (attempt in 1..100) {
				val pos = pickRandomPos(rand, size, y = rand.nextInt(size.centerY - 11, size.centerY + 7), edge = 10)
				
				if (world.isAir(pos) && Facing6.all { checkAir(world, pos, it) } && pos.distanceSqTo(size.centerPos) > square(200.0) && checkAround(world, rand, pos)) {
					generateIsland(world, rand, pos)
					break
				}
			}
		}
		
		private fun generateIsland(world: SegmentedWorld, rand: Random, pos: BlockPos) {
			GENERATOR.generate(world, rand, pos, BlobSmoothing.FULL, PATTERN)
			
			repeat(rand.nextInt(2, 7)) {
				for (plantAttempt in 1..3) {
					val plantPos = pos.add(rand.nextInt(-6, 6), 0, rand.nextInt(-6, 6)).takeUnless(world::isAir)?.offsetUntil(UP, 1..6, world::isAir)
					
					if (plantPos != null && Facing4.all { world.isAir(plantPos.offset(it)) }) {
						ChorusPlant.generate(world, rand, plantPos)
						break
					}
				}
			}
		}
		
		private fun checkAir(world: SegmentedWorld, pos: BlockPos, facing: Direction): Boolean {
			return (1..12).all { off -> pos.offset(facing, off).let { !world.isInside(it) || world.isAir(it) } }
		}
		
		private fun checkBlocks(world: SegmentedWorld, pos: BlockPos, facing: Direction): Boolean {
			return (13..19).any { off -> pos.offset(facing, off).let { world.isInside(it) && !world.isAir(it) } }
		}
		
		private fun checkAround(world: SegmentedWorld, rand: Random, pos: BlockPos): Boolean {
			for (attempt in 1..150) {
				val testPos = pos.add(
					rand.nextInt(-10, 10),
					rand.nextInt(-10, 10),
					rand.nextInt(-10, 10)
				)
				
				if (world.isInside(testPos) && !world.isAir(testPos)) {
					return false
				}
			}
			
			return true
		}
		
		private object ChorusPlant {
			fun generate(world: SegmentedWorld, rand: Random, pos: BlockPos) {
				placePlant(world, pos)
				growChorusPlantRecursive(world, rand, pos, pos, maxHorizontalSpan = rand.nextInt(5, 7), maxRecursionLevel = rand.nextInt(3, 5))
			}
			
			private fun growChorusPlantRecursive(world: SegmentedWorld, rand: Random, branchStart: BlockPos, rootStart: BlockPos, maxHorizontalSpan: Int, maxRecursionLevel: Int, recursionLevel: Int = 0) {
				val branchHeight = rand.nextInt(1, 4) + (if (recursionLevel == 0) 1 else 0)
				
				for (y in 0 until branchHeight) {
					val pos = branchStart.up(y + 1)
					
					if (!areAllNeighborsEmpty(world, pos)) {
						return
					}
					
					placePlant(world, pos)
					placePlant(world, pos.down())
				}
				
				var shouldGenerateFlower = true
				
				if (recursionLevel < maxRecursionLevel) {
					val sideLength = rand.nextInt(0, 3) + (if (recursionLevel == 0) 1 else 0)
					
					for (off in 0 until sideLength) {
						val dir = rand.nextItem(Facing4)
						val pos = branchStart.up(branchHeight).offset(dir)
						
						if (abs(pos.x - rootStart.x) < maxHorizontalSpan &&
						    abs(pos.z - rootStart.z) < maxHorizontalSpan &&
						    world.isAir(pos) &&
						    world.isAir(pos.down()) &&
						    areAllNeighborsEmpty(world, pos, dir.opposite)
						) {
							placePlant(world, pos)
							placePlant(world, pos.offset(dir.opposite))
							
							growChorusPlantRecursive(world, rand, pos, rootStart, maxHorizontalSpan, maxRecursionLevel, recursionLevel + 1)
							shouldGenerateFlower = false
						}
					}
				}
				
				if (shouldGenerateFlower) {
					world.setState(branchStart.up(branchHeight), Blocks.CHORUS_FLOWER.with(CHORUS_FLOWER_AGE, 5))
				}
			}
			
			private fun placePlant(world: SegmentedWorld, pos: BlockPos) {
				val sides = Facing6.associateWith {
					world.getBlock(pos.offset(it)).let { block -> block === Blocks.CHORUS_PLANT || block === Blocks.CHORUS_FLOWER || (it === DOWN && block === ModBlocks.HUMUS) }
				}
				
				val state = SixWayBlock.FACING_TO_PROPERTY_MAP.entries.fold(Blocks.CHORUS_PLANT.defaultState) { acc, entry ->
					acc.with(entry.value, sides.getValue(entry.key))
				}
				
				world.setState(pos, state)
			}
			
			private fun areAllNeighborsEmpty(world: SegmentedWorld, pos: BlockPos, excludingSide: Direction? = null): Boolean {
				return Facing4.all { it == excludingSide || world.isAir(pos.offset(it)) }
			}
		}
	}
	
	private object Decorations {
		private val VINE_FACINGS = Facing4 + UP
		
		fun generateVineGroup(world: SegmentedWorld, rand: Random, size: Size) {
			for (attempt in 1..400) {
				val pos = pickRandomPos(rand, size, y = rand.nextInt(12, size.maxY - 8), edge = 32)
				
				if (world.isAir(pos) && Facing4.any { !world.isAir(pos.offset(it)) }) { // require at least one vine attached horizontally
					placeVine(world, pos, rand.nextInt(4, 15))
					
					repeat(rand.nextInt(10, 700)) {
						val dist = 2 + (it / 35)
						
						val testPos = pos.add(
							rand.nextInt(-dist, dist),
							rand.nextInt(-dist / 2, dist / 2),
							rand.nextInt(-dist, dist)
						)
						
						if (world.isInside(testPos) && world.isAir(testPos) && VINE_FACINGS.any { facing -> isValidVineBlock(world.getBlock(testPos.offset(facing))) }) {
							placeVine(world, testPos, rand.nextInt(3, 13))
						}
					}
					
					break
				}
			}
		}
		
		private fun placeVine(world: SegmentedWorld, pos: BlockPos, length: Int) {
			val topState = placeIndividualVine(world, pos).with(VINE_UP, false)
			
			if (!Facing4.any { topState[VineBlock.FACING_TO_PROPERTY_MAP.getValue(it)] }) {
				return
			}
			
			for (y in 1 until length) {
				val testPos = pos.down(y)
				
				if (world.isAir(testPos)) {
					placeIndividualVine(world, testPos, topState)
				}
				else {
					break
				}
			}
		}
		
		private fun placeIndividualVine(world: SegmentedWorld, pos: BlockPos, baseState: BlockState = ModBlocks.DRY_VINES.defaultState): BlockState {
			val state = VINE_FACINGS.fold(baseState) { acc, facing ->
				if (isValidVineBlock(world.getBlock(pos.offset(facing))))
					acc.with(VineBlock.FACING_TO_PROPERTY_MAP.getValue(facing), true)
				else
					acc
			}
			
			world.setState(pos, state)
			return state
		}
		
		private fun isValidVineBlock(block: Block): Boolean {
			return block === Blocks.END_STONE || block === ModBlocks.ENDERSOL || block === ModBlocks.HUMUS || block is BlockWhitebarkLeaves
		}
	}
}
