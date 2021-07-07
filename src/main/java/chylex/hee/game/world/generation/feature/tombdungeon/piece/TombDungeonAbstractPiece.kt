package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.block.entity.TileEntityJarODust
import chylex.hee.game.block.util.SLAB_TYPE
import chylex.hee.game.block.util.with
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.mechanics.dust.DustLayers
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLoot
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.StructurePiece
import chylex.hee.game.world.generation.trigger.TileEntityStructureTrigger
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.nextRounded
import chylex.hee.util.collection.mutableWeightedListOf
import chylex.hee.util.math.Pos
import chylex.hee.util.math.PosXZ
import chylex.hee.util.math.floorToInt
import net.minecraft.block.Blocks
import net.minecraft.state.properties.SlabType
import net.minecraft.tileentity.ChestTileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

abstract class TombDungeonAbstractPiece : StructurePiece<TombDungeonLevel>() {
	protected abstract val isFancy: Boolean
	
	abstract val sidePathAttachWeight: Int
	abstract val secretAttachWeight: Int
	open val secretAttachY = 0
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		placeLayout(world)
		placeConnections(world, instance)
	}
	
	protected fun placeLayout(world: IStructureWorld) {
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		world.placeCube(Pos(0, 0, 0), Pos(maxX, 0, maxZ), Single(ModBlocks.DUSTY_STONE))
		world.placeWalls(Pos(0, 1, 0), Pos(maxX, maxY, maxZ), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		
		if (size.x > 1 && size.z > 1) {
			world.placeCube(Pos(1, maxY, 1), Pos(maxX - 1, maxY, maxZ - 1), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_CEILING else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		}
	}
	
	protected fun placeCobwebs(world: IStructureWorld, instance: Instance) {
		placeCobwebs(world, instance.context?.let { (0.175F - (0.125F * (it.ordinal / 4F))).pow(1.5F) } ?: 0F)
	}
	
	protected open fun placeCobwebs(world: IStructureWorld, chancePerXZ: Float) {
		if (chancePerXZ < 0.0001F) {
			return
		}
		
		val rand = world.rand
		val cobwebs = mutableListOf<BlockPos>()
		
		repeat((chancePerXZ * size.x * size.z).floorToInt()) {
			for (attempt in 1..3) {
				val pos = Pos(
					rand.nextInt(0, size.maxX),
					rand.nextInt(0, size.maxY),
					rand.nextInt(0, size.maxZ)
				)
				
				if (world.isAir(pos) && (Facing6.any { !world.isAir(pos.offset(it)) } || cobwebs.any { it.distanceSqTo(pos) <= 3.1 })) {
					world.setBlock(pos, ModBlocks.ANCIENT_COBWEB)
					cobwebs.add(pos)
					break
				}
			}
		}
	}
	
	protected fun placeCrumblingCeiling(world: IStructureWorld, instance: Instance, amount: Int) {
		if (instance.context?.let { it === TombDungeonLevel.LAST } == true) {
			return
		}
		
		val rand = world.rand
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		repeat(amount) {
			val basePos = PosXZ(
				rand.nextInt(0, maxX),
				rand.nextInt(0, maxZ)
			)
			
			repeat(rand.nextInt(1, 4)) {
				val testPos = basePos.add(
					rand.nextInt(-2, 2),
					rand.nextInt(-2, 2)
				)
				
				if (testPos.x >= 0 && testPos.z >= 0 && testPos.x <= maxX && testPos.z <= maxZ && world.isAir(testPos.withY(1))) {
					world.setBlock(testPos.withY(1), ModBlocks.DUSTY_STONE_BRICK_SLAB)
					world.setState(testPos.withY(maxY), ModBlocks.DUSTY_STONE_BRICK_SLAB.with(SLAB_TYPE, SlabType.TOP))
				}
			}
		}
	}
	
	protected fun placeWallTorch(world: IStructureWorld, pos: BlockPos, facing: Direction) {
		if (!world.isAir(pos)) {
			return
		}
		
		val wall = world.getBlock(pos.offset(facing.opposite))
		if (wall == ModBlocks.DUSTY_STONE || wall == ModBlocks.DUSTY_STONE_BRICKS || wall == ModBlocks.DUSTY_STONE_BRICK_STAIRS) {
			world.placeBlock(pos, Single(Blocks.REDSTONE_WALL_TORCH.withFacing(facing)))
		}
	}
	
	protected fun placeChest(world: IStructureWorld, instance: Instance, pos: BlockPos, facing: Direction, secret: Boolean = false) {
		val level = instance.context ?: TombDungeonLevel.FIRST
		val chest = ChestTileEntity().apply { TombDungeonLoot.generate(this, world.rand, level, secret) }
		
		world.addTrigger(pos, TileEntityStructureTrigger(Blocks.CHEST.withFacing(facing), chest))
	}
	
	protected fun placeJars(world: IStructureWorld, instance: Instance, availablePositions: List<BlockPos>) {
		val rand = world.rand
		val level = instance.context ?: TombDungeonLevel.FIRST
		
		val jars = availablePositions
			.filter(world::isAir)
			.ifEmpty { return }
			.shuffled(rand)
			.take(1 + rand.nextRounded(0.34F) + rand.nextRounded(0.27F))
			.map { it to DustLayers(TileEntityJarODust.DUST_CAPACITY) }
		
		var dustAmount = rand.nextInt(level.dustPerRoom)
		val dustTypeCount = if (level.isFancy)
			1 + rand.nextRounded(0.4F) + rand.nextRounded(0.25F)
		else
			1 + rand.nextRounded(0.3F)
		
		val dustTypePool = mutableWeightedListOf(
			8 to DustType.SUGAR,
			8 to DustType.GUNPOWDER,
			7 to DustType.ANCIENT_DUST,
			7 to DustType.REDSTONE,
			6 to DustType.END_POWDER,
		)
		
		val dustTypes = Array(dustTypeCount) {
			dustTypePool.removeItem(rand)!!
		}
		
		while (dustAmount > 0) {
			val takenAmount = if (dustAmount <= 24) dustAmount else rand.nextInt(12, dustAmount - 12)
			dustAmount -= takenAmount
			rand.nextItem(jars).second.addDust(rand.nextItem(dustTypes), takenAmount)
		}
		
		for ((jarPos, jarLayers) in jars) {
			if (jarLayers.contents.isNotEmpty()) {
				world.addTrigger(jarPos, TileEntityStructureTrigger(ModBlocks.JAR_O_DUST, TileEntityJarODust().apply { layers.deserializeNBT(jarLayers.serializeNBT()) }))
			}
		}
	}
}
