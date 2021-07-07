package chylex.hee.game.block

import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.Environment
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.command.client.CommandClientScaffolding
import chylex.hee.game.world.generation.structure.file.StructureFile
import chylex.hee.game.world.generation.util.WorldToStructureWorldAdapter
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.offsetUntilExcept
import chylex.hee.system.Debug
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Hand
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import java.nio.file.Files

class BlockScaffolding(builder: BlockBuilder) : BlockSimple(builder), IBlockLayerCutout {
	var enableShape = true
	
	override fun onBlockActivated(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockRayTraceResult): ActionResultType {
		if (world.isRemote && player.isSneaking && !player.abilities.isFlying && Debug.enabled) {
			val palette = CommandClientScaffolding.currentPalette
			
			if (palette == null) {
				player.sendMessage(StringTextComponent("No structure set."), Util.DUMMY_UUID)
				return FAIL
			}
			
			val minPos = findMinPos(world, pos)?.let { findMinPos(world, it) } // double pass to find min from any side
			val maxPos = minPos?.let { findMaxPos(world, it) }
			
			if (minPos == null || maxPos == null) {
				player.sendMessage(StringTextComponent("Could not find structure boundaries."), Util.DUMMY_UUID)
				return FAIL
			}
			
			val box = BoundingBox(minPos, maxPos)
			val serverWorld = Environment.getDimension(world.dimensionKey)
			
			val (structureTag, missingMappings) = StructureFile.save(WorldToStructureWorldAdapter(serverWorld, serverWorld.rand, box.min), box.size, palette, this)
			val structureFile = Files.createTempDirectory("HardcoreEnderExpansion_Structure_").resolve(CommandClientScaffolding.currentFile).toFile()
			
			CompressedStreamTools.write(structureTag, structureFile)
			Debug.setClipboardContents(structureFile)
			
			if (missingMappings.isNotEmpty()) {
				player.sendMessage(StringTextComponent("Missing mappings for states:"), Util.DUMMY_UUID)
				
				for (missingMapping in missingMappings) {
					player.sendMessage(StringTextComponent(" - ${TextFormatting.GRAY}$missingMapping"), Util.DUMMY_UUID)
				}
			}
			
			player.sendMessage(StringTextComponent("Generated structure file of ${box.size}."), Util.DUMMY_UUID)
			return SUCCESS
		}
		
		return FAIL
	}
	
	// Helpers
	
	private fun find(world: World, pos: BlockPos?, direction: Direction): BlockPos? {
		return pos?.offsetUntilExcept(direction, 0..255) { it.getBlock(world) === Blocks.AIR }
	}
	
	private fun findMinPos(world: World, pos: BlockPos): BlockPos? {
		val bottomPos = find(world, pos, DOWN)
		
		val y = bottomPos?.y
		val x = find(world, bottomPos, WEST)?.x
		val z = find(world, bottomPos, NORTH)?.z
		
		return if (x == null || y == null || z == null) null else Pos(x, y, z)
	}
	
	private fun findMaxPos(world: World, pos: BlockPos): BlockPos? {
		val topPos = find(world, pos, UP)
		
		val y = topPos?.y
		val x = find(world, topPos, EAST)?.x
		val z = find(world, topPos, SOUTH)?.z
		
		return if (x == null || y == null || z == null) null else Pos(x, y, z)
	}
	
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
