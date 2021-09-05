package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.game.Environment
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.IBlockReader

open class BlockScaffolding protected constructor(builder: BlockBuilder) : HeeBlock(builder) {
	companion object {
		var enableShape = true
		
		fun create(builder: BlockBuilder): HeeBlock {
			return HEE.debugModule?.createScaffoldingBlock(builder) ?: BlockScaffolding(builder)
		}
	}
	
	override val model
		get() = BlockModel.Manual
	
	override val renderLayer
		get() = CUTOUT
	
	// Visuals and physics
	
	override fun getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return if (enableShape)
			VoxelShapes.fullCube()
		else
			VoxelShapes.empty()
	}
	
	override fun getCollisionShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		val player = Environment.getClientSidePlayer()
		
		return if ((player == null || !player.abilities.isFlying) && enableShape)
			VoxelShapes.fullCube()
		else
			VoxelShapes.empty()
	}
	
	override fun getRaytraceShape(state: BlockState, world: IBlockReader, pos: BlockPos): VoxelShape {
		val player = Environment.getClientSidePlayer()
		
		return if ((player == null || player.isSneaking || player.abilities.isFlying) && enableShape)
			VoxelShapes.fullCube()
		else
			VoxelShapes.empty()
	}
	
	@Sided(Side.CLIENT)
	override fun getAmbientOcclusionLightValue(state: BlockState, world: IBlockReader, pos: BlockPos): Float {
		return 1F
	}
}
