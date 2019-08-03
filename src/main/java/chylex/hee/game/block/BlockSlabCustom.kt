package chylex.hee.game.block
import chylex.hee.game.block.BlockSlabCustom.PropertyDefault.DEFAULT
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setHardnessWithResistance
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import chylex.hee.game.block.util.Property
import chylex.hee.system.util.get
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockSlab.EnumBlockHalf.BOTTOM
import net.minecraft.block.BlockSlab.EnumBlockHalf.TOP
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

abstract class BlockSlabCustom(builder: BlockBuilder) : BlockSlab(builder.material, builder.color){
	companion object{
		val VARIANT = Property.enum<PropertyDefault>("variant")
	}
	
	enum class PropertyDefault : IStringSerializable{
		DEFAULT;
		
		override fun getName(): String{
			return "default"
		}
	}
	
	init{
		setupBlockProperties(builder)
		useNeighborBrightness = true
		
		if (!isDouble){
			setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance, 0.5F)
		}
	}
	
	class Half(builder: BlockBuilder): BlockSlabCustom(builder){
		init{
			defaultState = blockState.baseState.with(VARIANT, DEFAULT).with(HALF, BOTTOM)
		}
		
		override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Item.getItemFromBlock(this)
		override fun getItem(world: World, pos: BlockPos, state: IBlockState) = ItemStack(this)
		
		override fun getMetaFromState(state: IBlockState) = if (state[HALF] == BOTTOM) 0 else 8
		override fun getStateFromMeta(meta: Int) = this.with(HALF, if (meta == 0) BOTTOM else TOP)
		
		override fun createBlockState() = BlockStateContainer(this, HALF, VARIANT)
		
		override fun isDouble() = false
	}
	
	class Full(builder: BlockBuilder, private val slabBlock: Block): BlockSlabCustom(builder){
		init{
			defaultState = blockState.baseState.with(VARIANT, DEFAULT)
		}
		
		override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Item.getItemFromBlock(slabBlock)
		override fun getItem(world: World, pos: BlockPos, state: IBlockState) = ItemStack(slabBlock)
		
		override fun getMetaFromState(state: IBlockState) = 0
		override fun getStateFromMeta(meta: Int): IBlockState = defaultState
		
		override fun createBlockState() = BlockStateContainer(this, VARIANT)
		
		override fun isDouble() = true
	}
	
	override fun getTranslationKey(meta: Int): String = super.getTranslationKey()
	
	override fun getVariantProperty() = VARIANT
	
	override fun getTypeForItem(stack: ItemStack) = DEFAULT
}
