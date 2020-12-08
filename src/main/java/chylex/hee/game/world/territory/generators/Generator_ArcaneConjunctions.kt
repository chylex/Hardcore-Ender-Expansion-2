package chylex.hee.game.world.territory.generators
import chylex.hee.game.mechanics.energy.IClusterGenerator
import chylex.hee.game.mechanics.energy.IClusterGenerator.Companion.ARCANE_CONJUNCTIONS
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInCenteredBoxMutable
import chylex.hee.game.world.allInCenteredSphereMutable
import chylex.hee.game.world.center
import chylex.hee.game.world.component1
import chylex.hee.game.world.component2
import chylex.hee.game.world.component3
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.distanceTo
import chylex.hee.game.world.feature.basic.PortalGenerator
import chylex.hee.game.world.feature.basic.blobs.BlobGenerator
import chylex.hee.game.world.feature.basic.blobs.BlobPattern
import chylex.hee.game.world.feature.basic.blobs.BlobSmoothing
import chylex.hee.game.world.feature.basic.blobs.IBlobPopulator
import chylex.hee.game.world.feature.basic.blobs.PopulatorBuilder
import chylex.hee.game.world.feature.basic.blobs.layouts.BlobLayoutSingle
import chylex.hee.game.world.feature.basic.blobs.populators.BlobPopulatorFill
import chylex.hee.game.world.feature.basic.blobs.populators.BlobPopulatorOre
import chylex.hee.game.world.feature.basic.ores.OreGenerator
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueAdjacent
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueDistance
import chylex.hee.game.world.feature.basic.ores.impl.OreTechniqueSingle
import chylex.hee.game.world.feature.basic.ores.impl.withAdjacentAirCheck
import chylex.hee.game.world.floodFill
import chylex.hee.game.world.generation.IBlockPlacer.BlockPlacer
import chylex.hee.game.world.generation.IBlockPlacer.BlockReplacer
import chylex.hee.game.world.generation.ScaffoldedWorld
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.generation.TerritoryGenerationInfo
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.offsetUntil
import chylex.hee.game.world.offsetUntilExcept
import chylex.hee.game.world.structure.trigger.EnergyClusterStructureTrigger
import chylex.hee.game.world.structure.trigger.FluidStructureTrigger
import chylex.hee.game.world.territory.ITerritoryGenerator
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Facing4
import chylex.hee.system.facades.Facing6
import chylex.hee.system.math.Vec3
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.offsetTowards
import chylex.hee.system.math.square
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.random.RandomDouble.Companion.Constant
import chylex.hee.system.random.RandomInt.Companion.Constant
import chylex.hee.system.random.RandomInt.Companion.Linear
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextVector
import chylex.hee.system.random.removeItem
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.util.Random
import kotlin.collections.set
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object Generator_ArcaneConjunctions : ITerritoryGenerator{
	override val segmentSize = Size(24, 24, 24)
	
	override fun provide(world: SegmentedWorld): TerritoryGenerationInfo{
		val rand = world.rand
		val size = world.worldSize
		val centerPos = size.centerPos
		
		lateinit var layout: BlobLayout
		
		for(attempt in 0 until 10){
			layout = BlobLayout.generate(rand, size, 35..40)
			
			if (layout.isGoodEnough(minAmount = 35 - attempt)){
				break
			}
		}
		
		val unassigned = layout.blobs.toMutableList()
		var spawnBlob: Blob? = null
		
		for(blob in unassigned.filter { it.connections > 0 && it.center.distanceSqTo(centerPos) <= square(275) }.shuffled(rand).take(3)){
			unassigned.remove(blob)
			layout.assign(blob, BlobContents.ReturnPortal)
			spawnBlob = blob
		}
		
		for(generator in ARCANE_CONJUNCTIONS(rand, min(unassigned.size - 10, rand.nextInt(17, 20)))){
			layout.assign(rand.removeItem(unassigned), BlobContents.EnergyCluster(generator))
		}
		
		repeat(min(unassigned.size - 3, rand.nextInt(3, 4))){
			layout.assign(rand.removeItem(unassigned), BlobContents.EnderGoo)
		}
		
		repeat(min(unassigned.size - 3, rand.nextInt(4, 9))){
			layout.assign(rand.removeItem(unassigned), BlobContents.SmallBlob)
		}
		
		layout.generate(world, rand)
		
		if (spawnBlob == null){
			spawnBlob = Blob(centerPos, 5.5).apply { generateHollow(world, rand, BlobContents.ReturnPortal) }
		}
		
		Ores.generate(world)
		
		val spawnCenter = spawnBlob.center
		val portalCenter = spawnCenter.offsetUntil(DOWN, 0..(Blob.PRIMARY_RADIUS.endInclusive.ceilToInt())){ world.getBlock(it) === ModBlocks.VOID_PORTAL_INNER } ?: spawnCenter
		
		return TerritoryGenerationInfo(portalCenter)
	}
	
	private class BlobLayout private constructor(val blobs: List<Blob>, val paths: List<Path>){
		private val contents = mutableMapOf<Blob, BlobContents>()
		
		fun isGoodEnough(minAmount: Int): Boolean{
			return blobs.size >= minAmount && blobs.count { it.connections > 0 } >= (blobs.size * 4) / 5
		}
		
		fun assign(blob: Blob, contents: BlobContents){
			this.contents[blob] = contents
		}
		
		fun generate(world: SegmentedWorld, rand: Random){
			generatePrimaryAndSecondaryBlobs(world, rand)
			generateHolesWithFilledBlobs(world, rand)
			generateRandomFilledBlobs(world, rand)
		}
		
		private fun generatePrimaryAndSecondaryBlobs(world: SegmentedWorld, rand: Random){
			for(path in paths){
				path.forEachOuterBlock {
					if (world.isInside(it)){
						world.setBlock(it, Blocks.END_STONE)
					}
				}
			}
			
			for(blob in blobs){
				blob.generateHollow(world, rand, contents.getOrDefault(blob, BlobContents.Nothing))
			}
			
			for(path in paths){
				path.forEachInnerBlock(rand){
					if (world.isInside(it) && world.getBlock(it) === Blocks.END_STONE && it.up().let { above -> !world.isInside(above) || world.isAir(above) || world.getBlock(above) === Blocks.END_STONE }){
						world.setAir(it)
					}
				}
			}
			
			for(blob in blobs){
				contents[blob]?.after(world, rand, blob)
			}
		}
		
		private fun generateHolesWithFilledBlobs(world: SegmentedWorld, rand: Random){
			val holeLocations = mutableListOf<Hole>().apply {
				for(blob in blobs){
					repeat(rand.nextInt(0, 2)){
						add(blob.sampleHole(rand))
					}
				}
				
				for(path in paths){
					repeat(rand.nextInt(0, 3)){
						add(path.sampleHole(rand))
					}
				}
				
				shuffle(rand)
			}
			
			repeat(min(holeLocations.size, rand.nextInt(44, 50))){
				with(holeLocations[it]){
					generate(world)
					
					if (rand.nextInt(3) == 0){
						for(attempt in 1..3){
							val center = origin
								.add(offset.normalize().scale(radius))
								.add(offset.scale(rand.nextFloat(1.4, 3.2)))
								.add(
									rand.nextFloat(-2.5, 2.5),
									rand.nextFloat(-2.5, 2.5),
									rand.nextFloat(-2.5, 2.5)
								)
							
							if (generateFilledBlob(world, rand, Pos(center))){
								break
							}
						}
					}
				}
			}
		}
		
		private fun generateRandomFilledBlobs(world: SegmentedWorld, rand: Random){
			repeat(rand.nextInt(10, 13)){
				for(attempt in 1..5){
					if (generateFilledBlob(world, rand)){
						break
					}
				}
			}
		}
		
		private fun generateFilledBlob(world: SegmentedWorld, rand: Random, center: BlockPos? = null): Boolean{
			val radius = rand.nextFloat(Blob.FILLED_RADIUS)
			val filled = center?.let { Blob(it, radius) } ?: Blob.place(rand, world.worldSize, radius)
			
			if (blobs.any { it.spaceBetween(filled) < 1.5 }){
				return false
			}
			
			for(facing in Facing6) for(offset in 1..4){
				val testPos = filled.center.offset(facing, offset)
				
				if (!world.isInside(testPos) || !world.isAir(testPos)){
					return false
				}
			}
			
			filled.generateFilled(world, rand)
			return true
		}
		
		companion object{
			fun generate(rand: Random, size: Size, amount: IntRange): BlobLayout{
				val target = rand.nextInt(amount)
				
				val blobs = ArrayList<Blob>(target)
				val connections = ArrayList<Connection>(target * 2)
				
				for(attempt in 1..(amount.last * 20)){
					val radius = rand.nextFloat(Blob.PRIMARY_RADIUS)
					val blob = Blob.place(rand, size, radius)
					
					if (blobs.none { it.spaceBetween(blob) < 2.5 }){
						blobs.add(blob)
						
						if (blobs.size >= target){
							break
						}
					}
				}
				
				for(blob in blobs){
					val neighbors = blobs.filter { it !== blob && rand.nextInt(3) >= it.connections && it.center.distanceSqTo(blob.center) < square(150) }.toMutableList()
					
					repeat(min(neighbors.size, rand.nextInt(rand.nextInt(0, 1), 2))){
						val candidate1 = rand.nextItem(neighbors)
						val candidate2 = rand.nextItem(neighbors)
						
						val neighbor = if (candidate1.center.distanceSqTo(blob.center) < candidate2.center.distanceSqTo(blob.center))
							candidate1
						else
							candidate2
						
						if (connections.add(Connection(blob, neighbor))){
							++blob.connections
							++neighbor.connections
							
							neighbors.remove(neighbor)
						}
					}
				}
				
				return BlobLayout(blobs, connections.map { Path(it.blob1, it.blob2, rand) })
			}
			
			private class Connection(val blob1: Blob, val blob2: Blob){
				override fun equals(other: Any?): Boolean{
					return other is Connection && ((blob1 == other.blob1 && blob2 == other.blob2) || (blob1 == other.blob2 && blob2 == other.blob1))
				}
				
				override fun hashCode(): Int{
					return blob1.hashCode() + blob2.hashCode()
				}
			}
		}
	}
	
	private class Blob(val center: BlockPos, val radius: Double){
		companion object{
			val PRIMARY_RADIUS = (5.0)..(9.5)
			val FILLED_RADIUS = (2.25)..(3.75)
			
			private val FILL_POPULATOR = BlobPopulatorFill(Blocks.AIR)
			
			private val FILLED_BLOB_POPULATORS = PopulatorBuilder().apply {
				guarantee(BlobPopulatorOre(
					Ores.STARDUST_ORE_TECHNIQUE.withAdjacentAirCheck(),
					Ores.STARDUST_ORE_PLACER,
					
					attemptsPerBlob = 50,
					clustersPerBlob = Linear(0, 1)
				))
			}
			
			fun place(rand: Random, size: Size, radius: Double): Blob{
				val dist = radius.ceilToInt()
				
				val center = Pos(
					rand.nextInt(dist, size.maxX - dist),
					rand.nextInt(dist, size.maxY - dist),
					rand.nextInt(dist, size.maxZ - dist)
				)
				
				return Blob(center, radius)
			}
		}
		
		var connections = 0
		
		fun spaceBetween(other: Blob): Double{
			return center.distanceTo(other.center) - radius - other.radius
		}
		
		fun generateHollow(world: SegmentedWorld, rand: Random, contents: BlobContents){
			BlobGenerator.END_STONE.generate(world, rand, center, BlobSmoothing.MILD, BlobPattern(
				BlobLayoutSingle(Constant(radius)),
				PopulatorBuilder().apply {
					guarantee(FILL_POPULATOR, contents)
				}
			))
		}
		
		fun generateFilled(world: SegmentedWorld, rand: Random){
			BlobGenerator.END_STONE.generate(world, rand, center, BlobSmoothing.NONE, BlobPattern(
				BlobLayoutSingle(Constant(radius)),
				FILLED_BLOB_POPULATORS
			))
		}
		
		fun sampleHole(rand: Random): Hole{
			val offset = rand.nextVector(radius + rand.nextFloat(-1.5, 0.5))
			val radius = max(1.75, radius * rand.nextFloat(0.35, 0.45))
			
			return Hole(center.center, offset, radius)
		}
		
		override fun equals(other: Any?): Boolean{
			return other is Blob && center == other.center
		}
		
		override fun hashCode(): Int{
			return center.hashCode()
		}
	}
	
	private class Path(private val blob1: Blob, private val blob2: Blob, rand: Random){
		private val center1 = blob1.center.center
		private val center2 = blob2.center.center
		private val length = center1.distanceTo(center2)
		
		private val outerThickness = rand.nextFloat(1.0, 2.3)
		private val outerRadius = rand.nextFloat(2.7, 3.9) + outerThickness
		
		private val baseRadiusXZ: Double
		private val baseRadiusY: Double
		
		private val swirlMpX = rand.nextFloat(1F, 6F) * (if (rand.nextBoolean()) 1F else -1F)
		private val swirlMpY = rand.nextFloat(0F, 3F) * (if (rand.nextBoolean()) 1F else -1F)
		private val swirlMpZ = rand.nextFloat(1F, 6F) * (if (rand.nextBoolean()) 1F else -1F)
		
		private val swirlFreqX = rand.nextInt(1, 3) * PI
		private val swirlFreqY = rand.nextInt(1, 2) * PI
		private val swirlFreqZ = rand.nextInt(1, 3) * PI
		
		init{
			val verticality = abs(center2.subtract(center1).normalize().dotProduct(Vec3.y(1.0)))
			val extra = (outerThickness - 1.0) * 0.5
			
			baseRadiusXZ = max(2.7 + extra, outerRadius * (0.8 + ((1.0 - verticality) * 0.2)))
			baseRadiusY = max(2.4 + extra, outerRadius * (0.7 + (verticality * 0.2)))
		}
		
		private fun getCenterPoint(progress: Double): Vec3d{
			return center1.offsetTowards(center2, progress).add(
				swirlMpX * sin(progress * swirlFreqX),
				swirlMpY * sin(progress * swirlFreqY),
				swirlMpZ * sin(progress * swirlFreqZ)
			)
		}
		
		private inline fun forEachPoint(callback: (Vec3d) -> Unit){
			val startOffset = blob1.radius - (outerRadius * 2.0)
			val endOffset = length - (blob2.radius - (outerRadius * 2.0))
			
			// can sometimes start/end a path a bit too far, but it's kinda cute
			
			for(offset in (10 * startOffset).roundToInt()..(10 * endOffset).roundToInt() step 4){
				callback(getCenterPoint((offset / 10.0) / length))
			}
		}
		
		inline fun forEachOuterBlock(callback: (BlockPos) -> Unit) = forEachPoint {
			val center = Pos(it)
			
			for((x, y, z) in BlockPos.ZERO.allInCenteredBoxMutable(baseRadiusXZ, baseRadiusY, baseRadiusXZ)){
				if (square(abs(x) / baseRadiusXZ) + square(abs(y) / baseRadiusY) + square(abs(z) / baseRadiusXZ) <= 1F){
					callback(center.add(x, y, z))
				}
			}
		}
		
		inline fun forEachInnerBlock(rand: Random, callback: (BlockPos) -> Unit) = forEachPoint {
			val center = Pos(it)
			
			for((x, y, z) in BlockPos.ZERO.allInCenteredBoxMutable(baseRadiusXZ, baseRadiusY, baseRadiusXZ)){
				val subtractRadius = outerThickness + rand.nextFloat(0.0, 0.2)
				val radXZ = baseRadiusXZ - subtractRadius
				val radY = baseRadiusY - subtractRadius
				
				if (square(abs(x) / radXZ) + square(abs(y) / radY) + square(abs(z) / radXZ) <= 1F){
					callback(center.add(x, y, z))
				}
			}
		}
		
		fun sampleHole(rand: Random): Hole{
			val progress = rand.nextDouble()
			val p1 = getCenterPoint(progress)
			val p2 = getCenterPoint(progress + (1.0 / length))
			
			val offset: Vec3d
			val radius: Double
			
			if (rand.nextInt(9) == 0){
				offset = rand.nextVector(rand.nextFloat(0.0, 2.0))
				radius = max(baseRadiusXZ, baseRadiusY) * rand.nextFloat(1.05, 1.85)
			}
			else{
				val perpendicular = p2.subtract(p1).crossProduct(rand.nextVector(1.0)).normalize()
				
				offset = perpendicular.scale(rand.nextFloat(0.8, 1.2) * max(baseRadiusXZ, baseRadiusY))
				radius = (baseRadiusXZ + baseRadiusY) * rand.nextFloat(0.3, 0.5)
			}
			
			return Hole(p1, offset, radius)
		}
	}
	
	private class Hole(val origin: Vec3d, val offset: Vec3d, val radius: Double){
		fun generate(world: SegmentedWorld){
			for(pos in Pos(origin.add(offset)).allInCenteredSphereMutable(radius)){
				if (world.isInside(pos) && world.getBlock(pos) === Blocks.END_STONE){
					world.setAir(pos)
				}
			}
		}
	}
	
	private sealed class BlobContents : IBlobPopulator{
		override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){}
		open fun after(world: SegmentedWorld, rand: Random, blob: Blob){}
		
		object Nothing : BlobContents()
		
		object ReturnPortal : BlobContents(){
			override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){
				val size = world.worldSize
				val center = size.centerPos
				
				val pos = center.offsetUntilExcept(DOWN, 1..((size.y + 1) / 2)){ !world.isAir(it) } ?: center
				PortalGenerator.VoidPortalReturnActive.place(world, pos, outline = BlockPlacer(Blocks.END_STONE))
			}
		}
		
		class EnergyCluster(private val cluster: IClusterGenerator) : BlobContents(){
			override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){
				val size = world.worldSize
				val center = size.centerPos
				
				val pos = if (rand.nextInt(3) != 0)
					center
				else
					Pos(center.center.add(rand.nextVector(rand.nextFloat(0.0, (min(size.x, size.z) - 1) * 0.3))))
				
				world.addTrigger(pos, EnergyClusterStructureTrigger(cluster.generate(rand)))
			}
		}
		
		object EnderGoo : BlobContents(){
			override fun generate(world: ScaffoldedWorld, rand: Random, generator: BlobGenerator){
				val size = world.worldSize
				val center = size.centerPos
				val radius = min(size.x, size.z) / 2
				
				val bottomCenter = center.offsetUntil(DOWN, 1..radius){ world.getBlock(it) === Blocks.END_STONE } ?: return
				
				val topBlocks = center.offsetUntil(UP, 1..radius){
					world.getBlock(it) === Blocks.END_STONE
				}?.floodFill(Facing4){
					world.getBlock(it) === Blocks.END_STONE && Facing4.all { side -> world.getBlock(it.offset(side)) === Blocks.END_STONE } && world.isAir(it.down())
				}
				
				if (topBlocks.isNullOrEmpty()){
					return
				}
				
				world.addTrigger(rand.nextItem(topBlocks), FluidStructureTrigger(ModBlocks.ENDER_GOO))
				
				for(level in 1..2) for(pos in bottomCenter.up(level).floodFill(Facing4){ world.isAir(it) && !world.isAir(it.down()) }){
					world.addTrigger(pos, FluidStructureTrigger(ModBlocks.ENDER_GOO))
				}
			}
		}
		
		object SmallBlob : BlobContents(){
			override fun after(world: SegmentedWorld, rand: Random, blob: Blob){
				Blob(blob.center, (blob.radius * rand.nextFloat(0.325, 0.525)).coerceIn(Blob.FILLED_RADIUS)).generateFilled(world, rand)
			}
		}
	}
	
	private object Ores{
		private val END_POWDER_ORE = OreGenerator(
			OreTechniqueAdjacent(
				oresPerCluster = { rand -> rand.nextInt(3, rand.nextInt(4, 6)) },
				allowDiagonals = true
			),
			
			BlockReplacer(
				fill = ModBlocks.END_POWDER_ORE,
				replace = Blocks.END_STONE
			),
			
			chunkSize = 24,
			attemptsPerChunk = 50,
			clustersPerChunk = Linear(2, 3)
		)
		
		private val ENDIUM_ORE = OreGenerator(
			OreTechniqueSingle.withAdjacentAirCheck(),
			
			BlockReplacer(
				fill = ModBlocks.ENDIUM_ORE,
				replace = Blocks.END_STONE
			),
			
			chunkSize = 48,
			attemptsPerChunk = 800,
			clustersPerChunk = Constant(1)
		)
		
		val STARDUST_ORE_TECHNIQUE = OreTechniqueDistance(
			oresPerCluster = Linear(3, 8),
			maxDistance = 3.0,
			powDistance = 0.5,
			attemptMultiplier = 2F
		)
		
		val STARDUST_ORE_PLACER = BlockReplacer(
			fill = ModBlocks.STARDUST_ORE,
			replace = Blocks.END_STONE
		)
		
		private val STARDUST_ORE = OreGenerator(
			STARDUST_ORE_TECHNIQUE,
			STARDUST_ORE_PLACER,
			
			chunkSize = 60,
			attemptsPerChunk = 100,
			clustersPerChunk = Constant(1) // triggered twice
		)
		
		fun generate(world: SegmentedWorld){
			END_POWDER_ORE.generate(world)
			ENDIUM_ORE.generate(world)
			
			repeat(2){
				STARDUST_ORE.generate(world)
			}
			
			// TODO support modded ores w/ rebalancing as described in the doc
		}
	}
}
