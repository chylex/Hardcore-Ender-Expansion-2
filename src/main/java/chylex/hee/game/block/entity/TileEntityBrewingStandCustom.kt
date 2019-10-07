package chylex.hee.game.block.entity
import chylex.hee.game.container.ContainerBrewingStandCustom
import chylex.hee.game.mechanics.potion.brewing.PotionItems
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.getStack
import chylex.hee.system.util.getState
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.playServer
import chylex.hee.system.util.setStack
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.BlockBrewingStand
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.Container
import net.minecraft.inventory.InventoryHelper
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntityBrewingStand
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.brewing.BrewingRecipeRegistry
import net.minecraftforge.event.ForgeEventFactory
import java.util.Arrays

class TileEntityBrewingStandCustom : TileEntityBrewingStand(){
	companion object{
		val SLOTS_POTIONS = 0..2
		const val SLOT_REAGENT = 3
		const val SLOT_MODIFIER = 4
		
		private val POTION_SLOTS = SLOTS_POTIONS.toList().toIntArray()
	}
	
	val isEnhanced
		get() = getBlockType() === ModBlocks.ENHANCED_BREWING_STAND
	
	var brewTime
		get() = getField(0)
		set(value){ setField(0, value) }
	
	private var prevReagentItem: Item? = null
	private var prevFilledSlots: BooleanArray? = null
	
	override fun update(){
		val canBrew = canBrew()
		
		if (brewTime > 0){
			if (!canBrew || prevReagentItem != getStackInSlot(SLOT_REAGENT).item){
				brewTime = 0
				prevReagentItem = null
				markDirty()
			}
			else if (--brewTime == 0){
				doBrew()
				markDirty()
			}
			else if (isEnhanced && brewTime > 2){
				--brewTime // double brewing speed
			}
		}
		else if (canBrew){
			brewTime = 400
			prevReagentItem = getStack(SLOT_REAGENT).item
			markDirty()
		}
		
		if (!world.isRemote){
			val filledSlots = createFilledSlotsArray()
			
			if (!Arrays.equals(prevFilledSlots, filledSlots)){
				prevFilledSlots = filledSlots
				
				val state = pos.getState(world)
				
				if (state.block is BlockBrewingStand){
					pos.setState(world, filledSlots.zip(BlockBrewingStand.HAS_BOTTLE).fold(state){ acc, (on, prop) -> acc.with(prop, on) }, FLAG_SYNC_CLIENT)
				}
			}
		}
	}
	
	// Brewing
	
	private fun canBrew(): Boolean{
		if (POTION_SLOTS.all { brewingItemStacks[it].isEmpty }){
			return false
		}
		
		val reagent = getStack(SLOT_REAGENT)
		val endPowder = reagent.item === ModItems.END_POWDER
		
		if (endPowder){
			if (!isEnhanced){
				return false
			}
		}
		else if (POTION_SLOTS.any { slot -> brewingItemStacks[slot].let { it.isNotEmpty && !BrewingRecipeRegistry.hasOutput(it, reagent) } }){
			return false
		}
		
		val modifier = PotionItems.findModifier(getStack(SLOT_MODIFIER))
		
		if (modifier != null){
			val simulatedInputs = NonNullList.create<ItemStack>()
			
			for(stack in brewingItemStacks){
				simulatedInputs.add(stack.copy())
			}
			
			BrewingRecipeRegistry.brewPotions(simulatedInputs, reagent, POTION_SLOTS)
			
			if (POTION_SLOTS.any { slot -> simulatedInputs[slot].let { it.isNotEmpty && !modifier.check(it) } }){
				return false
			}
		}
		else if (endPowder){
			return false
		}
		
		return true
	}
	
	private fun doBrew(){
		if (ForgeEventFactory.onPotionAttemptBrew(brewingItemStacks)){
			return
		}
		
		val reagent = getStack(SLOT_REAGENT)
		
		if (reagent.item !== ModItems.END_POWDER){
			BrewingRecipeRegistry.brewPotions(brewingItemStacks, reagent, POTION_SLOTS)
		}
		
		useIngredient(SLOT_REAGENT)
		
		val modifier = PotionItems.findModifier(getStack(SLOT_MODIFIER))
		
		if (modifier != null){
			for(slot in POTION_SLOTS){
				val potion = brewingItemStacks[slot]
				
				if (potion.isNotEmpty){
					brewingItemStacks[slot] = modifier.apply(potion)
				}
			}
			
			useIngredient(SLOT_MODIFIER)
		}
		
		if (!world.isRemote){
			Sounds.BLOCK_BREWING_STAND_BREW.playServer(world, pos, SoundCategory.BLOCKS)
		}
		
		ForgeEventFactory.onPotionBrewed(brewingItemStacks)
	}
	
	private fun useIngredient(slot: Int){
		val stack = getStack(slot)
		val item = stack.item
		
		stack.shrink(1)
		
		if (item.hasContainerItem(stack)){
			val container = item.getContainerItem(stack)
			
			if (stack.isNotEmpty){
				InventoryHelper.spawnItemStack(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), container)
			}
			else{
				setStack(slot, container)
			}
		}
		else{
			setStack(slot, stack)
		}
	}
	
	// Container
	
	override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean{
		return when(index){
			SLOT_REAGENT -> PotionItems.isReagent(stack)
			SLOT_MODIFIER -> PotionItems.isModifier(stack)
			else -> super.isItemValidForSlot(index, stack)
		}
	}
	
	override fun createContainer(inventory: InventoryPlayer, player: EntityPlayer): Container{
		return ContainerBrewingStandCustom(inventory, this)
	}
	
	override fun getName(): String{
		return if (isEnhanced && !hasCustomName())
			"gui.hee.enhanced_brewing_stand.title"
		else
			super.getName()
	}
	
	// Serialization
	
	override fun shouldRefresh(world: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState): Boolean{
		return newState.block != oldState.block
	}
}
