package chylex.hee.game.world.territory.generators
import chylex.hee.HEE
import chylex.hee.game.world.feature.basic.NoiseGenerator
import chylex.hee.game.world.feature.basic.PortalGenerator
import chylex.hee.game.world.feature.basic.ores.OreGenerator
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueAdjacent
import chylex.hee.game.world.feature.basic.ores.impl.withAdjacentAirCheck
import chylex.hee.game.world.feature.tombdungeon.TombDungeonBuilder
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonStart
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.territory.ITerritoryGenerator
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.math.RandomInt.Companion.Biased
import chylex.hee.system.util.math.RandomInt.Companion.Constant
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.square
import chylex.hee.system.util.withFacing
import chylex.hee.system.util.xz
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Generator_ForgottenTombs : ITerritoryGenerator{
	override val segmentSize = Size(32, 16, 32)
	
	override val defaultBlock: Block
		get() = Blocks.END_STONE
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo{
		val rand = world.rand
		val size = world.worldSize
		
		val spawnPoint = EntranceCave.generate(world, rand, size)
		PortalGenerator.VoidPortalReturnActive.place(world, spawnPoint)
		
		val entrancePoint = TombDungeonEntrance.generate(world, rand, spawnPoint)
		TombDungeon.generate(world, rand, entrancePoint)
		
		return TerritoryGenerationInfo(spawnPoint)
	}
	
	private object EntranceCave{
		const val RADIUS_XZ = 65
		const val RADIUS_Y = 35
		
		private const val EXTRA_ELEVATION = 2
		
		fun generate(world: SegmentedWorld, rand: Random, size: Size): BlockPos{
			val ellipsoidBottom = size.maxY + EXTRA_ELEVATION - (2 * RADIUS_Y)
			val ellipsoidCenter = Pos(size.centerX, ellipsoidBottom + RADIUS_Y, size.centerZ + (size.z / 6))
			
			Cutout.generate(world, rand, ellipsoidCenter)
			EndPowderOre.generate(world, size, ellipsoidCenter)
			
			for(chunkX in -3..3) for(chunkZ in -3..3){
				if ((chunkX == 0 && chunkZ == 0) || (square(chunkX) + square(chunkZ) > 10)){
					continue
				}
				
				val centerXZ = ellipsoidCenter.add(chunkX * 20, 0, chunkZ * 20).xz
				
				for(attempt in 1..10){
					if (Stalactite.generate(world, rand, size, centerXZ.add(rand.nextInt(-9, 9), rand.nextInt(-9, 9)))){
						break
					}
				}
			}
			
			return ellipsoidCenter.add(0, -RADIUS_Y, 0)
		}
		
		private object Cutout{
			private const val SKYVIEW_RADIUS = 11
			
			fun generate(world: SegmentedWorld, rand: Random, center: BlockPos){
				generateEllipsoid(world, rand, center)
				generateRoughness(world, rand, center)
				generateSkyview(world, rand, Pos(center.x, world.worldSize.maxY, center.z))
			}
			
			private fun generateEllipsoid(world: SegmentedWorld, rand: Random, center: BlockPos){
				val radXZ = RADIUS_XZ.toFloat()
				val radY = RADIUS_Y.toFloat()
				
				val noiseXZ = NoiseGenerator.PerlinNormalized(rand, scale = 24.0, octaves = 2)
				val noiseY = NoiseGenerator.PerlinNormalized(rand, scale = 48.0, octaves = 2)
				
				for((x, y, z) in BlockPos.ORIGIN.allInCenteredBoxMutable(RADIUS_XZ, RADIUS_Y, RADIUS_XZ)){
					val normalizedY = remapRange(y.toFloat(), -radY..radY, 0F..1F)
					
					val powerY = remapRange(sqrt(normalizedY), 0F..1F, (4.8F)..(1.6F))
					val powerXZ = powerY * 0.8F
					
					val corner = 1 + ((abs(x) + abs(z)) / (3F * radXZ)).pow(2F)
					
					val edge = 1F - noiseXZ.getValue(x, z){
						multiply(0.25 * (0.8 + (0.2 * corner * noiseY.getRawValue(y, x + z))))
						
						if (normalizedY < 0.1F){
							multiply(0.2 + (0.8 * (normalizedY / 0.1)))
						}
					}
					
					if ((abs(x) / radXZ).pow(powerXZ) + (abs(y) / radY).pow(powerY) + (abs(z) / radXZ).pow(powerXZ) <= edge){
						world.setAir(center.add(x, y, z))
					}
				}
			}
			
			private fun generateRoughness(world: SegmentedWorld, rand: Random, center: BlockPos){
				for(attempt in 1..25000){
					val pos1 = rand.nextInt(-RADIUS_XZ, RADIUS_XZ)
					val pos2 = rand.nextInt(RADIUS_XZ / 2, RADIUS_XZ) * (if (rand.nextBoolean()) 1 else -1)
					val posY = rand.nextInt(-RADIUS_Y, RADIUS_Y - EXTRA_ELEVATION - 1)
					
					val randomPos = if (rand.nextBoolean())
						center.add(pos1, posY, pos2)
					else
						center.add(pos2, posY, pos1)
					
					if (world.isInside(randomPos) && world.getBlock(randomPos) == Blocks.END_STONE && Facing6.count { world.isAir(randomPos.offset(it)) } >= 3){
						world.setAir(randomPos)
					}
				}
			}
			
			private fun generateSkyview(world: SegmentedWorld, rand: Random, top: BlockPos){
				val topOffset = 1 - (SKYVIEW_RADIUS / 2)
				
				for((x, y, z) in Pos(-SKYVIEW_RADIUS, -SKYVIEW_RADIUS, -SKYVIEW_RADIUS).allInBoxMutable(Pos(SKYVIEW_RADIUS, 0, SKYVIEW_RADIUS))){
					if (square(x) + square(y * 2) + square(z) <= square(SKYVIEW_RADIUS - rand.nextFloat(0F, 0.1F))){
						val finalPos = top.add(x, topOffset - y, z)
						
						if (world.isInside(finalPos)){
							world.setAir(finalPos)
						}
					}
				}
			}
		}
		
		private object Stalactite{
			fun generate(world: SegmentedWorld, rand: Random, size: Size, pos: PosXZ): Boolean{
				var top = pos.withY(size.maxY).offsetUntil(DOWN, 0..(RADIUS_Y / 2), world::isAir) ?: return false
				
				for(x in -3..3) for(z in -3..3){
					val max = top.add(x, 0, z).offsetUntil(UP, 0..3){ !world.isAir(it) } ?: return false
					
					if (max.y > top.y){
						top = Pos(top.x, max.y, top.z)
					}
				}
				
				val radius = rand.nextInt(1, 2)
				val height = rand.nextInt(15, 45)
				val spikiness = rand.nextInt(0, 5) + (height / 5)
				
				for(x in -2..2) for(z in -2..2){
					if (square(x) + square(z) <= square(radius) + rand.nextInt(0, 1)){
						val h = height - (spikiness * (abs(x) + abs(z))) - rand.nextInt(spikiness)
						
						for(y in 1..h){
							world.setBlock(top.add(x, -y, z), Blocks.END_STONE)
						}
					}
				}
				
				return true
			}
		}
		
		private object EndPowderOre{
			private const val CHUNK_SIZE = 33
			private const val CENTER_OFFSET_XZ = CHUNK_SIZE * 2
			
			private val PASS = OreGenerator(
				OreTechniqueAdjacent(
					oresPerCluster = Biased(3, 7, biasSoftener = 4F),
					allowDiagonals = true
				).withAdjacentAirCheck(
					checkDistance = 1,
					chanceIfNoAir = 0.01F
				),
				
				BlockReplacer(
					fill = ModBlocks.END_POWDER_ORE,
					replace = Blocks.END_STONE
				),
				
				chunkSize = CHUNK_SIZE,
				attemptsPerChunk = 400,
				clustersPerChunk = Constant(3)
			)
			
			fun generate(world: SegmentedWorld, size: Size, center: BlockPos){
				val bounds = BoundingBox(
					Pos(center.x - CENTER_OFFSET_XZ, center.y - RADIUS_Y - 1, center.z - CENTER_OFFSET_XZ),
					Pos(center.x + CENTER_OFFSET_XZ - 1, size.maxY - 4, center.z + CENTER_OFFSET_XZ - 1)
				)
				
				PASS.generate(world, bounds)
			}
		}
	}
	
	private object TombDungeonEntrance{
		private const val CUT_WIDTH_FULL = 8
		private const val CUT_WIDTH_DOOR = 4
		private const val CUT_HEIGHT = 5
		
		private val FULL_X_OFFSETS = -CUT_WIDTH_FULL..CUT_WIDTH_FULL
		private val DOOR_X_OFFSETS = -CUT_WIDTH_DOOR..CUT_WIDTH_DOOR
		private val DOOR_Y_OFFSETS = 0 until CUT_HEIGHT
		
		fun generate(world: SegmentedWorld, rand: Random, portal: BlockPos): BlockPos{
			val minOffset = EntranceCave.RADIUS_XZ * 2 / 5
			val maxOffset = EntranceCave.RADIUS_XZ
			
			val center = portal.up()
			
			val firstCut = center.offsetUntil(NORTH, minOffset..maxOffset){
				FULL_X_OFFSETS.any { offX -> !world.isAir(it.east(offX)) }
			} ?: center.north(minOffset)
			
			val entranceCut = center.offsetUntil(NORTH, (firstCut.z - center.z)..maxOffset){
				DOOR_X_OFFSETS.all { offX -> DOOR_Y_OFFSETS.all { offY -> !world.isAir(it.east(offX).up(offY)) } }
			} ?: center.north(maxOffset)
			
			val cutDistance = abs(entranceCut.z - firstCut.z)
			
			generateTerraforming(world, firstCut, entranceCut, cutDistance)
			generateEntrance(world, rand, entranceCut, cutDistance)
			
			return entranceCut
		}
		
		private fun generateTerraforming(world: SegmentedWorld, firstCut: BlockPos, entranceCut: BlockPos, cutDistance: Int){
			world.placeCube(firstCut.add(-CUT_WIDTH_DOOR, 0, 0), entranceCut.add(CUT_WIDTH_DOOR, CUT_HEIGHT, 1), Air)
			
			for(mp in intArrayOf(-1, 1)){
				world.placeCube(firstCut.add(mp * (CUT_WIDTH_DOOR + 1), 0, 0), entranceCut.add(mp * (CUT_WIDTH_DOOR + 1), CUT_HEIGHT, 2), Air)
				world.placeCube(firstCut.add(mp * (CUT_WIDTH_DOOR + 2), 0, 0), entranceCut.add(mp * (CUT_WIDTH_DOOR + 2), CUT_HEIGHT, 3), Air)
				
				val end1 = entranceCut.add(mp * (CUT_WIDTH_DOOR + 3), 0, 4).offsetUntil(SOUTH, 0..(cutDistance - 3), world::isAir) ?: continue
				world.setAir(end1.north(1))
				world.setAir(end1.north(2))
				
				for(y in 1 until CUT_HEIGHT){
					val endAbove = entranceCut.add(mp * (CUT_WIDTH_DOOR + 3), y, 4).offsetUntil(SOUTH, 0..(cutDistance - 3), world::isAir) ?: break
					world.setAir(endAbove.north())
				}
				
				val end2 = entranceCut.add(mp * (CUT_WIDTH_DOOR + 4), 0, 5).offsetUntil(SOUTH, 0..(cutDistance - 4), world::isAir) ?: continue
				world.setAir(end2.north())
			}
		}
		
		private fun generateEntrance(world: SegmentedWorld, rand: Random, doorCenter: BlockPos, cutDistance: Int){
			world.placeCube(doorCenter.add(-2, -1, 0), doorCenter.add(-2, 2, 0), TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL)
			world.placeCube(doorCenter.add(2, -1, 0), doorCenter.add(2, 2, 0), TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL)
			world.placeCube(doorCenter.add(-2, 3, 0), doorCenter.add(2, 3, 0), TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL)
			world.placeCube(doorCenter.add(-1, 0, 0), doorCenter.add(1, 2, 0), Air)
			
			for(pos in doorCenter.add(-CUT_WIDTH_DOOR - 3, -1, 0).allInBoxMutable(doorCenter.add(CUT_WIDTH_DOOR + 3, -1, cutDistance / 2))){
				if (world.isAir(pos.up()) && !world.isAir(pos)){
					world.setBlock(pos, ModBlocks.DUSTY_STONE)
				}
			}
			
			val floorEdge = doorCenter.add(0, -1, 1 + (cutDistance / 2))
			
			val cosOffset1 = rand.nextFloat(0.0, PI)
			val cosOffset2 = cosOffset1 + (PI / 2) + rand.nextFloat(PI / 4, PI / 3)
			
			for(offsetX in -CUT_WIDTH_DOOR..CUT_WIDTH_DOOR){
				val height = (abs(cos((offsetX / 2.0) + cosOffset1)) + abs(cos((offsetX / 1.2) + cosOffset2))).roundToInt()
				
				if (height > 0){
					val firstPos = floorEdge.east(offsetX)
					world.placeCube(firstPos, firstPos.south(height - 1), Single(ModBlocks.DUSTY_STONE))
				}
			}
			
			repeat(6){
				for(attempt in 1..25){
					val offsetX = rand.nextInt(3, CUT_WIDTH_FULL) * (if (rand.nextBoolean()) 1 else -1)
					var randomPos = doorCenter.add(offsetX, 0, rand.nextInt(1, cutDistance - 3))
					
					if (!world.isAir(randomPos)){
						if (rand.nextBoolean()){
							continue
						}
						else{
							randomPos = randomPos.up()
						}
					}
					
					if (world.isAir(randomPos) && Facing4.count { !world.isAir(randomPos.offset(it)) } < 2){
						world.placeBlock(randomPos, TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
						
						if (rand.nextInt(3) != 0){
							world.setState(randomPos.up(), when(rand.nextInt(8)){
								0 -> ModBlocks.DUSTY_STONE.defaultState
								in 1..3 -> ModBlocks.DUSTY_STONE_BRICK_SLAB.defaultState
								in 4..6 -> ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(rand.nextItem(Facing4))
								else -> TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING.pick(rand)
							})
						}
						
						break
					}
				}
			}
		}
	}
	
	private object TombDungeon{
		fun generate(world: SegmentedWorld, rand: Random, entrance: BlockPos){
			for(attempt in 1..1000){
				val build = TombDungeonBuilder.build(rand)
				
				if (build != null){
					build.generate(OffsetStructureWorld(world, entrance.up().subtract(TombDungeonBuilder.ENTRANCE_POS).subtract(TombDungeonStart.size.centerPos)))
					return
				}
			}
			
			HEE.log.error("[Generator_ForgottenTombs] failed all attempts at generating Tomb Dungeon")
		}
	}
}
