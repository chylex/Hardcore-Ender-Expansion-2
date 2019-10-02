package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerPieces
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerRoomData
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.INTRODUCTION
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.FlowerPotStructureTrigger
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModItems
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.MagicValues
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.removeItemOrNull
import chylex.hee.system.util.setStack
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockPlanks
import net.minecraft.block.BlockTallGrass
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFurnace
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.storage.loot.LootContext
import net.minecraftforge.common.DimensionManager

abstract class ObsidianTowerRoom_General(file: String, val guaranteesSpawnersOnLevel1: Boolean = false) : ObsidianTowerAbstractPieceFromFile<ObsidianTowerRoomData>(file){
	private companion object{
		private val FLOWER_TYPES = arrayOf(
			Blocks.RED_MUSHROOM to 0,
			Blocks.BROWN_MUSHROOM to 0,
			Blocks.CACTUS to 0,
			Blocks.DEADBUSH to 0,
			Blocks.TALLGRASS to BlockTallGrass.EnumType.FERN.meta,
			Blocks.SAPLING to BlockPlanks.EnumType.DARK_OAK.metadata
		)
		
		private fun addSpawnerTrigger(world: IStructureWorld, pos: BlockPos, data: ObsidianTowerRoomData){
			// TODO world.addTrigger(pos, TileEntityStructureTrigger(ModBlocks.SPAWNER_OBSIDIAN_TOWERS, TileEntitySpawnerObsidianTower(pos, data)))
		}
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
				/* TODO if (world.getBlock(pos) === ModBlocks.SPAWNER_OBSIDIAN_TOWERS){
					spawners.add(pos.toImmutable())
				}*/
			}
			
			rand.removeItemOrNull(spawners)?.let {
				addSpawnerTrigger(world, it, ObsidianTowerRoomData(INTRODUCTION, rand))
			}
			
			for(spawner in spawners){
				addSpawnerTrigger(world, spawner, data)
			}
		}
	}
	
	protected fun placeSpawner(world: IStructureWorld, pos: BlockPos, instance: Instance){
		val data = instance.context
		
		if (data == null){
			world.setBlock(pos, Blocks.BEDROCK)
		}
		else if (data.spawnerLevel == LEVEL_1){
			// TODO world.setBlock(pos, ModBlocks.SPAWNER_OBSIDIAN_TOWERS) // delay adding trigger until generate()
		}
		else{
			addSpawnerTrigger(world, pos, data)
		}
	}
	
	protected fun placeEndermanHead(world: IStructureWorld, pos: BlockPos, rotation: Int){
		world.addTrigger(pos, TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, ModItems.ENDERMAN_HEAD.createTileEntity().apply { skullRotation = rotation }))
	}
	
	protected fun placeEndermanHead(world: IStructureWorld, pos: BlockPos, facing: EnumFacing){
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
	
	protected fun placeChest(world: IStructureWorld, pos: BlockPos, facing: EnumFacing, isSpecial: Boolean = false){
		world.setState(pos, Blocks.CHEST.withFacing(facing))
		placeLootTrigger(world, pos, isSpecial)
	}
	
	protected fun placeFurnace(world: IStructureWorld, pos: BlockPos, facing: EnumFacing){
		val overworld = DimensionManager.getWorld(0)
		val table = overworld.lootTableManager.getLootTableFromLocation(ObsidianTowerPieces.LOOT_FUEL)
		
		val fuel = table.generateLootForPools(world.rand, LootContext.Builder(overworld).build()).firstOrNull() ?: ItemStack.EMPTY
		world.addTrigger(pos, TileEntityStructureTrigger(Blocks.FURNACE.withFacing(facing), TileEntityFurnace().apply { setStack(MagicValues.FURNACE_FUEL_SLOT, fuel) }))
	}
	
	protected fun placeFlowerPot(world: IStructureWorld, pos: BlockPos){
		world.addTrigger(pos, FlowerPotStructureTrigger(world.rand.nextItem(FLOWER_TYPES).let { ItemStack(it.first, 1, it.second) }))
	}
}
