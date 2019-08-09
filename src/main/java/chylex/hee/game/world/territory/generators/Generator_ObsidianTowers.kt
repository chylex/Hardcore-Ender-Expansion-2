package chylex.hee.game.world.territory.generators
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.world.feature.basic.NoiseGenerator
import chylex.hee.game.world.feature.basic.PortalGenerator
import chylex.hee.game.world.feature.basic.ores.IOreTechnique
import chylex.hee.game.world.feature.basic.ores.OreGenerator
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueSingle
import chylex.hee.game.world.feature.basic.ores.impl.withAdjacentAirCheck
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerBuilder
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.structure.world.OffsetStructureWorld
import chylex.hee.game.world.territory.ITerritoryGenerator
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrap.LARGE
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrap.MEDIUM
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrap.SMALL
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.ENDIUM_BLOCK
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.ENDIUM_ORES
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.GOO_POOL
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.OBSIDIAN_PILES
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.TOKEN_ARCANE_CONJUNCTIONS
import chylex.hee.game.world.territory.generators.Generator_ObsidianTowers.FloatyCrapContents.TOKEN_LOST_GARDEN
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.game.world.util.Size
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MIN
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.center
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.distanceSqTo
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.math.RandomInt.Companion.Constant
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.square
import chylex.hee.system.util.toRadians
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.Random
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object Generator_ObsidianTowers : ITerritoryGenerator{
	override val segmentSize = Size(16, 40, 16)
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo{
		val rand = world.rand
		val size = world.worldSize
		
		val remainingFloatyCrapTokens = mutableListOf( // TODO
			TerritoryType.OBSIDIAN_TOWERS to TOKEN_ARCANE_CONJUNCTIONS,
			TerritoryType.OBSIDIAN_TOWERS to TOKEN_LOST_GARDEN
		)
		
		val remainingTowerTokens = mutableListOf(
			TerritoryType.OBSIDIAN_TOWERS to (if (rand.nextInt(100) < 45) TokenType.RARE else TokenType.NORMAL),
			rand.removeItem(remainingFloatyCrapTokens).first to TokenType.NORMAL
		)
		
		val remainingFloatyCrapContents = mutableWeightedListOf(
			10 to (OBSIDIAN_PILES to SMALL),
			20 to (OBSIDIAN_PILES to SMALL),
			45 to (OBSIDIAN_PILES to MEDIUM),
			10 to (GOO_POOL to MEDIUM),
			10 to (GOO_POOL to MEDIUM),
			75 to (GOO_POOL to LARGE),
			15 to (ENDIUM_BLOCK to (if (rand.nextBoolean()) MEDIUM else LARGE)),
			40 to (ENDIUM_ORES to SMALL),
			25 to (ENDIUM_ORES to LARGE),
			45 to (remainingFloatyCrapTokens[0].second to SMALL)
		)
		
		val islands = mutableListOf<Island>()
		val towers = mutableListOf<Island>()
		
		val spawnIsland = SpawnPoint.generate(world, rand, size)
		islands.add(spawnIsland)
		
		val angleOffset = rand.nextFloat(0F, 360F)
		
		for(mp in 0..2){
			val island = spawnIsland.getRelativeIsland(
				angle    = (angleOffset + (120F * mp) + rand.nextFloat(-15F, 15F)).toRadians().toFloat(),
				distance = if (mp == 2) rand.nextFloat(86.0, 94.0) else rand.nextFloat(42.0, 56.0),
				radius   = if (mp == 2) 14.5 else rand.nextFloat(11.0, 12.0)
			)
			
			if (mp == 2){
				Tower.generateBoss(world, rand, island)
				Path.generate(world, rand, island, spawnIsland, cutBase = 0.99F, cutMultiplier = 0.94F)
			}
			else{
				Tower.generateRegular(world, rand, island, rand.removeItem(remainingTowerTokens))
				Path.generate(world, rand, island, spawnIsland, cutBase = 0.8F, cutMultiplier = 0.7F)
			}
			
			islands.add(island)
			towers.add(island)
		}
		
		repeat(rand.nextInt(5, 9)){
			val (contents, type) = remainingFloatyCrapContents.removeItem(rand)!!
			
			val radius = rand.nextFloat(type.radius)
			val radiusInteger = radius.ceilToInt()
			
			for(attempt in 1..50){
				val source = rand.nextItem(islands)
				
				val island = source.getRelativeIsland(
					angle    = rand.nextFloat(0F, 360F),
					distance = radius + source.radius + rand.nextFloat(rand.nextFloat(2F, 14F), rand.nextFloat(18F, 28F)),
					radius   = radius
				)
				
				val center = island.center
				
				if (center.distanceSqTo(spawnIsland.center) < square(30.0) ||
					Facing4.any { !world.isInside(center.offset(it, radiusInteger)) } ||
					islands.any { it !== source && center.distanceSqTo(it.center) < radius + it.radius + 6F } ||
					islands.any { it !== source && it !== island && it.distanceToPath(center, source.center) < it.radius + 3F } ||
					towers.any { island.distanceToPath(spawnIsland.center, it.center) < radius + 12F }
				){
					continue
				}
				
				type.generate(world, rand, island, contents)
				Path.generate(world, rand, island, source, cutBase = 1F, cutMultiplier = rand.nextFloat(0.1F, 0.8F), thicknessMultiplier = rand.nextFloat(0.3F, 0.7F))
				
				islands.add(island)
				break
			}
		}
		
		EndiumOre.generate(world)
		
		return TerritoryGenerationInfo(spawnIsland.center, towers[rand.nextInt(0, 1)].center)
	}
	
	private class Island(val center: BlockPos, val radius: Double){
		fun getRelativePos(angle: Float, distance: Double): BlockPos{
			return Pos(center.center.add(Vec3d(distance, 0.0, 0.0).rotateYaw(angle)))
		}
		
		fun getBoundingBox(yOffsets: IntRange): Pair<Int, BoundingBox>{
			val radius = radius.ceilToInt()
			val size = (radius * 2) + 1
			
			val pos1 = center.add(-size / 2, yOffsets.first, -size / 2)
			val pos2 = pos1.add(size - 1, yOffsets.last, size - 1)
			
			return size to BoundingBox(pos1, pos2)
		}
		
		fun getRelativeIsland(angle: Float, distance: Double, radius: Double): Island{
			return Island(getRelativePos(angle, distance), radius)
		}
		
		fun distanceToPath(start: BlockPos, end: BlockPos): Double{
			val dist = start.distanceTo(end) // unnecessary to recompute is all the time but meh
			val t1 = (end.z - start.z) * center.x
			val t2 = (end.x - start.x) * center.z
			val t3 = (end.x * start.z) - (end.z * start.x)
			return abs(t1 - t2 + t3) / dist
		}
		
		fun generateBase(world: SegmentedWorld, rand: Random){
			val islandOffset = radius.ceilToInt()
			
			for(x in -islandOffset..islandOffset) for(z in -islandOffset..islandOffset){
				val distSq = square(x) + square(z)
				
				if (distSq <= square(radius + rand.nextFloat(0.2, 0.4))){
					val height = (radius + 1.0 - sqrt(distSq.toDouble()) - rand.nextFloat(0.0, 1.5)).ceilToInt().coerceAtLeast(0)
					
					for(y in 0..height){
						world.setBlock(center.add(x, -y, z), Blocks.END_STONE)
					}
				}
			}
		}
		
		fun generatePillars(world: SegmentedWorld, rand: Random, amount: Int, exclusionRadius: Double = 0.0){
			repeat(amount){
				for(attempt in 1..3){
					val pos = getRelativePos(rand.nextFloat(0F, 360F), rand.nextFloat(exclusionRadius, radius - 0.5))
					
					if (rand.nextInt(5) == 0){
						if (!generatePillar(world, pos, 1)){
							continue
						}
					}
					else{
						if (!generatePillar(world, pos, 2)){
							continue
						}
						
						repeat(rand.nextInt(1, 3)){
							val offset = pos.add(rand.nextInt(-1, 1), 0, rand.nextInt(-1, 1))
							
							if (offset.distanceSqTo(center) >= square(exclusionRadius)){
								generatePillar(world, offset, if (rand.nextInt(7) == 0) 2 else 1)
							}
						}
					}
					
					break
				}
			}
		}
		
		private fun generatePillar(world: SegmentedWorld, pos: BlockPos, height: Int): Boolean{
			if (world.getBlock(pos) === Blocks.END_STONE){
				for(y in 1..height){
					world.setBlock(pos.up(y), Blocks.END_STONE)
				}
				
				return true
			}
			
			return false
		}
	}
	
	private object Path{
		fun generate(world: SegmentedWorld, rand: Random, island1: Island, island2: Island, cutBase: Float, cutMultiplier: Float, thicknessMultiplier: Float = 1F){
			val center1 = island1.center.center
			val center2 = island2.center.center
			
			val dir = center1.directionTowards(center2)
			val dist = center1.distanceTo(center2) - island1.radius - island2.radius
			val start = center1.add(dir.scale(island1.radius))
			
			val middle = dist * 0.5
			
			var offset = 0.0
			val tested = mutableSetOf<BlockPos>()
			val noise = NoiseGenerator.PerlinNormalized(rand, scale = 3.0, octaves = 1)
			
			while(offset < dist){
				offset += 0.33
				
				val point = start.add(dir.scale(offset))
				
				for(attempt in 1..4){
					val d1 = rand.nextFloat(-1.8, 1.8) * thicknessMultiplier
					val d2 = rand.nextFloat(-1.2, 1.2) * thicknessMultiplier
					
					val testPos = if (rand.nextBoolean())
						Pos(point.add(d1, 0.0, d2))
					else
						Pos(point.add(d2, 0.0, d1))
					
					if (!tested.add(testPos)){
						continue
					}
					
					val distFactor = (abs(offset - middle) / middle).pow(1.3)
					val cutThreshold = cutBase - (cutMultiplier - (cutMultiplier * distFactor))
					
					if (noise.getRawValue(testPos.x, testPos.z) < cutThreshold){
						world.setBlock(testPos, Blocks.END_STONE)
					}
				}
			}
		}
	}
	
	private object SpawnPoint{
		fun generate(world: SegmentedWorld, rand: Random, size: Size): Island{
			val spawnPoint = Pos(size.centerX, size.maxY / 4, size.centerZ)
			
			return Island(spawnPoint, 7.5).apply {
				generateBase(world, rand)
				generatePillars(world, rand, amount = rand.nextInt(5, 8), exclusionRadius = 3.0)
				PortalGenerator.VoidPortalReturnActive.place(world, center)
			}
		}
	}
	
	private object Tower{
		fun generateBoss(world: SegmentedWorld, rand: Random, island: Island){
			generate(world, rand, island, ObsidianTowerBuilder(5, ObsidianTowerPieces.PIECE_LEVEL_TOP_BOSS))
		}
		
		fun generateRegular(world: SegmentedWorld, rand: Random, island: Island, tokenInfo: Pair<TerritoryType, TokenType>){
			generate(world, rand, island, ObsidianTowerBuilder(3, ObsidianTowerPieces.PIECE_LEVEL_TOP_TOKEN(tokenInfo.second, tokenInfo.first)))
		}
		
		private fun generate(world: SegmentedWorld, rand: Random, island: Island, builder: ObsidianTowerBuilder){
			island.generateBase(world, rand)
			island.generatePillars(world, rand, amount = (1 + island.radius).ceilToInt(), exclusionRadius = 8.0)
			builder.build(rand)?.generate(OffsetStructureWorld(world, island.center.subtract(ObsidianTowerPieces.STRUCTURE_SIZE.getPos(CENTER, MIN, CENTER))))
		}
	}
	
	private enum class FloatyCrapContents{
		OBSIDIAN_PILES{
			override fun afterPillars(world: SegmentedWorld, rand: Random, island: Island){
				val radius = island.radius.ceilToInt()
				
				repeat(rand.nextInt(1, 3) + (radius * 1.5).roundToInt()){
					for(attempt in 1..4){
						val pos = island.center.add(rand.nextInt(-radius, radius), 0, rand.nextInt(-radius, radius))
						
						if (world.getBlock(pos) === Blocks.END_STONE && world.isAir(pos.up())){
							if (rand.nextInt(5) == 0){
								generatePile(world, rand, pos, Blocks.OBSIDIAN, height = rand.nextFloat(2.5, 3.5), radius = rand.nextFloat(1.0, 2.0))
							}
							else{
								repeat(rand.nextInt(1, 3)){
									world.setBlock(pos.up(1 + it), Blocks.OBSIDIAN)
								}
							}
							
							break
						}
					}
				}
			}
		},
		
		GOO_POOL{
			private val checkOffsets = arrayOf(
				BlockPos.ORIGIN.offset(DOWN),
				*BlockPos.ORIGIN.allInCenteredBox(1, 0, 1).toList().toTypedArray()
			)
			
			override fun beforePillars(world: SegmentedWorld, rand: Random, island: Island){
				val radius = (rand.nextFloat(0.25, 0.75) * island.radius).ceilToInt()
				val radiusSq = square(radius + 0.5)
				
				val center = island.getRelativePos(rand.nextFloat(0F, 360F), rand.nextFloat(0.0, rand.nextFloat(0.1, 1.1)) * radius)
				val depth = 1 + (radius / 3)
				
				for(y in 0..depth) for(x in -radius..radius) for(z in -radius..radius){
					if (square(x) + square(y * 2) + square(z) < radiusSq){
						val pos = center.add(x, -y, z)
						
						if (world.getBlock(pos) === Blocks.END_STONE && checkOffsets.none { world.isAir(pos.add(it)) }){
							world.setBlock(pos, ModBlocks.ENDER_GOO)
						}
					}
				}
			}
		},
		
		ENDIUM_BLOCK{
			override fun beforePillars(world: SegmentedWorld, rand: Random, island: Island){
				val center = island.center.add(
					rand.nextInt(-2, 2) * rand.nextInt(0, 1),
					0,
					rand.nextInt(-2, 2) * rand.nextInt(0, 1)
				)
				
				val height = rand.nextFloat(3.0, 4.5)
				val radius = 2.5 + (rand.nextFloat(0.1, 0.3) * island.radius)
				
				generatePile(world, rand, center, Blocks.END_STONE, height, radius)
				
				center.offsetUntil(UP, 1..(1 + height.ceilToInt()), world::isAir)?.let {
					world.setBlock(it.down(), ModBlocks.ENDIUM_BLOCK)
				}
			}
		},
		
		ENDIUM_ORES{
			override fun beforePillars(world: SegmentedWorld, rand: Random, island: Island){
				val piles = if (island.radius >= 5.0)
					rand.nextRounded(remapRange(island.radius, (5.0)..(7.0), (2.0)..(4.0)).toFloat())
				else
					1
				
				repeat(piles){
					val pos = island.getRelativePos(rand.nextFloat(0F, 360F), rand.nextFloat(0.0, island.radius - 1.5))
					generatePile(world, rand, pos, Blocks.END_STONE, height = rand.nextFloat(2.0, 4.5), radius = rand.nextFloat(1.5, 3.0))
				}
				
				val (size, bb) = island.getBoundingBox(1..4)
				EndiumOre.construct(OreTechniqueSingle, size, 250, rand.nextInt(2, 4)).generate(world, bb)
			}
		},
		
		TOKEN_ARCANE_CONJUNCTIONS{
			override fun afterPillars(world: SegmentedWorld, rand: Random, island: Island){
				generateToken(world, rand, island, TerritoryType.OBSIDIAN_TOWERS) // TODO
			}
		},
		
		TOKEN_LOST_GARDEN{
			override fun afterPillars(world: SegmentedWorld, rand: Random, island: Island){
				generateToken(world, rand, island, TerritoryType.OBSIDIAN_TOWERS) // TODO
			}
		};
		
		// TODO additional & modded content?
		
		open fun beforePillars(world: SegmentedWorld, rand: Random, island: Island){}
		open fun afterPillars(world: SegmentedWorld, rand: Random, island: Island){}
		
		protected fun generatePile(world: SegmentedWorld, rand: Random, center: BlockPos, block: Block, height: Double, radius: Double){
			val offset = radius.ceilToInt()
			val radiusSq = square(radius)
			
			for(x in -offset..offset) for(z in -offset..offset){
				val distSq = square(x) + square(z)
				
				if (distSq < radiusSq + rand.nextFloat(-0.5, 0.5)){
					val pos = center.add(x, 0, z)
					
					if (world.getBlock(pos) === Blocks.END_STONE){
						val pillarHeight = if (x == 0 && z == 0)
							height.ceilToInt()
						else
							((1.0 - sqrt(distSq / radiusSq)) * height).ceilToInt() - rand.nextInt(0, 1)
						
						for(y in 1..pillarHeight){
							world.setBlock(pos.up(y), block)
						}
					}
				}
			}
		}
		
		protected fun generateToken(world: SegmentedWorld, rand: Random, island: Island, territoryType: TerritoryType){
			for(attempt in 1..100){
				val pos = island.getRelativePos(rand.nextFloat(0F, 360F), rand.nextFloat(0.0, island.radius * 0.6))
				
				if (!world.isAir(pos) && pos.up().allInCenteredBox(1, 0, 1).all(world::isAir)){
					world.addTrigger(pos.up(), EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, TokenType.NORMAL, territoryType) }, yOffset = 0.4))
					break
				}
			}
		}
	}
	
	private enum class FloatyCrap(val radius: ClosedFloatingPointRange<Double>, private val pillars: IntRange, private val ores: IntRange){
		SMALL (radius = (2.5)..(3.5), pillars = 0..1, ores = 1..2),
		MEDIUM(radius = (3.5)..(5.0), pillars = 1..2, ores = 1..3),
		LARGE (radius = (5.0)..(7.0), pillars = 3..5, ores = 2..3);
		
		fun generate(world: SegmentedWorld, rand: Random, island: Island, contents: FloatyCrapContents){
			island.generateBase(world, rand)
			contents.beforePillars(world, rand, island)
			island.generatePillars(world, rand, rand.nextInt(pillars))
			contents.afterPillars(world, rand, island)
			generateOres(world, rand, island)
		}
		
		private fun generateOres(world: SegmentedWorld, rand: Random, island: Island){
			var remainingOres = rand.nextInt(ores)
			
			if (remainingOres == 0){
				return
			}
			
			val radius = island.radius.ceilToInt()
			val (size, bb) = island.getBoundingBox(-radius..(radius + 5))
			
			remainingOres -= EndiumOre.construct(OreTechniqueSingle.withAdjacentAirCheck(), size, 10, remainingOres).generate(world, bb)
			
			if (remainingOres > 0){
				EndiumOre.construct(OreTechniqueSingle, size, 100, remainingOres).generate(world, bb)
			}
		}
	}
	
	private object EndiumOre{
		private val PLACER = BlockReplacer(
			fill = ModBlocks.ENDIUM_ORE,
			replace = Blocks.END_STONE
		)
		
		private val GENERATOR = construct(
			OreTechniqueSingle.withAdjacentAirCheck(),
			chunkSize = 56,
			attemptsPerChunk = 500,
			clustersPerChunk = 1
		)
		
		fun construct(technique: IOreTechnique, chunkSize: Int, attemptsPerChunk: Int, clustersPerChunk: Int): OreGenerator{
			return OreGenerator(technique, PLACER, chunkSize, attemptsPerChunk, Constant(clustersPerChunk))
		}
		
		fun generate(world: SegmentedWorld){
			GENERATOR.generate(world, heights = 5..25)
		}
	}
}
