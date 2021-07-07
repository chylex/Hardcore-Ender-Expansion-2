package chylex.hee.game.territory.generator

import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.particle.spawner.properties.IShape.Line
import chylex.hee.game.territory.TerritoryType.FORGOTTEN_TOMBS
import chylex.hee.game.territory.system.ITerritoryGenerator
import chylex.hee.game.territory.system.TerritoryGenerationInfo
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.blob.BlobGenerator
import chylex.hee.game.world.generation.blob.BlobPattern
import chylex.hee.game.world.generation.blob.BlobSmoothing
import chylex.hee.game.world.generation.blob.layouts.BlobLayoutAttaching
import chylex.hee.game.world.generation.blob.layouts.BlobLayoutAttaching.AttachingStrategy.FIRST_BLOB
import chylex.hee.game.world.generation.blob.layouts.BlobLayoutSingle
import chylex.hee.game.world.generation.cave.CaveGenerator
import chylex.hee.game.world.generation.cave.impl.CaveCarverSphere
import chylex.hee.game.world.generation.cave.impl.CavePatherRotatingBase
import chylex.hee.game.world.generation.cave.impl.CaveRadiusSine
import chylex.hee.game.world.generation.feature.basic.PortalGenerator
import chylex.hee.game.world.generation.noise.NoiseGenerator
import chylex.hee.game.world.generation.noise.NoiseValue
import chylex.hee.game.world.generation.ore.OreGenerator
import chylex.hee.game.world.generation.ore.impl.OreTechniqueAdjacent
import chylex.hee.game.world.generation.ore.impl.withAdjacentAirCheck
import chylex.hee.game.world.generation.structure.world.SegmentedWorld
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.generation.trigger.TileEntityStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.allInCenteredBoxMutable
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.offsetUntil
import chylex.hee.game.world.util.offsetUntilExcept
import chylex.hee.game.world.util.offsetWhile
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextItemOrNull
import chylex.hee.system.random.nextVector
import chylex.hee.system.random.nextVector2
import chylex.hee.system.random.removeItemOrNull
import chylex.hee.util.collection.weightedListOf
import chylex.hee.util.math.Pos
import chylex.hee.util.math.PosXZ
import chylex.hee.util.math.Size
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.addY
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.remapRange
import chylex.hee.util.math.scale
import chylex.hee.util.math.scaleY
import chylex.hee.util.math.square
import chylex.hee.util.math.withY
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.random.RandomDouble.Companion.Linear
import chylex.hee.util.random.RandomInt.Companion.Biased
import chylex.hee.util.random.RandomInt.Companion.Linear
import net.minecraft.block.Blocks
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import java.util.Random
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

object Generator_TheHub : ITerritoryGenerator {
	override val segmentSize = Size(32)
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo {
		val rand = world.rand
		val size = world.worldSize
		
		MainIsland.generate(world, rand, size)
		
		val spawnIslandCenter = SpawnIsland.findSpawnPos(world, rand, size)
		val voidPortalCenter = VoidPortal.findSpawnPos(world, rand, size)
		val voidPortalFacing = VoidPortal.generatePath(world, voidPortalCenter, spawnIslandCenter)
		
		CaveSystem.generate(world, rand, size)
		SpawnIsland.generate(world, rand, spawnIslandCenter)
		VoidPortal.generatePortal(world, rand, voidPortalCenter, voidPortalFacing)
		EndPowderOre.generate(world, size)
		EndstoneBlobs.generate(world, rand, size)
		
		return TerritoryGenerationInfo(spawnIslandCenter, voidPortalCenter)
	}
	
	private object MainIsland {
		const val RADIUS = 111.5
		const val ELEVATION_TOP = 17.0
		const val ELEVATION_BOTTOM = 13.0
		
		fun generate(world: SegmentedWorld, rand: Random, size: Size) {
			val noiseIslandTop    = NoiseGenerator.OldPerlinNormalized(rand, scale =  48.0, octaves = 2)
			val noiseIslandBottom = NoiseGenerator.OldPerlinNormalized(rand, scale = 104.0, octaves = 1)
			val noiseIslandEdge   = NoiseGenerator.OldPerlinNormalized(rand, scale =  52.0, octaves = 1)
			
			val centerPos = size.centerPos
			
			for (x in -128..128) for (z in -128..128) {
				if (square(x) + square(z) > square(RADIUS)) {
					continue
				}
				
				val distance = determineDistance(x, z) + noiseIslandEdge.getValue(x, z) {
					redistribute(0.5)
					multiply(0.22)
				}
				
				val heightTop = noiseIslandTop.getValue(x, z) {
					distanceReshape(distance)
					redistribute(0.4)
					multiply(ELEVATION_TOP)
				}
				
				val heightBottom = noiseIslandBottom.getValue(x, z) {
					distanceReshape(distance)
					redistribute(0.4)
					
					ifNonZero {
						remap((0.2)..(1.0))
						multiply(ELEVATION_BOTTOM)
					}
				}
				
				for (y in 0 until heightTop.floorToInt()) {
					world.setBlock(centerPos.add(x, y, z), Blocks.END_STONE)
				}
				
				for (y in 0 until heightBottom.ceilToInt()) {
					world.setBlock(centerPos.add(x, -y, z), Blocks.END_STONE)
				}
			}
			
			// POLISH maybe run a second air gen pass on the edges to make them look nicer
		}
		
		private fun determineDistance(x: Int, z: Int): Double {
			val distance1 = sqrt((square(x) + square(z)).toDouble()) / RADIUS
			val distance2 = (square(square(x)) + square(square(z))) / square(square(RADIUS))
			return (distance1 * 0.85) + (distance2 * 0.15)
		}
		
		private fun NoiseValue.distanceReshape(distance: Double) {
			value = when (distance) {
				in (0.00)..(0.85) -> value
				in (0.85)..(1.00) -> value * remapRange(distance, (0.85)..(1.0), (1.0)..(0.0))
				else              -> 0.0
			}
		}
	}
	
	private object CaveSystem {
		private val MAIN_CAVE = CaveGenerator(
			CaveCarverSphere(maxRandomRadiusReduction = 0.15F),
			CaveRadiusSine(2.8, deviation = 0.3, frequency = 0.09, iterations = 3),
			stepSize = 0.5
		)
		
		private val SECONDARY_CAVE = CaveGenerator(
			CaveCarverSphere(maxRandomRadiusReduction = 0.25F),
			CaveRadiusSine(2.6, deviation = 0.2, frequency = 0.06, iterations = 2),
			stepSize = 0.5
		)
		
		fun generate(world: SegmentedWorld, rand: Random, size: Size) {
			val tokenCandidates = mutableListOf<BlockPos>()
			val branchCandidates = mutableListOf<BlockPos>()
			val centerPos = size.centerPos
			
			repeat(5) {
				for (attempt in 1..25) {
					val start = centerPos.center.add(rand.nextVector2(xz = rand.nextFloat(32.0, MainIsland.RADIUS), y = MainIsland.ELEVATION_TOP))
					val length = rand.nextFloat(90.0, 100.0)
					
					val targetX = centerPos.x + rand.nextFloat(-MainIsland.RADIUS, MainIsland.RADIUS) * 0.5
					val targetZ = centerPos.z + rand.nextFloat(-MainIsland.RADIUS, MainIsland.RADIUS) * 0.5
					
					val direction = Vec3.xz(targetX - start.x, targetZ - start.z).normalize().withY(rand.nextFloat(-1.1, -0.6))
					val pather = Pather(world, direction)
					
					if (MAIN_CAVE.generate(world, start, length, pather) > length * 0.3) {
						repeat(3) {
							rand.nextItemOrNull(pather.randomPositions)?.let(branchCandidates::add)
						}
						
						pather.randomPositions.lastOrNull { pos -> findTokenSpawnPos(world, pos) != null }?.let(tokenCandidates::add)
						break
					}
				}
			}
			
			repeat(rand.nextInt(11, 13)) {
				for (attempt in 1..10) {
					val start = rand.nextItem(branchCandidates).center
					val length = rand.nextFloat(10.0, 75.0)
					
					val direction = rand.nextVector(1.0)
					val pather = Pather(world, direction)
					
					if (SECONDARY_CAVE.generate(world, start, length, pather) > length * 0.2) {
						if (length > 50.0) {
							rand.nextItemOrNull(pather.randomPositions)?.let(branchCandidates::add)
						}
						
						if (rand.nextInt(3) == 0) {
							pather.randomPositions.lastOrNull { pos -> findTokenSpawnPos(world, pos) != null }?.let(tokenCandidates::add)
						}
						
						break
					}
				}
			}
			
			if (!placeTokens(tokenCandidates, world)) {
				for (fallbackAttempt in 1..200) {
					val testPos = centerPos.add(
						rand.nextFloat(-MainIsland.RADIUS, MainIsland.RADIUS) * 0.25,
						MainIsland.ELEVATION_TOP,
						rand.nextFloat(-MainIsland.RADIUS, MainIsland.RADIUS) * 0.25
					)
					
					val floorPos = testPos.offsetUntil(DOWN, 0..(MainIsland.ELEVATION_TOP.ceilToInt())) {
						world.getBlock(it) === Blocks.END_STONE
					}
					
					if (floorPos != null && placeTokens(mutableListOf(floorPos), world)) {
						break
					}
				}
			}
		}
		
		private fun placeTokens(candidates: MutableList<BlockPos>, world: SegmentedWorld): Boolean {
			val spawnedTokens = mutableSetOf<BlockPos>()
			
			for (token in 1..4) {
				val caveCenter = world.rand.removeItemOrNull(candidates) ?: break
				val tokenPos = findTokenSpawnPos(world, caveCenter)
				
				if (tokenPos != null && spawnedTokens.all { it.distanceSqTo(tokenPos) > square(32) }) {
					world.addTrigger(tokenPos, EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, NORMAL, FORGOTTEN_TOMBS) }, yOffset = 1.0))
					spawnedTokens.add(tokenPos)
				}
			}
			
			return spawnedTokens.isNotEmpty()
		}
		
		private fun findTokenSpawnPos(world: SegmentedWorld, caveCenter: BlockPos): BlockPos? {
			val floorPos = caveCenter.offsetUntil(DOWN, 0..5) { world.getBlock(it) === Blocks.END_STONE }
			
			if (floorPos == null || Facing4.any { world.getBlock(floorPos.offset(it)) !== Blocks.END_STONE }) {
				return null
			}
			
			val tokenPos = floorPos.up()
			
			if (!world.isAir(tokenPos) ||
			    !world.isAir(tokenPos.up()) ||
			    Facing4.any { !world.isAir(tokenPos.offset(it)) } ||
			    tokenPos.offsetUntil(UP, 2..8) { world.getBlock(it) === Blocks.END_STONE } == null
			) {
				return null
			}
			
			return tokenPos
		}
		
		private class Pather(private val world: SegmentedWorld, initialDirection: Vector3d) : CavePatherRotatingBase(initialDirection) {
			val randomPositions = mutableListOf<BlockPos>()
			
			private var reachedCenter = false
			private val centerY = world.worldSize.centerY
			
			override fun update(rand: Random, point: Vector3d) {
				if (point.y <= centerY) {
					reachedCenter = true
				}
				
				if (rand.nextInt(8) == 0) {
					rotation = rotation.add(rand.nextFloat(-0.75, 0.75), if (reachedCenter) rand.nextFloat(-0.25, 0.25) else 0.0, rand.nextFloat(-0.75, 0.75))
				}
				
				if (reachedCenter) {
					if (abs(direction.y) > 0.4) {
						rotation = rotation.scaleY(0.9)
						setDirectionAndNormalize(direction.scaleY(0.9))
					}
					
					val pos = Pos(point)
					
					if ((direction.y < 0 && rotation.y < 0 && isAirOrOutside(world, pos.down(6))) ||
					    (direction.y > 0 && rotation.y > 0 && isAirOrOutside(world, pos.up(6)))
					) {
						rotation = rotation.scaleY(-0.6)
					}
					
					if (rand.nextInt(5) == 0 && world.isInside(pos)) {
						randomPositions.add(pos)
					}
				}
			}
			
			private fun isAirOrOutside(world: SegmentedWorld, pos: BlockPos): Boolean {
				return !world.isInside(pos) || world.isAir(pos)
			}
		}
	}
	
	private object SpawnIsland {
		const val RADIUS = 5.5
		private val ISLAND_EDGE_RANGE = (MainIsland.RADIUS * 0.75).roundToInt()..(MainIsland.RADIUS * 1.25).roundToInt()
		
		fun findSpawnPos(world: SegmentedWorld, rand: Random, size: Size): BlockPos {
			val centerVec = size.centerPos.center
			val offsetVec = rand.nextVector2(xz = 1.0, y = 0.0)
			
			val islandEdgeDistance = (ISLAND_EDGE_RANGE step 4).first {
				world.isAir(Pos(centerVec.add(offsetVec.scale(it))))
			}
			
			val islandCenterDistance = islandEdgeDistance + rand.nextFloat(5.0, 7.0) + RADIUS
			return Pos(centerVec.add(offsetVec.scale(islandCenterDistance)).addY(17.0))
		}
		
		fun generate(world: SegmentedWorld, rand: Random, pos: BlockPos) {
			val islandOffset = RADIUS.ceilToInt()
			
			for (y in -islandOffset..0) for (x in -islandOffset..islandOffset) for (z in -islandOffset..islandOffset) {
				if (square(x) + square(y * 1.5) + square(z) <= square(RADIUS + (if (y == 0) 0.0 else rand.nextFloat(-0.25, 0.25)))) {
					world.setBlock(pos.add(x, y, z), Blocks.END_STONE)
				}
			}
			
			PortalGenerator.EndPortal.place(world, pos)
		}
	}
	
	private object VoidPortal {
		fun findSpawnPos(world: SegmentedWorld, rand: Random, size: Size): BlockPos {
			var bestPos = BlockPos.ZERO
			var bestHeightDifference = Int.MAX_VALUE
			
			for (attempt in 1..100) {
				var testPos = size.centerPos
					.add(rand.nextInt(-18, 18), 0, rand.nextInt(-18, 18))
					.offsetUntilExcept(UP, 0..24, world::isAir)!!
				
				for (pos in testPos.allInCenteredBoxMutable(3, 0, 3)) {
					val topPos = pos.offsetUntil(DOWN, 0..5) { !world.isAir(it) }
					
					if (topPos != null && topPos.y < testPos.y) {
						testPos = testPos.down(testPos.y - topPos.y)
					}
				}
				
				var highestY = testPos.y
				var lowestY = testPos.y
				
				for (pos in testPos.allInCenteredBoxMutable(6, 0, 6)) {
					if (pos.distanceSqTo(testPos) <= square(2.0)) {
						continue
					}
					
					val topY = if (world.isAir(pos))
						pos.offsetWhile(DOWN, 0..4, world::isAir).y
					else
						pos.offsetWhile(UP, 0..3) { !world.isAir(it) }.y
					
					if (topY > highestY) {
						highestY = topY
					}
					else if (topY < lowestY) {
						lowestY = topY
					}
				}
				
				val maxHeightDifference = highestY - lowestY
				
				if (maxHeightDifference < bestHeightDifference) {
					bestPos = testPos
					bestHeightDifference = maxHeightDifference
					
					if (maxHeightDifference <= 1) {
						break
					}
				}
			}
			
			return bestPos
		}
		
		fun generatePath(world: SegmentedWorld, portalCenter: BlockPos, spawnIslandCenter: BlockPos): Direction {
			return Path.generate(world, spawnIslandCenter, portalCenter)
		}
		
		fun generatePortal(world: SegmentedWorld, rand: Random, portalCenter: BlockPos, pathFacing: Direction) {
			Cutout.generate(world, portalCenter)
			PortalGenerator.VoidPortalHub.place(world, portalCenter, radius = 2, outline = BlockReplacer(fill = Blocks.END_STONE, replace = Blocks.AIR), base = Blocks.END_STONE)
			Pillars.generate(world, rand, portalCenter)
			
			world.addTrigger(portalCenter.offset(pathFacing, 3), TileEntityStructureTrigger(ModBlocks.VOID_PORTAL_STORAGE, TagCompound()))
		}
		
		private object Cutout {
			private const val CUTOUT_RADIUS = 10.5
			private const val CUTOUT_VERTICAL_MP = 0.9
			
			fun generate(world: SegmentedWorld, pos: BlockPos) {
				val rand = world.rand
				
				val cutoutOffset = CUTOUT_RADIUS.ceilToInt()
				val verticalOffset = (CUTOUT_RADIUS * CUTOUT_VERTICAL_MP).floorToInt() - 1
				
				for (y in 0..verticalOffset) for (x in -cutoutOffset..cutoutOffset) for (z in -cutoutOffset..cutoutOffset) {
					if (square(x) + square(y * CUTOUT_VERTICAL_MP) + square(z) <= square(CUTOUT_RADIUS + rand.nextFloat(-0.1, 0.1))) {
						world.setAir(pos.add(x, verticalOffset - y + 1, z))
					}
				}
			}
		}
		
		private object Path {
			private const val PAINT_RADIUS = 2.5
			
			fun generate(world: SegmentedWorld, start: BlockPos, destination: BlockPos): Direction {
				val points = generatePoints(world.rand, start, destination)
				
				for ((p1, p2) in points.zipWithNext()) {
					for (point in Line(p1, p2, 0.66).points) {
						paintPoint(world, point.withY(destination.y.toDouble()))
					}
				}
				
				return Facing4.fromDirection(destination.center, points[1])
			}
			
			private fun generatePoints(rand: Random, start: BlockPos, destination: BlockPos): List<Vector3d> {
				val destinationVec = destination.center
				val startVec = start.center
				
				val diffVec = startVec.subtract(destinationVec).withY(0.0)
				val diffLength = diffVec.length() - SpawnIsland.RADIUS - 20
				
				val offsetVec = diffVec.normalize()
				val perpendicularVec = Vec3.xz(-offsetVec.z, offsetVec.x)
				var distance = 8.0
				
				val points = mutableListOf(destinationVec)
				
				do {
					val step = offsetVec.scale(distance)
					val deviation = perpendicularVec.scale(13 * (1 - (distance / diffLength)).pow(0.5) * sin(distance * 0.085))
					
					points.add(destinationVec.add(step).add(deviation))
					distance += rand.nextFloat(7.0, 10.0)
				} while (distance < diffLength)
				
				points.add(destinationVec.add(offsetVec.scale(diffLength)))
				return points
			}
			
			private fun paintPoint(world: SegmentedWorld, point: Vector3d) {
				val top = Pos(point).offsetUntilExcept(UP, -8..8, world::isAir) ?: return
				
				val paintOffset = PAINT_RADIUS.ceilToInt()
				val paintedPositions = mutableListOf<BlockPos>()
				
				for (x in -paintOffset..paintOffset) for (z in -paintOffset..paintOffset) {
					if (square(x) + square(z) <= square(PAINT_RADIUS)) {
						val pos = Pos(point.x + x, top.y.toDouble(), point.z + z).offsetUntilExcept(UP, -3..3, world::isAir) ?: continue
						
						if (world.getBlock(pos) === Blocks.END_STONE) {
							world.setBlock(pos, ModBlocks.DARK_LOAM_SLAB)
							paintedPositions.add(pos)
							
							for (y in 1..2) {
								world.setBlock(pos.down(y), ModBlocks.DARK_LOAM)
							}
						}
					}
				}
				
				paintedPositions.sortBy { it.y }
				
				for ((pos, height) in paintedPositions.associateWith { pos -> Facing4.sumBy {
					val offset = pos.offset(it)
					
					when {
						world.getBlock(offset) === ModBlocks.DARK_LOAM -> 1
						world.isAir(offset) -> -1
						else -> 0
					}
				}}) {
					if (height < 0) {
						world.setAir(pos)
						paintedPositions.remove(pos)
						paintedPositions.add(pos.down())
					}
				}
				
				for (cleanup in 1..8) {
					for (pos in paintedPositions) {
						if (Facing4.count { world.isAir(pos.offset(it)) } >= 3) {
							world.setAir(pos)
						}
						else if (Facing4.count { world.getBlock(pos.offset(it)) === ModBlocks.DARK_LOAM } >= 3) {
							world.setBlock(pos, ModBlocks.DARK_LOAM)
						}
						else if (Facing4.count { world.getBlock(pos.up().offset(it)) === ModBlocks.DARK_LOAM_SLAB } >= 3) {
							world.setBlock(pos.up(), ModBlocks.DARK_LOAM_SLAB)
						}
					}
				}
			}
		}
		
		private object Pillars {
			private const val PILLAR_MIN_DISTANCE = 8.5
			private const val PILLAR_MAX_DISTANCE = 17.5
			
			fun generate(world: SegmentedWorld, rand: Random, pos: BlockPos) {
				val usedPositions = mutableSetOf<PosXZ>()
				
				for (attempt in 1..200) {
					val pillarOffset = rand.nextVector2(xz = rand.nextFloat(PILLAR_MIN_DISTANCE, PILLAR_MAX_DISTANCE), y = 0.0)
					val pillarPos = Pos(pos.center.add(pillarOffset)).offsetUntilExcept(DOWN, -6..6) { world.getBlock(it) === Blocks.END_STONE }
					
					if (pillarPos != null && world.isAir(pillarPos) && usedPositions.add(PosXZ(pillarPos))) {
						val height = min(2, rand.nextInt(1, 3) - rand.nextInt(0, 1))
						world.placeCube(pillarPos, pillarPos.up(height), Single(Blocks.END_STONE))
						
						usedPositions.addAll(Facing4.map(pillarPos::offset).map(::PosXZ))
						
						if (usedPositions.size >= 5 * rand.nextInt(21, 25)) {
							break
						}
					}
				}
			}
		}
	}
	
	private object EndstoneBlobs {
		private val PATTERN = BlobPattern(
			weightedListOf(
				75 to BlobLayoutAttaching(
					amount = { rand -> rand.nextInt(2, rand.nextInt(3, 4)) },
					radiusFirst = Linear(2.6, 4.4),
					radiusOther = Linear(2.2, 3.4),
					distance = Linear(0.4, 0.7),
					strategy = FIRST_BLOB,
					maxSize = 16
				),
				25 to BlobLayoutSingle(
					radius = Linear(2.3, 4.6)
				)
			)
		)
		
		fun generate(world: SegmentedWorld, rand: Random, size: Size) {
			val center = size.centerPos
			
			repeat(rand.nextInt(100, 120)) {
				for (attempt in 1..10) {
					val blobPos = Pos(
						rand.nextInt(5, size.maxX - 5),
						rand.nextInt(5, size.maxY - 5),
						rand.nextInt(5, size.maxZ - 5)
					)
					
					if (blobPos.distanceSqTo(center) > square(MainIsland.RADIUS + 28.0) && BlobGenerator.END_STONE.generate(world, rand, blobPos, BlobSmoothing.FULL, PATTERN)) {
						break
					}
				}
			}
		}
	}
	
	private object EndPowderOre {
		private val FIRST_PASS = OreGenerator(
			OreTechniqueAdjacent(
				oresPerCluster = Biased(2, 4, biasSoftener = 10F),
				allowDiagonals = true
			).withAdjacentAirCheck(
				checkDistance = 2,
				chanceIfNoAir = 0.12F
			),
			
			BlockReplacer(
				fill = ModBlocks.END_POWDER_ORE,
				replace = Blocks.END_STONE
			),
			
			chunkSize = 32,
			attemptsPerChunk = 128,
			clustersPerChunk = Linear(1, 3)
		)
		
		private val SECOND_PASS = OreGenerator(
			OreTechniqueAdjacent(
				oresPerCluster = Biased(3, 7, biasSoftener = 10F),
				allowDiagonals = true
			).withAdjacentAirCheck(
				checkDistance = 1,
				chanceIfNoAir = 0.02F
			),
			
			BlockReplacer(
				fill = ModBlocks.END_POWDER_ORE,
				replace = Blocks.END_STONE
			),
			
			chunkSize = 32,
			attemptsPerChunk = 128,
			clustersPerChunk = { rand -> if (rand.nextInt(3) == 0) 3 else 2 }
		)
		
		fun generate(world: SegmentedWorld, size: Size) {
			val secondPassMinY = size.centerY - (MainIsland.ELEVATION_BOTTOM * 0.7).floorToInt()
			val secondPassMaxY = size.centerY + (MainIsland.ELEVATION_TOP * 0.2).ceilToInt()
			
			FIRST_PASS.generate(world)
			SECOND_PASS.generate(world, secondPassMinY..secondPassMaxY)
		}
	}
}
