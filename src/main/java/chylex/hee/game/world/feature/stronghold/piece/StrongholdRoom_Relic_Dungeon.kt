package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntitySkull

class StrongholdRoom_Relic_Dungeon(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem){
	override val lootChestPos = Pos(centerX, 2, 2)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextBoolean()){
			world.addTrigger(Pos(centerX - 1, 2, 2), TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(0); skullRotation = 7 }))
		}
		else{
			world.addTrigger(Pos(centerX + 1, 2, 2), TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(0); skullRotation = 9 }))
		}
		
		repeat(rand.nextInt(9, 15)){
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(3, 10))
			
			if (world.isAir(redstonePos)){
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
