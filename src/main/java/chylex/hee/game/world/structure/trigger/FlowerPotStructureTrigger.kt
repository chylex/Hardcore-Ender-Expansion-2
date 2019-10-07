package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setBlock
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityFlowerPot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class FlowerPotStructureTrigger(private val flowerStack: ItemStack) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		pos.setBlock(world, Blocks.FLOWER_POT)
		pos.getTile<TileEntityFlowerPot>(world)?.setItemStack(flowerStack)
	}
}
