package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.Hand.MAIN_HAND
import net.minecraft.block.BlockState
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

class BlockDustyStonePlain(builder: BlockBuilder) : BlockDustyStone(builder){
	override fun canHarvestBlock(state: BlockState, world: IBlockReader, pos: BlockPos, player: PlayerEntity): Boolean{
		return player.getHeldItem(MAIN_HAND).let { EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, it) == 0 || isPickaxeOrShovel(it) }
	}
}
