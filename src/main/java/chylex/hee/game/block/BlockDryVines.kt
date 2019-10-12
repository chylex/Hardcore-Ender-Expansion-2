package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder.Companion.setHarvestTool
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
import chylex.hee.system.migration.Facing
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import net.minecraft.block.BlockVine
import net.minecraft.block.SoundType
import net.minecraft.block.state.BlockFaceShape.SOLID
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ColorizerFoliage
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeColorHelper
import java.util.Random

class BlockDryVines : BlockVine(){
	init{
		setHardness(0.1F)
		setHarvestTool(Pair(WOOD, AXE))
		
		soundType = SoundType.PLANT
	}
	
	// Custom behavior
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){}
	
	override fun isLadder(state: IBlockState, world: IBlockAccess, pos: BlockPos, entity: EntityLivingBase): Boolean{
		return !entity.onGround
	}
	
	override fun canAttachTo(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
		val above = pos.up()
		return isAcceptableNeighbor(world, pos.offset(facing.opposite), facing) && (above.isAir(world) || above.getBlock(world) === this || isAcceptableNeighbor(world, above, Facing.UP))
	}
	
	private fun isAcceptableNeighbor(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
		val state = pos.getState(world)
		return state.getBlockFaceShape(world, pos, facing) == SOLID && !isExceptBlockForAttaching(state.block)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor{
		private fun dryify(color: Int): Int{
			val hsb = IntColor(color).asHSB
			
			return hsb.copy(
				saturation = hsb.saturation * 0.6F,
				brightness = hsb.brightness * 0.8F
			).i
		}
		
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
			return if (world != null && pos != null)
				dryify(BiomeColorHelper.getFoliageColorAtPos(world, pos))
			else
				dryify(ColorizerFoliage.getFoliageColorBasic())
		}
	}
}
