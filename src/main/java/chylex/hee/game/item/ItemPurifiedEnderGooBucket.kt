package chylex.hee.game.item

import chylex.hee.game.block.BlockEnderGooPurified
import chylex.hee.game.fx.FxBlockData
import chylex.hee.network.client.PacketClientFX
import net.minecraft.block.CauldronBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.world.World

class ItemPurifiedEnderGooBucket(fluid: Fluid, cauldronBlock: CauldronBlock, properties: Properties) : ItemBucketWithCauldron(fluid, cauldronBlock, properties) {
	override fun tryPlaceContainedLiquid(player: PlayerEntity?, world: World, pos: BlockPos, hit: BlockRayTraceResult?): Boolean {
		if (super.tryPlaceContainedLiquid(player, world, pos, hit)) {
			PacketClientFX(BlockEnderGooPurified.FX_PLACE, FxBlockData(pos)).sendToAllAround(world, pos, 16.0)
			return true
		}
		
		return false
	}
}
