package chylex.hee.game.block.entity

import chylex.hee.game.block.entity.base.TileEntityBaseTable
import chylex.hee.game.block.entity.base.TileEntityBaseTableWithSupportingItem
import chylex.hee.game.inventory.util.setStack
import chylex.hee.game.item.ItemExperienceBottleCustom
import chylex.hee.game.item.util.size
import chylex.hee.game.mechanics.dust.DustType
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import chylex.hee.game.mechanics.table.interfaces.ITableContext
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer.Companion.CONSUME_ONE
import chylex.hee.game.mechanics.table.interfaces.ITableInputTransformer.Companion.CONSUME_STACK
import chylex.hee.game.mechanics.table.interfaces.ITableProcess
import chylex.hee.game.mechanics.table.process.ProcessManyPedestals.State.Work
import chylex.hee.game.mechanics.table.process.ProcessOnePedestal
import chylex.hee.game.mechanics.table.process.serializer.MultiProcessSerializer
import chylex.hee.game.mechanics.table.process.serializer.MultiProcessSerializer.Companion.Mapping
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.init.ModTileEntities
import chylex.hee.util.color.RGB
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.over
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.random.nextFloat
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.inventory.Inventory
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos
import kotlin.math.pow

class TileEntityExperienceTable(type: TileEntityType<TileEntityExperienceTable>) : TileEntityBaseTableWithSupportingItem(type) {
	@Suppress("unused")
	constructor() : this(ModTileEntities.EXPERIENCE_TABLE)
	
	object Type : IHeeTileEntityType<TileEntityExperienceTable> {
		override val blocks
			get() = arrayOf(
				ModBlocks.EXPERIENCE_TABLE_TIER_1,
				ModBlocks.EXPERIENCE_TABLE_TIER_2,
				ModBlocks.EXPERIENCE_TABLE_TIER_3,
			)
	}
	
	override val tableIndicatorColor = RGB(167, 187, 45)
	override val tableDustType = DustType.STARDUST
	
	override val processTickRate = 13
	override val processSerializer = MultiProcessSerializer(
		*SUPPORTING_ITEM_MAPPINGS, Mapping("", ::Process)
	)
	
	private val smeltingInventory = Inventory(1)
	
	override fun isSupportingItem(stack: ItemStack): Boolean {
		return stack.item === Items.GLASS_BOTTLE
	}
	
	override fun getProcessFor(pedestalPos: BlockPos, stack: ItemStack): ITableProcess? {
		val rand = wrld.rand
		val item = stack.item
		
		if (item === Items.EXPERIENCE_BOTTLE) {
			return Process(this, pedestalPos, experience = (0 until stack.size).sumOf { 3 + rand.nextInt(5) + rand.nextInt(5) }, updates = 1, usesBottle = false)
		}
		else if (item === ModItems.EXPERIENCE_BOTTLE) {
			return Process(this, pedestalPos, experience = stack.size * ModItems.EXPERIENCE_BOTTLE.getExperienceAmountPerItem(stack), updates = 0, usesBottle = false)
		}
		else if (item === Items.ENCHANTED_BOOK) {
			val enchantments = EnchantmentHelper.getEnchantments(stack)
			val experience = enchantments.entries.sumOf { (ench, level) -> (1F + (level.toFloat() / ench.maxLevel)).pow(1.5F + (level.coerceAtMost(5) * 0.5F)).floorToInt() }
			val updates = 1 + enchantments.values.sum()
			
			return Process(this, pedestalPos, experience, updates)
		}
		else if (item is BlockItem) {
			val block = item.block
			
			for (attempt in 1..50) {
				val experience = block.getExpDrop(block.defaultState, world, pedestalPos, 0, 0)
				
				if (experience > 0) {
					return Process(this, pedestalPos, (experience * 1.75F).floorToInt().coerceAtLeast(1), updates = 9)
				}
			}
		}
		
		smeltingInventory.setStack(0, stack)
		val recipe = wrld.recipeManager.getRecipe(IRecipeType.SMELTING, smeltingInventory, wrld).orElse(null)
		smeltingInventory.setStack(0, ItemStack.EMPTY)
		
		if (recipe != null) {
			return Process(this, pedestalPos, (recipe.experience * rand.nextFloat(1.75F, 3.25F)).ceilToInt(), updates = 5)
		}
		
		return null
	}
	
	private class Process : ProcessOnePedestal {
		constructor(table: TileEntityBaseTable, pos: BlockPos) : super(table, pos)
		constructor(table: TileEntityBaseTable, nbt: TagCompound) : super(table, nbt)
		
		constructor(table: TileEntityBaseTable, pos: BlockPos, experience: Int, updates: Int, usesBottle: Boolean = true) : this(table, pos) {
			this.experience = experience
			this.updatesLeft = updates
			this.usesBottle = usesBottle
		}
		
		override val energyPerTick =
			Units(1)
		
		override val dustPerTick =
			1 over 6
		
		override val whenFinished
			get() = if (usesBottle)
				CONSUME_ONE
			else
				CONSUME_STACK
		
		private var experience = 0
		private var updatesLeft = 0
		private var usesBottle = false
		
		override fun isInputStillValid(oldInput: ItemStack, newInput: ItemStack): Boolean {
			return oldInput.item === newInput.item
		}
		
		override fun onWorkTick(context: ITableContext, input: ItemStack): State {
			if (updatesLeft == 0) {
				if (usesBottle && context.requestUseSupportingItem(Items.GLASS_BOTTLE, (experience + ItemExperienceBottleCustom.MAX_EXPERIENCE - 1) / ItemExperienceBottleCustom.MAX_EXPERIENCE) == null) {
					return Work.Blocked
				}
				
				return Output(ModItems.EXPERIENCE_BOTTLE.createBottles(experience).toTypedArray())
			}
			
			if (!context.requestUseResources()) {
				return Work.Blocked
			}
			
			--updatesLeft
			return Work.Success
		}
		
		override fun serializeNBT() = super.serializeNBT().apply {
			putShort("Experience", experience.toShort())
			putShort("UpdatesLeft", updatesLeft.toShort())
			putBoolean("UsesBottle", usesBottle)
		}
		
		override fun deserializeNBT(nbt: TagCompound) = with(nbt) {
			super.deserializeNBT(nbt)
			experience = getShort("Experience").toInt()
			updatesLeft = getShort("UpdatesLeft").toInt()
			usesBottle = getBoolean("UsesBottle")
		}
	}
}
