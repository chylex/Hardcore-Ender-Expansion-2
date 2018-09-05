package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHardnessWithResistance
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import chylex.hee.game.block.state.PropertyDefault
import chylex.hee.game.block.state.PropertyDefault.DEFAULT
import net.minecraft.block.Block
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockSlab.EnumBlockHalf.BOTTOM
import net.minecraft.block.BlockSlab.EnumBlockHalf.TOP
import net.minecraft.block.properties.IProperty
import net.minecraft.block.properties.PropertyEnum
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

abstract class BlockSlabCustom(builder: Builder) : BlockSlab(builder.material, builder.mapColor){
	companion object{
		@JvmStatic val VARIANT: PropertyEnum<PropertyDefault> = PropertyEnum.create<PropertyDefault>("variant", PropertyDefault::class.java)
	}
	
	init{
		setHarvestTool(builder.harvestTool)
		slipperiness = builder.slipperiness
		soundType = builder.soundType
	}
	
	class Half(builder: Builder): BlockSlabCustom(builder){
		init{
			defaultState = blockState.baseState.withProperty(VARIANT, DEFAULT).withProperty(HALF, BOTTOM)
			
			setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance, 0.5F)
		}
		
		override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Item.getItemFromBlock(this)
		
		override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState): ItemStack = ItemStack(this)
		
		override fun getMetaFromState(state: IBlockState): Int = if (state.getValue(HALF) == BOTTOM) 0 else 8
		
		override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(HALF, if (meta == 0) BOTTOM else TOP)
		
		override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, HALF, VARIANT)
		
		override fun isDouble() = false
	}
	
	class Full(builder: Builder, private val slabBlock: Block): BlockSlabCustom(builder){
		init{
			defaultState = blockState.baseState.withProperty(VARIANT, DEFAULT)
			
			setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance)
		}
		
		override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Item.getItemFromBlock(slabBlock)
		
		override fun getItem(worldIn: World, pos: BlockPos, state: IBlockState): ItemStack = ItemStack(slabBlock)
		
		override fun getMetaFromState(state: IBlockState): Int = 0
		
		override fun getStateFromMeta(meta: Int): IBlockState = defaultState
		
		override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, VARIANT)
		
		override fun isDouble() = true
	}
	
	override fun getUnlocalizedName(meta: Int): String = super.getUnlocalizedName()
	
	override fun getVariantProperty(): IProperty<*> = VARIANT
	
	override fun getTypeForItem(stack: ItemStack): Comparable<*> = PropertyDefault.DEFAULT
}
