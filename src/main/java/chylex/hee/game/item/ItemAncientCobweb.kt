package chylex.hee.game.item
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.system.migration.Hand.OFF_HAND
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.size
import chylex.hee.system.util.totalTime
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.min

class ItemAncientCobweb(private val sourceBlock: BlockAncientCobweb) : ItemBlock(sourceBlock){
	override fun onUpdate(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || world.totalTime % 4L != 0L){
			return
		}
		
		if (!(isSelected || (entity is EntityLivingBase && entity.getHeldItem(OFF_HAND) === stack))){
			return
		}
		
		if (entity is EntityPlayer && (entity.isCreative || entity.isSpectator)){
			return
		}
		
		val itemsDisintegrated = min(stack.size, world.rand.nextInt(0, 2))
		stack.shrink(itemsDisintegrated)
		
		repeat(itemsDisintegrated){
			val drops = NonNullList.create<ItemStack>()
			sourceBlock.getDrops(drops, world, BlockPos.ORIGIN, sourceBlock.defaultState, 0)
			
			val front = entity.positionVector.add(entity.lookVec.scale(0.6))
			
			for(drop in drops){
				EntityItem(world, front.x, entity.posY + entity.height * 0.45, front.z, drop).apply {
					motionY = 0.0
					setDefaultPickupDelay()
					world.spawnEntity(this)
				}
			}
		}
	}
}
