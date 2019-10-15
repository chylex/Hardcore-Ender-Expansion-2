package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.getTile
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFlowerPot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FlowerPotStructureTrigger(private val flowerStack: ItemStack) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		world.setBlock(pos, Blocks.FLOWER_POT)
	}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		pos.getTile<TileEntityFlowerPot>(world)?.setItemStack(flowerStack)
	}
}
