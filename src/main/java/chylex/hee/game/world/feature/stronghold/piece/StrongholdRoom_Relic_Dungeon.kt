package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.with
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.BlockSkull
import chylex.hee.system.migration.Blocks
import chylex.hee.system.random.nextInt
import net.minecraft.item.ItemStack

class StrongholdRoom_Relic_Dungeon(file: String, relicItem: ItemStack) : StrongholdRoom_Relic(file, relicItem){
	override val lootChestPos = Pos(centerX, 2, 2)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextBoolean()){
			world.setState(Pos(centerX - 1, 2, 2), Blocks.ZOMBIE_HEAD.with(BlockSkull.ROTATION, 7))
		}
		else{
			world.setState(Pos(centerX + 1, 2, 2), Blocks.ZOMBIE_HEAD.with(BlockSkull.ROTATION, 9))
		}
		
		repeat(rand.nextInt(9, 15)){
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(3, 10))
			
			if (world.isAir(redstonePos)){
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
