package chylex.hee.game.item
import chylex.hee.game.block.BlockAncientCobweb
import chylex.hee.system.migration.Hand.OFF_HAND
import chylex.hee.system.migration.vanilla.EntityItem
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.ItemBlock
import chylex.hee.system.util.motionY
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.size
import chylex.hee.system.util.totalTime
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameters
import kotlin.math.min

class ItemAncientCobweb(private val block: BlockAncientCobweb, properties: Properties) : ItemBlock(block, properties){
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean){
		if (world.isRemote || world.totalTime % 4L != 0L){
			return
		}
		
		if (!(isSelected || (entity is EntityLivingBase && entity.getHeldItem(OFF_HAND) === stack))){
			return
		}
		
		if (entity is EntityPlayer && (entity.isCreative || entity.isSpectator)){
			return
		}
		
		val lootContext = LootContext.Builder(world as ServerWorld)
			.withRandom(world.rand)
			.withParameter(LootParameters.POSITION, BlockPos.ZERO)
			.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
		
		val itemsDisintegrated = min(stack.size, world.rand.nextInt(0, 2))
		stack.shrink(itemsDisintegrated)
		
		repeat(itemsDisintegrated){
			val drops = block.getDrops(block.defaultState, lootContext)
			val front = entity.positionVector.add(entity.lookVec.scale(0.6))
			
			for(drop in drops){
				EntityItem(world, front.x, entity.posY + entity.height * 0.45, front.z, drop).apply {
					motionY = 0.0
					setDefaultPickupDelay()
					world.addEntity(this)
				}
			}
		}
	}
}
