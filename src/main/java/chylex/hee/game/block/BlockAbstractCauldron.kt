package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.system.util.Pos
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.playUniversal
import net.minecraft.block.BlockCauldron
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.EntityItemPickupEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
abstract class BlockAbstractCauldron : BlockCauldron(){
	companion object{
		const val MAX_LEVEL = 3
		
		@JvmStatic
		@SubscribeEvent
		fun onEntityItemPickup(e: EntityItemPickupEvent){
			val item = e.item
			val pos = Pos(item)
			
			if (pos.getBlock(item.world) is BlockCauldron && Pos(e.entityPlayer) != pos){
				e.isCanceled = true
			}
		}
	}
	
	init{
		@Suppress("LeakingThis")
		setHardness(2F)
	}
	
	protected abstract fun createFilledBucket(): ItemStack?
	protected abstract fun createFilledBottle(): ItemStack?
	
	override fun setWaterLevel(world: World, pos: BlockPos, state: IBlockState, level: Int){
		super.setWaterLevel(world, pos, if (level == 0) Blocks.CAULDRON.defaultState else state, level)
	}
	
	private fun useAndUpdateHeldItem(player: EntityPlayer, hand: EnumHand, newHeldItem: ItemStack){
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
			val filledBucket = createFilledBucket()
			
			if (filledBucket != null && state[LEVEL] == MAX_LEVEL){
				if (!world.isRemote){
					player.addStat(StatList.CAULDRON_USED)
					useAndUpdateHeldItem(player, hand, filledBucket)
					setWaterLevel(world, pos, state, 0)
				}
				
				SoundEvents.ITEM_BUCKET_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return true
		}
		else if (item === Items.GLASS_BOTTLE){
			val filledBottle = createFilledBottle()
			
			if (filledBottle != null && state[LEVEL] > 0){
				if (!world.isRemote){
					player.addStat(StatList.CAULDRON_USED)
					useAndUpdateHeldItem(player, hand, filledBottle)
					setWaterLevel(world, pos, state, state[LEVEL] - 1)
				}
				
				SoundEvents.ITEM_BOTTLE_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return true
		}
		
		return false
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){}
	override fun fillWithRain(world: World, pos: BlockPos){}
}
