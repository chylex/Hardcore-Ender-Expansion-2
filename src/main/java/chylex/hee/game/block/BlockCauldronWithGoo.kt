package chylex.hee.game.block
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.system.util.get
import chylex.hee.system.util.playUniversal
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.init.PotionTypes
import net.minecraft.init.SoundEvents
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BlockCauldronWithGoo(private val goo: BlockAbstractGoo) : BlockAbstractCauldron(){
	init{
		setHardness(2F)
	}
	
	override fun createFilledBucket(): ItemStack{
		return ItemStack(goo.filledBucket)
	}
	
	override fun onRightClickedWithItem(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, item: Item): Boolean{
		if (item === Items.GLASS_BOTTLE){
			val level = state[LEVEL]
			
			if (level > 0){
				if (!world.isRemote){
					player.addStat(StatList.CAULDRON_USED)
					useAndUpdateHeldItem(player, hand, PotionItems.getBottle(Items.POTIONITEM, PotionTypes.THICK))
					setWaterLevel(world, pos, state, level - 1)
				}
				
				SoundEvents.ITEM_BOTTLE_FILL.playUniversal(player, pos, SoundCategory.BLOCKS)
			}
			
			return true
		}
		
		return super.onRightClickedWithItem(world, pos, state, player, hand, item)
	}
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		goo.onInsideGoo(entity)
	}
	
	override fun fillWithRain(world: World, pos: BlockPos){}
}
