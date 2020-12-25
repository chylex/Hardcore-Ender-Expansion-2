package chylex.hee.game.mechanics.scorching

import chylex.hee.game.inventory.copyIfNotEmpty
import chylex.hee.game.inventory.isNotEmpty
import chylex.hee.game.inventory.size
import chylex.hee.init.ModBlocks
import chylex.hee.proxy.Environment
import chylex.hee.system.migration.ItemBlock
import chylex.hee.system.random.nextRounded
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.crafting.IRecipeType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.loot.LootContext
import net.minecraft.world.storage.loot.LootParameterSets
import net.minecraft.world.storage.loot.LootParameters
import java.util.Random
import kotlin.math.pow

object ScorchingFortune {
	private class Spec(private val dropRange: ClosedFloatingPointRange<Float>, private val highestChance: Float) {
		constructor(dropRange: IntRange, highestChance: Float) : this((dropRange.first.toFloat())..(dropRange.last.toFloat()), highestChance)
		
		fun nextDropAmount(rand: Random): Int {
			val curveScale = (dropRange.endInclusive - dropRange.start) * 0.5F
			val fractionalAmount = highestChance + (rand.nextGaussian().toFloat() * curveScale)
			
			return rand.nextRounded(fractionalAmount.coerceIn(dropRange))
		}
	}
	
	// Specification lists
	
	private val FORTUNE_HARDCODED = mapOf(
		Blocks.COAL_ORE to Spec(1..4, 2.4F),
		Blocks.IRON_ORE to Spec(1..3, 1.5F),
		Blocks.GOLD_ORE to Spec(1..2, 1.66F),
		Blocks.DIAMOND_ORE to Spec((1.5F)..(3.2F), 2.25F),
		Blocks.EMERALD_ORE to Spec((1.0F)..(4.5F), 2.5F),
		Blocks.LAPIS_ORE to Spec(5..28, 12F),
		Blocks.REDSTONE_ORE to Spec(3..20, 10F),
		Blocks.NETHER_QUARTZ_ORE to Spec((1.7F)..(3.1F), 3F),
		ModBlocks.END_POWDER_ORE to Spec(2..5, 2.5F),
		ModBlocks.ENDIUM_ORE to Spec(1..2, 1.2F),
		ModBlocks.IGNEOUS_ROCK_ORE to Spec(1..2, 1.8F)
		// TODO infernium
		// TODO red beryl
	)
	
	private val FORTUNE_BLACKLISTED = mutableSetOf<Block>(
		ModBlocks.STARDUST_ORE
	)
	
	private val FORTUNE_CACHED = mutableMapOf<Block, Spec?>()
	
	// Calculations
	
	private fun createGeneralFortuneSpec(world: ServerWorld, block: Block): Spec? {
		if (FORTUNE_CACHED.containsKey(block)) {
			return FORTUNE_CACHED[block]
		}
		
		val defaultRange = estimateDropRange(world, block)
		
		if (defaultRange.isEmpty()) {
			FORTUNE_CACHED[block] = null
			return null
		}
		
		val minimum = defaultRange.first
		val maximum = defaultRange.last
		
		val middleAmount = (minimum + maximum) / 2F
		val extraShift = 1.75F.pow(1F / (1F + (maximum - minimum) / 2F))
		
		val highestChance = middleAmount + extraShift - 1F
		val newRange = minimum..(maximum + 2)
		
		return Spec(newRange, highestChance).also { FORTUNE_CACHED[block] = it }
	}
	
	private fun estimateDropRange(world: ServerWorld, block: Block): IntRange {
		val rand = Random(96L)
		
		val lootTable = Environment.getLootTable(block.lootTable)
		val lootContext = LootContext.Builder(world)
			.withRandom(rand)
			.withParameter(LootParameters.BLOCK_STATE, block.defaultState)
			.withParameter(LootParameters.POSITION, BlockPos.ZERO)
			.withParameter(LootParameters.TOOL, ItemStack(Items.DIAMOND_PICKAXE))
			.build(LootParameterSets.BLOCK)
		
		var minimum = Int.MAX_VALUE
		var maximum = Int.MIN_VALUE
		
		repeat(200) {
			val items = lootTable.generate(lootContext)
			
			if (items.size != 1) {
				return IntRange.EMPTY
			}
			
			val amount = items.first().size
			
			if (amount == 0) {
				return IntRange.EMPTY
			}
			
			if (amount < minimum) {
				minimum = amount
			}
			
			if (amount > maximum) {
				maximum = amount
			}
		}
		
		return minimum..maximum
	}
	
	private fun getSmeltingResult(world: World, block: Block): ItemStack {
		val inventory = Inventory(ItemStack(block))
		val recipe = world.recipeManager.getRecipe(IRecipeType.SMELTING, inventory, world).orElse(null)
		
		return recipe?.recipeOutput?.copyIfNotEmpty() ?: ItemStack.EMPTY
	}
	
	// Public
	
	fun canSmelt(world: World, block: Block): Boolean {
		return getSmeltingResult(world, block).isNotEmpty
	}
	
	fun createSmeltedStack(world: World, block: Block, rand: Random): ItemStack {
		if (world !is ServerWorld) {
			return ItemStack.EMPTY
		}
		
		val smelted = getSmeltingResult(world, block)
		
		if (smelted.isNotEmpty && !FORTUNE_BLACKLISTED.contains(block)) {
			val fortuneSpec = FORTUNE_HARDCODED[block] ?: if (smelted.item is ItemBlock) null else createGeneralFortuneSpec(world, block)
			
			if (fortuneSpec != null) {
				smelted.size = fortuneSpec.nextDropAmount(rand)
			}
		}
		
		return smelted
	}
}
