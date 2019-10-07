package chylex.hee.game.mechanics.scorching
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.copyIf
import chylex.hee.system.util.isNotEmpty
import chylex.hee.system.util.nextRounded
import chylex.hee.system.util.size
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import java.util.Random
import kotlin.math.pow

object ScorchingFortune{
	private class Spec(private val dropRange: ClosedFloatingPointRange<Float>, private val highestChance: Float){
		constructor(dropRange: IntRange, highestChance: Float) : this((dropRange.first.toFloat())..(dropRange.last.toFloat()), highestChance)
		
		fun nextDropAmount(rand: Random): Int{
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
		Blocks.LIT_REDSTONE_ORE to Spec(3..20, 10F),
		Blocks.QUARTZ_ORE to Spec((1.7F)..(3.1F), 3F),
		ModBlocks.END_POWDER_ORE to Spec(2..5, 2.5F),
		ModBlocks.ENDIUM_ORE to Spec(1..2, 1.2F),
		ModBlocks.IGNEOUS_ROCK_ORE to Spec(1..2, 1.8F)
		// TODO infernium
		// TODO red beryl
	)
	
	private val FORTUNE_BLACKLISTED = mutableSetOf<Block>(
		ModBlocks.STARDUST_ORE
	)
	
	private val FORTUNE_CACHED = mutableMapOf<IBlockState, Spec?>()
	
	// Calculations
	
	private fun createGeneralFortuneSpec(state: IBlockState): Spec?{
		if (FORTUNE_CACHED.containsKey(state)){
			return FORTUNE_CACHED[state]
		}
		
		val defaultRange = estimateDropRange(state)
		
		if (defaultRange.isEmpty()){
			FORTUNE_CACHED[state] = null
			return null
		}
		
		val minimum = defaultRange.first
		val maximum = defaultRange.last
		
		val middleAmount = (minimum + maximum) / 2F
		val extraShift = 1.75F.pow(1F / (1F + (maximum - minimum) / 2F))
		
		val highestChance = middleAmount + extraShift - 1F
		val newRange = minimum..(maximum + 2)
		
		return Spec(newRange, highestChance).also { FORTUNE_CACHED[state] = it }
	}
	
	private fun estimateDropRange(state: IBlockState): IntRange{
		val block = state.block
		val rand = Random(96L)
		
		var minimum = Int.MAX_VALUE
		var maximum = Int.MIN_VALUE
		
		repeat(200){
			val amount = block.quantityDropped(state, 0, rand)
			
			if (amount == 0){
				return IntRange.EMPTY
			}
			
			if (amount < minimum){
				minimum = amount
			}
			
			if (amount > maximum){
				maximum = amount
			}
		}
		
		return minimum..maximum
	}
	
	private fun getStackFromState(state: IBlockState): ItemStack{
		return state.block.let { ItemStack(it, 1, it.getMetaFromState(state)) }
	}
	
	private fun getSmeltingResult(state: IBlockState): ItemStack{
		return if (state.block === Blocks.LIT_REDSTONE_ORE) // TODO ...
			getSmeltingResult(Blocks.REDSTONE_ORE.defaultState)
		else
			FurnaceRecipes.instance().getSmeltingResult(getStackFromState(state)).copyIf { it.isNotEmpty }
	}
	
	// Public
	
	fun canSmelt(state: IBlockState): Boolean{
		return getSmeltingResult(state).isNotEmpty
	}
	
	fun createSmeltedStack(state: IBlockState, rand: Random): ItemStack{
		val block = state.block
		val smelted = getSmeltingResult(state)
		
		if (smelted.isNotEmpty && !FORTUNE_BLACKLISTED.contains(block)){
			val fortuneSpec = FORTUNE_HARDCODED[block] ?: if (smelted.item is ItemBlock) null else createGeneralFortuneSpec(state)
			
			if (fortuneSpec != null){
				smelted.size = fortuneSpec.nextDropAmount(rand)
			}
		}
		
		return smelted
	}
}
