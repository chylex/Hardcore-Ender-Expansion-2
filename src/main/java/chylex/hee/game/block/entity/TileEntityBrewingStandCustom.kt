package chylex.hee.game.block.entity

import chylex.hee.game.block.util.BREWING_STAND_HAS_BOTTLE
import chylex.hee.game.block.util.BlockStateGenerics
import chylex.hee.game.container.ContainerBrewingStandCustom
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.inventory.util.getStack
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.inventory.util.size
import chylex.hee.game.item.util.isNotEmpty
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.brewing.PotionItems
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.ModTileEntities
import chylex.hee.network.client.PacketClientFX
import chylex.hee.util.color.RGB
import net.minecraft.block.BrewingStandBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.InventoryHelper
import net.minecraft.inventory.container.Container
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.BrewingStandTileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.Direction
import net.minecraft.util.NonNullList
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraftforge.common.brewing.BrewingRecipeRegistry
import net.minecraftforge.event.ForgeEventFactory
import java.util.Arrays
import java.util.Random

class TileEntityBrewingStandCustom : BrewingStandTileEntity() {
	companion object {
		val SLOTS_POTIONS = 0..2
		const val SLOT_REAGENT = 3
		const val SLOT_MODIFIER = 4
		
		val TOTAL_SLOTS: Int
		val TOTAL_FIELDS: Int
		
		init {
			val testTile = TileEntityBrewingStandCustom()
			TOTAL_SLOTS = testTile.size
			TOTAL_FIELDS = testTile.field_213954_a.size()
		}
		
		private val POTION_SLOTS = SLOTS_POTIONS.toList().toIntArray()
		
		fun canInsertIntoReagentSlot(stack: ItemStack, isEnhanced: Boolean): Boolean {
			return if (stack.item === ModItems.END_POWDER)
				isEnhanced
			else if (stack.item === ModItems.AMELIOR)
				!isEnhanced
			else
				PotionItems.isReagent(stack)
		}
		
		fun canInsertIntoModifierSlot(stack: ItemStack): Boolean {
			return PotionItems.isModifier(stack)
		}
		
		private val PARTICLE_AMELIORATE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(RGB(180, 46, 214), lifespan = 10..18, scale = 0.9F),
			pos = InBox(-0.325F, 0.325F, -0.4F, 0.25F, -0.3F, 0.325F)
		)
		
		val FX_AMELIORATE = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				PARTICLE_AMELIORATE.spawn(Point(pos, 21), rand)
				SoundEvents.BLOCK_BREWING_STAND_BREW.playClient(pos, SoundCategory.BLOCKS, volume = 1.35F, pitch = 1.1F)
			}
		}
	}
	
	val wrld
		get() = super.world!!
	
	val isEnhanced
		get() = blockState.block === ModBlocks.ENHANCED_BREWING_STAND
	
	private var brewTime
		get() = field_213954_a.get(0)
		set(value) {
			field_213954_a.set(0, value)
		}
	
	private var prevReagentItem: Item? = null
	private var prevFilledSlots: BooleanArray? = null
	
	private var playersViewingGUI = 0
	private var ameliorTimer = 0
	
	override fun getType(): TileEntityType<*> {
		return ModTileEntities.BREWING_STAND
	}
	
	override fun tick() {
		val canBrew = canBrew()
		
		if (brewTime > 0) {
			if (!canBrew || prevReagentItem != getStack(SLOT_REAGENT).item) {
				brewTime = 0
				prevReagentItem = null
				markDirty()
			}
			else if (--brewTime == 0) {
				doBrew()
				markDirty()
			}
			else if (isEnhanced && brewTime > 2) {
				--brewTime // double brewing speed
			}
		}
		else if (canBrew) {
			brewTime = 400
			prevReagentItem = getStack(SLOT_REAGENT).item
			markDirty()
		}
		
		if (!wrld.isRemote) {
			val filledSlots = createFilledSlotsArray()
			
			if (!Arrays.equals(prevFilledSlots, filledSlots)) {
				prevFilledSlots = filledSlots
				
				val state = pos.getState(wrld)
				
				if (state.block is BrewingStandBlock) {
					pos.setState(wrld, filledSlots.zip(BREWING_STAND_HAS_BOTTLE).fold(state) { acc, (on, prop) -> acc.with(prop, on) }, FLAG_SYNC_CLIENT)
				}
			}
			
			if (!isEnhanced && getStack(SLOT_REAGENT).item === ModItems.AMELIOR && playersViewingGUI == 0) {
				if (++ameliorTimer > 25) {
					getStack(SLOT_REAGENT).shrink(1)
					
					val prevState = pos.getState(wrld)
					val newState = prevState.properties.fold(ModBlocks.ENHANCED_BREWING_STAND.defaultState) { acc, prop -> BlockStateGenerics.copyProperty(acc, prevState, prop) }
					
					pos.setState(wrld, newState, FLAG_SYNC_CLIENT)
					markDirty()
					
					PacketClientFX(FX_AMELIORATE, FxBlockData(pos)).sendToAllAround(this, 24.0)
				}
			}
			else if (ameliorTimer > 0) {
				ameliorTimer = 0
			}
		}
	}
	
	// Brewing
	
	private fun canBrew(): Boolean {
		if (POTION_SLOTS.all { brewingItemStacks[it].isEmpty }) {
			return false
		}
		
		val reagent = getStack(SLOT_REAGENT)
		val endPowder = reagent.item === ModItems.END_POWDER
		
		if (endPowder) {
			if (!isEnhanced) {
				return false
			}
		}
		else if (POTION_SLOTS.any { slot -> brewingItemStacks[slot].let { it.isNotEmpty && !BrewingRecipeRegistry.hasOutput(it, reagent) } }) {
			return false
		}
		
		val modifier = PotionItems.findModifier(getStack(SLOT_MODIFIER))
		
		if (modifier != null) {
			val simulatedInputs = NonNullList.create<ItemStack>()
			
			for (stack in brewingItemStacks) {
				simulatedInputs.add(stack.copy())
			}
			
			BrewingRecipeRegistry.brewPotions(simulatedInputs, reagent, POTION_SLOTS)
			
			if (POTION_SLOTS.any { slot -> simulatedInputs[slot].let { it.isNotEmpty && !modifier.check(it) } }) {
				return false
			}
		}
		else if (endPowder) {
			return false
		}
		
		return true
	}
	
	private fun doBrew() {
		if (ForgeEventFactory.onPotionAttemptBrew(brewingItemStacks)) {
			return
		}
		
		val reagent = getStack(SLOT_REAGENT)
		
		if (reagent.item !== ModItems.END_POWDER) {
			BrewingRecipeRegistry.brewPotions(brewingItemStacks, reagent, POTION_SLOTS)
		}
		
		useIngredient(SLOT_REAGENT)
		
		val modifier = PotionItems.findModifier(getStack(SLOT_MODIFIER))
		
		if (modifier != null) {
			for (slot in POTION_SLOTS) {
				val potion = brewingItemStacks[slot]
				
				if (potion.isNotEmpty) {
					brewingItemStacks[slot] = modifier.apply(potion)
				}
			}
			
			useIngredient(SLOT_MODIFIER)
		}
		
		if (!wrld.isRemote) {
			SoundEvents.BLOCK_BREWING_STAND_BREW.playServer(wrld, pos, SoundCategory.BLOCKS)
		}
		
		ForgeEventFactory.onPotionBrewed(brewingItemStacks)
	}
	
	private fun useIngredient(slot: Int) {
		val stack = getStack(slot)
		val item = stack.item
		
		stack.shrink(1)
		
		if (item.hasContainerItem(stack)) {
			val container = item.getContainerItem(stack)
			
			if (stack.isNotEmpty) {
				InventoryHelper.spawnItemStack(wrld, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), container)
			}
			else {
				setStack(slot, container)
			}
		}
		else {
			setStack(slot, stack)
		}
	}
	
	// Container
	
	override fun openInventory(player: PlayerEntity) {
		super.openInventory(player)
		++playersViewingGUI
	}
	
	override fun closeInventory(player: PlayerEntity) {
		super.closeInventory(player)
		--playersViewingGUI
	}
	
	override fun canInsertItem(index: Int, stack: ItemStack, direction: Direction?): Boolean {
		return super.canInsertItem(index, stack, direction) && (stack.item !== ModItems.AMELIOR || getStack(index).isEmpty) // try to limit Amelior stack to 1 item, it's mostly futile though
	}
	
	override fun isItemValidForSlot(index: Int, stack: ItemStack): Boolean {
		return when (index) {
			SLOT_REAGENT  -> canInsertIntoReagentSlot(stack, isEnhanced)
			SLOT_MODIFIER -> canInsertIntoModifierSlot(stack)
			else          -> super.isItemValidForSlot(index, stack)
		}
	}
	
	override fun createMenu(id: Int, inventory: PlayerInventory): Container {
		return ContainerBrewingStandCustom(id, inventory, this, field_213954_a, this)
	}
	
	override fun getDefaultName(): ITextComponent {
		return if (isEnhanced)
			TranslationTextComponent("gui.hee.enhanced_brewing_stand.title")
		else
			super.getDefaultName()
	}
}
