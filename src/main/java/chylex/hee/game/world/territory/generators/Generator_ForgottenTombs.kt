package chylex.hee.game.world.territory.generators
import chylex.hee.game.world.feature.basic.NoiseGenerator
import chylex.hee.game.world.feature.basic.PortalGenerator
import chylex.hee.game.world.feature.basic.ores.OreGenerator
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueAdjacent
import chylex.hee.game.world.feature.basic.ores.impl.withAdjacentAirCheck
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.territory.ITerritoryGenerator
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.game.world.util.PosXZ
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Facing6
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.allInCenteredBoxMutable
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.nextBiasedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.square
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.math.BlockPos
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow
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
		
		// TODO tomb dungeon
		
		return TerritoryGenerationInfo(spawnPoint)
	}
	
	private object EntranceCave{
		private const val RADIUS_XZ = 65
		private const val RADIUS_Y = 35
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
				
				val centerPos = PosXZ(ellipsoidCenter.add(chunkX * 20, 0, chunkZ * 20))
				
				for(attempt in 1..10){
					if (Stalactite.generate(world, rand, size, centerPos.add(rand.nextInt(-9, 9), rand.nextInt(-9, 9)))){
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
					oresPerCluster = { rand -> 3 + (5 * rand.nextBiasedFloat(4F)).floorToInt() },
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
				clustersPerChunk = { 3 }
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
}
