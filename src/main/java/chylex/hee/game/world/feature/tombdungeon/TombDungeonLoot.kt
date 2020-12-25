package chylex.hee.game.world.feature.tombdungeon

import chylex.hee.game.inventory.getStack
import chylex.hee.game.inventory.setStack
import chylex.hee.game.inventory.size
import chylex.hee.init.ModBlocks
import chylex.hee.proxy.Environment
import chylex.hee.system.collection.WeightedList
import chylex.hee.system.facades.Resource
import chylex.hee.system.math.floorToInt
import chylex.hee.system.migration.ItemArmor
import chylex.hee.system.migration.ItemTiered
import chylex.hee.system.random.nextBiasedFloat
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import net.minecraft.inventory.EquipmentSlotType
import net.minecraft.inventory.IInventory
import net.minecraft.item.IArmorMaterial
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.ResourceLocation
import net.minecraft.world.dimension.DimensionType
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import java.util.Collections
import java.util.EnumMap
import java.util.Random
import kotlin.math.roundToInt

object TombDungeonLoot {
	private enum class LootType(val weight: Int, val limit: Int, val table: ResourceLocation) {
		ARMOR       (weight = 10, limit = 4, table = Resource.Custom("chests/forgottentombs_set_armor")),
		WEAPONS     (weight = 10, limit = 2, table = Resource.Custom("chests/forgottentombs_set_weapons")),
		TOOLS       (weight =  5, limit = 2, table = Resource.Custom("chests/forgottentombs_set_tools")),
		RESOURCES   (weight = 15, limit = 4, table = Resource.Custom("chests/forgottentombs_set_resources")),
		ESSENCES    (weight =  8, limit = 2, table = Resource.Custom("chests/forgottentombs_set_essences")),
		MISCELLANOUS(weight = 12, limit = 5, table = Resource.Custom("chests/forgottentombs_set_miscellaneous"));
		
		// TODO add modded resources
		
		fun generateItem(rand: Random): ItemStack {
			val overworld = Environment.getDimension(DimensionType.OVERWORLD)
			val table = Environment.getLootTable(table)
			
			return table.generate(LootContext.Builder(overworld).withRandom(rand).build(LootParameterSets.EMPTY)).firstOrNull() ?: ItemStack.EMPTY
		}
		
		fun isCompatible(stack: ItemStack, generated: List<ItemStack>): Boolean {
			when(this) {
				ARMOR -> {
					val slot = getArmorSlot(stack) ?: return true
					
					if (generated.any { getArmorSlot(it) === slot }) {
						return false
					}
					
					val material = getArmorMaterial(stack) ?: return true
					val lockedMaterials = generated.mapNotNull(::getArmorMaterial).toSet()
					
					return lockedMaterials.size < 2 || lockedMaterials.contains(material)
				}
				
				WEAPONS, TOOLS -> {
					val tier = getToolTier(stack) ?: return true
					return generated.none { it.item.javaClass === stack.item.javaClass && getToolTier(it) !== tier }
				}
				
				RESOURCES -> {
					val testStack = when(stack.item) {
						Items.IRON_NUGGET -> ItemStack(Items.IRON_INGOT)
						Items.GOLD_NUGGET -> ItemStack(Items.GOLD_INGOT)
						else              -> stack
					}
					
					return generated.any {
						getToolTier(it)?.repairMaterial?.test(testStack) == true ||
						getArmorMaterial(it)?.repairMaterial?.test(testStack) == true
					}
				}
				
				else -> return true
			}
		}
	}
	
	private val WEIGHTED_LOOT_TYPES = WeightedList(LootType.values().map { it.weight to it })
	
	// Item helpers
	
	private fun getArmorSlot(stack: ItemStack): EquipmentSlotType? {
		return (stack.item as? ItemArmor)?.equipmentSlot
	}
	
	private fun getArmorMaterial(stack: ItemStack): IArmorMaterial? {
		return (stack.item as? ItemArmor)?.armorMaterial
	}
	
	private fun getToolTier(stack: ItemStack): IItemTier? {
		return (stack.item as? ItemTiered)?.tier
	}
	
	// Generation
	
	private fun determineAmount(rand: Random, level: TombDungeonLevel, secret: Boolean): Int {
		val amount = 1 + (rand.nextBiasedFloat(5F) * (3F + (level.ordinal * 0.5F))).roundToInt()
		
		return if (secret)
			(amount / 2) + rand.nextInt(6, 7)
		else if (rand.nextInt(100) < 4)
			(amount * rand.nextFloat(2F, 3F)).floorToInt()
		else
			amount
	}
	
	private fun determineLootTypes(rand: Random, amount: Int): List<LootType> {
		if (amount == 1) {
			return listOf(WEIGHTED_LOOT_TYPES.generateItem(rand))
		}
		
		val types = EnumMap<LootType, Int>(LootType::class.java)
		
		repeat(amount) {
			for(attempt in 1..10) {
				val nextType = WEIGHTED_LOOT_TYPES.generateItem(rand)
				val currentAmount = types.getOrDefault(nextType, 0)
				
				if (currentAmount + 1 <= nextType.limit) {
					types[nextType] = currentAmount + 1
					break
				}
			}
		}
		
		return types.flatMap { (type, amount) -> Collections.nCopies(amount, type) }.sortedBy { it.ordinal }
	}
	
	fun generate(chest: IInventory, rand: Random, level: TombDungeonLevel, secret: Boolean) {
		val amount = determineAmount(rand, level, secret)
		val generatedTypes = determineLootTypes(rand, amount)
		val generatedItems = ArrayList<ItemStack>(amount)
		
		for(type in generatedTypes) {
			for(attempt in 1..10) {
				val stack = type.generateItem(rand)
				
				if (type.isCompatible(stack, generatedItems)) {
					if (secret && stack.isDamageable) {
						stack.damage -= ((stack.damage * 0.1F) + (stack.maxDamage * 0.2F)).roundToInt()
					}
					
					generatedItems.add(stack)
					break
				}
			}
		}
		
		while(generatedItems.size < amount) { // sometimes a compatibility check may fail repeatedly, so just fill it up with whatever
			generatedItems.add(LootType.MISCELLANOUS.generateItem(rand))
		}
		
		for(stack in generatedItems) {
			for(attempt in 1..50) {
				val slot = rand.nextInt(chest.size)
				
				if (chest.getStack(slot).isEmpty) {
					chest.setStack(slot, stack)
					break
				}
			}
		}
		
		repeat(rand.nextInt(0, (2 * (TombDungeonLevel.LAST.ordinal - level.ordinal)))) {
			val slot = rand.nextInt(chest.size)
			
			if (chest.getStack(slot).isEmpty) {
				chest.setStack(slot, ItemStack(ModBlocks.ANCIENT_COBWEB))
			}
		}
	}
}
