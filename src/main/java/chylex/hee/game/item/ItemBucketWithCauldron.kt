package chylex.hee.game.item
import chylex.hee.game.block.BlockAbstractCauldron
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.facades.Stats
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.playUniversal
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockCauldron
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBucket
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class ItemBucketWithCauldron(containedBlock: Block, private val cauldronBlock: BlockCauldron) : ItemBucket(containedBlock){
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		if (pos.getState(world).let { it.block === Blocks.CAULDRON && it[BlockCauldron.LEVEL] == 0 }){
			if (!world.isRemote){
				if (!player.capabilities.isCreativeMode){
					player.setHeldItem(hand, ItemStack(Items.BUCKET))
				}
				
				player.addStat(Stats.CAULDRON_FILLED)
				pos.setState(world, cauldronBlock.with(BlockCauldron.LEVEL, BlockAbstractCauldron.MAX_LEVEL))
			}
			
			Sounds.ITEM_BUCKET_EMPTY.playUniversal(player, pos, SoundCategory.BLOCKS)
			return SUCCESS
		}
		
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ)
	}
}
