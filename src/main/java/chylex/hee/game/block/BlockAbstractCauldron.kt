package chylex.hee.game.block
import chylex.hee.system.util.get
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.playUniversal
import net.minecraft.block.BlockCauldron
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class BlockAbstractCauldron : BlockCauldron(){
	init{
		@Suppress("LeakingThis")
		setHardness(2F)
	}
	
	protected abstract fun createFilledBucket(): ItemStack
	
	override fun setWaterLevel(world: World, pos: BlockPos, state: IBlockState, level: Int){
		super.setWaterLevel(world, pos, if (level == 0) Blocks.CAULDRON.defaultState else state, level)
	}
	
	protected fun useAndUpdateHeldItem(player: EntityPlayer, hand: EnumHand, newHeldItem: ItemStack){
		val oldHeldItem = player.getHeldItem(hand)
		
		oldHeldItem.shrink(1)
		
		if (oldHeldItem.isEmpty){
			player.setHeldItem(hand, newHeldItem)
		}
		else if (!player.inventory.addItemStackToInventory(newHeldItem)){
			player.dropItem(newHeldItem, false)
		}
	}
	
	final override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		val item = player.getHeldItem(hand).takeIf { it.isNotEmpty }?.item
		
		if (item == null){
			return true
		}
		
		if (item === Items.BUCKET){
			if (state[LEVEL] == 3){
				if (!world.isRemote){
					player.addStat(StatList.CAULDRON_USED)
					useAndUpdateHeldItem(player, hand, createFilledBucket())
					setWaterLevel(world, pos, state, 0)
				}
				
				SoundEvents.ITEM_BUCKET_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return true
		}
		
		return onRightClickedWithItem(world, pos, state, player, hand, item)
	}
	
	open fun onRightClickedWithItem(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, item: Item): Boolean{
		return false
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){}
	override fun fillWithRain(world: World, pos: BlockPos){}
}
