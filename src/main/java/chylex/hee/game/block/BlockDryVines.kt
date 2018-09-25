package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.AXE
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
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random

class BlockDryVines : BlockVine(){
	init{
		setHardness(0.1F)
		setHarvestTool(Pair(WOOD, AXE))
		
		soundType = SoundType.PLANT
	}
	
	// Custom behavior
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){}
	
	override fun isLadder(state: IBlockState, world: IBlockAccess, pos: BlockPos, entity: EntityLivingBase): Boolean = false
	
	override fun canAttachTo(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
		val above = pos.up()
		return isAcceptableNeighbor(world, pos.offset(facing.opposite), facing) && (above.isAir(world) || above.getBlock(world) === this || isAcceptableNeighbor(world, above, EnumFacing.UP))
	}
	
	private fun isAcceptableNeighbor(world: World, pos: BlockPos, facing: EnumFacing): Boolean{
		val state = pos.getState(world)
		return state.getBlockFaceShape(world, pos, facing) == SOLID && !isExceptBlockForAttaching(state.block)
	}
	
	// Client side
	
	@SideOnly(Side.CLIENT)
	object Color : IBlockColor{
		private fun dryify(color: Int): Int{
			val hsb = FloatArray(3)
			
			java.awt.Color.RGBtoHSB(
				(color shr 16) and 255,
				(color shr 8) and 255,
				color and 255,
				hsb
			)
			
			hsb[1] *= 0.6F
			hsb[2] *= 0.8F
			
			return java.awt.Color.HSBtoRGB(hsb[0], hsb[1], hsb[2])
		}
		
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
			return if (world != null && pos != null)
				dryify(BiomeColorHelper.getFoliageColorAtPos(world, pos))
			else
				dryify(ColorizerFoliage.getFoliageColorBasic())
		}
	}
}
