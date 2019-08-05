package chylex.hee.game.item
import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.fx.FxBlockData
import chylex.hee.init.ModBlocks
import chylex.hee.network.client.PacketClientFX
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ItemPurifiedEnderGooBucket : ItemBucketWithCauldron(ModBlocks.PURIFIED_ENDER_GOO, ModBlocks.CAULDRON_PURIFIED_ENDER_GOO){
	override fun tryPlaceContainedLiquid(player: EntityPlayer?, world: World, pos: BlockPos): Boolean{
		if (super.tryPlaceContainedLiquid(player, world, pos)){
			PacketClientFX(BlockEnderGooPurified.FX_PLACE, FxBlockData(pos)).sendToAllAround(world, pos, 16.0)
			return true
		}
		
		return false
	}
}
