package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.block.with
import chylex.hee.game.block.withFacing
import chylex.hee.game.inventory.setStack
import chylex.hee.game.world.allInBoxMutable
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerRoomData
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.INTRODUCTION
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.ObsidianTowerSpawnerStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.proxy.Environment
import chylex.hee.system.MagicValues
import chylex.hee.system.migration.BlockSkull
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.TileEntityFurnace
import chylex.hee.system.migration.TileEntitySkull
import chylex.hee.system.random.nextItem
import chylex.hee.system.random.removeItemOrNull
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets

abstract class ObsidianTowerRoom_General(file: String, val guaranteesSpawnersOnLevel1: Boolean = false) : ObsidianTowerAbstractPieceFromFile<ObsidianTowerRoomData>(file){
	private companion object{
		private val FLOWER_POT_TYPES = arrayOf(
			Blocks.POTTED_RED_MUSHROOM,
			Blocks.POTTED_BROWN_MUSHROOM,
			Blocks.POTTED_CACTUS,
			Blocks.POTTED_DEAD_BUSH,
			Blocks.POTTED_FERN,
			Blocks.POTTED_DARK_OAK_SAPLING
		)
	}
	
	protected abstract fun generateContents(world: IStructureWorld, instance: Instance)
	
	final override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		generateContents(world, instance)
		
		val data = instance.context
		
		if (data?.spawnerLevel == LEVEL_1){
			val rand = world.rand
			val spawners = mutableListOf<BlockPos>()
			
			for(pos in size.minPos.allInBoxMutable(size.maxPos)){
				if (world.getBlock(pos) === ModBlocks.SPAWNER_OBSIDIAN_TOWERS){
					spawners.add(pos.toImmutable())
				}
			}
			
			rand.removeItemOrNull(spawners)?.let {
				placeSpawnerTrigger(world, it, ObsidianTowerRoomData(INTRODUCTION, rand))
			}
			
			for(spawner in spawners){
				placeSpawnerTrigger(world, spawner, data)
			}
		}
	}
	
	protected fun placeSpawner(world: IStructureWorld, pos: BlockPos, instance: Instance){
		val data = instance.context
		
		if (data == null){
			world.setBlock(pos, Blocks.BEDROCK)
		}
		else if (data.spawnerLevel == LEVEL_1){
			world.setBlock(pos, ModBlocks.SPAWNER_OBSIDIAN_TOWERS) // delay adding trigger until generate()
		}
		else{
			placeSpawnerTrigger(world, pos, data)
		}
	}
	
	private fun placeSpawnerTrigger(world: IStructureWorld, pos: BlockPos, data: ObsidianTowerRoomData){
		world.addTrigger(pos, ObsidianTowerSpawnerStructureTrigger(TileEntitySpawnerObsidianTower(data, world.rand), pos, size))
	}
	
	protected fun placeEndermanHead(world: IStructureWorld, pos: BlockPos, rotation: Int){
		world.addTrigger(pos, TileEntityStructureTrigger(ModBlocks.ENDERMAN_HEAD.with(BlockSkull.ROTATION, rotation), TileEntitySkull()))
	}
	
	protected fun placeEndermanHead(world: IStructureWorld, pos: BlockPos, facing: Direction){
		placeEndermanHead(world, pos, when(facing){
			WEST -> 12
			SOUTH -> 8
			EAST -> 4
			else -> 0
		})
	}
	
	protected fun placeLootTrigger(world: IStructureWorld, pos: BlockPos, isSpecial: Boolean){
		world.addTrigger(pos, LootChestStructureTrigger(if (isSpecial) ObsidianTowerPieces.LOOT_SPECIAL else ObsidianTowerPieces.LOOT_GENERAL, world.rand.nextLong()))
	}
	
	protected fun placeChest(world: IStructureWorld, pos: BlockPos, facing: Direction, isSpecial: Boolean = false){
		world.setState(pos, Blocks.CHEST.withFacing(facing))
		placeLootTrigger(world, pos, isSpecial)
	}
	
	protected fun placeFurnace(world: IStructureWorld, pos: BlockPos, facing: Direction){
		val overworld = Environment.getDimension(DimensionType.OVERWORLD)
		val table = Environment.getLootTable(ObsidianTowerPieces.LOOT_FUEL)
		
		val fuel = table.generate(LootContext.Builder(overworld).withRandom(world.rand).build(LootParameterSets.EMPTY)).firstOrNull() ?: ItemStack.EMPTY
		world.addTrigger(pos, TileEntityStructureTrigger(Blocks.FURNACE.withFacing(facing), TileEntityFurnace().apply { setStack(MagicValues.FURNACE_FUEL_SLOT, fuel) }))
	}
	
	protected fun placeFlowerPot(world: IStructureWorld, pos: BlockPos){
		world.setBlock(pos, world.rand.nextItem(FLOWER_POT_TYPES))
	}
}
