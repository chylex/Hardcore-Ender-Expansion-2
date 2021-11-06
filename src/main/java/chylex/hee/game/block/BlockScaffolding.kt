package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.game.Environment
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.Materials
import net.minecraft.block.BlockState
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes

object BlockScaffolding : HeeBlockBuilder() {
	var enableShape = true
	
	init {
		includeFrom(BlockIndestructible)
		
		model = BlockModel.Manual
		renderLayer = CUTOUT
		
		material = Materials.SCAFFOLDING
		color = MaterialColor.AIR
		sound = SoundType.STONE
		
		isSolid = false
		isOpaque = false
		suffocates = false
		blocksVision = false
		
		components.shape = object : IBlockShapeComponent {
			override fun getShape(state: BlockState): VoxelShape {
				return fullCubeIf(enableShape)
			}
			
			override fun getCollisionShape(state: BlockState): VoxelShape {
				return fullCubeIf(enableShape && Environment.getClientSidePlayer().let { it == null || !it.abilities.isFlying })
			}
			
			override fun getRaytraceShape(state: BlockState): VoxelShape {
				return fullCubeIf(enableShape && Environment.getClientSidePlayer().let { it == null || it.isSneaking || it.abilities.isFlying })
			}
			
			private fun fullCubeIf(condition: Boolean): VoxelShape {
				return if (condition) VoxelShapes.fullCube() else VoxelShapes.empty()
			}
		}
		
		components.ambientOcclusionValue = 1F
		
		components.playerUse = HEE.debugModule?.scaffoldingBlockBehavior
	}
}
