package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityTablePedestal
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.properties.PropertyBool
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.max

class BlockTablePedestal(builder: BlockSimple.Builder) : BlockSimpleShaped(builder, COMBINED_BOX), ITileEntityProvider{
	companion object{
		val IS_LINKED = PropertyBool.create("linked")!!
		
		private const val BOTTOM_SLAB_HALF_WIDTH = 6.0 / 16.0
		private const val BOTTOM_SLAB_TOP_Y = 4.0 / 16.0
		
		private const val MIDDLE_PILLAR_HALF_WIDTH = 3.5 / 16.0
		private const val MIDDLE_PILLAR_TOP_Y = BOTTOM_SLAB_TOP_Y + (6.5 / 16.0)
		
		private const val TOP_SLAB_HALF_WIDTH = 7.0 / 16.0
		private const val TOP_SLAB_TOP_Y = MIDDLE_PILLAR_TOP_Y + (3.0 / 16.0)
		
		private val COLLISION_BOXES = arrayOf(
			AxisAlignedBB(
				0.5 - BOTTOM_SLAB_HALF_WIDTH,               0.0, 0.5 - BOTTOM_SLAB_HALF_WIDTH,
				0.5 + BOTTOM_SLAB_HALF_WIDTH, BOTTOM_SLAB_TOP_Y, 0.5 + BOTTOM_SLAB_HALF_WIDTH
			),
			AxisAlignedBB(
				0.5 - MIDDLE_PILLAR_HALF_WIDTH,   BOTTOM_SLAB_TOP_Y, 0.5 - MIDDLE_PILLAR_HALF_WIDTH,
				0.5 + MIDDLE_PILLAR_HALF_WIDTH, MIDDLE_PILLAR_TOP_Y, 0.5 + MIDDLE_PILLAR_HALF_WIDTH
			),
			AxisAlignedBB(
				0.5 - TOP_SLAB_HALF_WIDTH, MIDDLE_PILLAR_TOP_Y, 0.5 - TOP_SLAB_HALF_WIDTH,
				0.5 + TOP_SLAB_HALF_WIDTH,      TOP_SLAB_TOP_Y, 0.5 + TOP_SLAB_HALF_WIDTH
			)
		)
		
		val COMBINED_BOX = max(BOTTOM_SLAB_HALF_WIDTH, TOP_SLAB_HALF_WIDTH).let {
			AxisAlignedBB(0.5, 0.0, 0.5, 0.5, TOP_SLAB_TOP_Y, 0.5).grow(it, 0.0, it)
		}
	}
	
	init{
		defaultState = blockState.baseState.withProperty(IS_LINKED, false)
	}
	
	override fun createBlockState(): BlockStateContainer = BlockStateContainer(this, IS_LINKED)
	
	override fun getMetaFromState(state: IBlockState): Int = if (state.getValue(IS_LINKED)) 1 else 0
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.withProperty(IS_LINKED, meta == 1)
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityTablePedestal()
	}
	
	override fun addCollisionBoxToList(state: IBlockState, world: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: MutableList<AxisAlignedBB>, entity: Entity?, isActualState: Boolean){
		COLLISION_BOXES.forEach { addCollisionBoxToList(pos, entityBox, collidingBoxes, it) }
	}
	
	override fun getRenderLayer(): BlockRenderLayer = CUTOUT_MIPPED
	
	@SideOnly(Side.CLIENT)
	object Color : IBlockColor{
		private const val NONE = -1
		
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
			if (world == null || pos == null){
				return NONE
			}
			
			return when(tintIndex){
				else -> NONE
			}
		}
	}
}
