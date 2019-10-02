package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.commands.sub.client.CommandClientScaffolding
import chylex.hee.game.world.structure.file.StructureFile
import chylex.hee.game.world.util.BoundingBox
import chylex.hee.system.Debug
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.offsetUntil
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.nio.file.Files

class BlockScaffolding(builder: BlockBuilder) : BlockSimple(builder){
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (world.isRemote && player.isSneaking && !player.capabilities.isFlying && Debug.enabled){
			val palette = CommandClientScaffolding.currentPalette
			
			if (palette == null){
				player.sendMessage(TextComponentString("No structure set."))
				return true
			}
			
			val minPos = findMinPos(world, pos)?.let { findMinPos(world, it) } // double pass to find min from any side
			val maxPos = minPos?.let { findMaxPos(world, it) }
			
			if (minPos == null || maxPos == null){
				player.sendMessage(TextComponentString("Could not find structure boundaries."))
				return true
			}
			
			val box = BoundingBox(minPos, maxPos)
			
			val (structureTag, missingMappings) = StructureFile.save(world, box, palette)
			val structureFile = Files.createTempDirectory("HardcoreEnderExpansion_Structure_").resolve(CommandClientScaffolding.currentFile).toFile()
			
			CompressedStreamTools.write(structureTag, structureFile)
			Debug.setClipboardContents(structureFile)
			
			if (missingMappings.isNotEmpty()){
				player.sendMessage(TextComponentString("Missing mappings for states:"))
				
				for(missingMapping in missingMappings){
					player.sendMessage(TextComponentString(" - ${TextFormatting.GRAY}$missingMapping"))
				}
			}
			
			player.sendMessage(TextComponentString("Generated structure file of ${box.size}."))
			return true
		}
		
		return false
	}
	
	// Helpers
	
	private fun find(world: World, pos: BlockPos?, direction: EnumFacing): BlockPos?{
		return pos?.offsetUntil(direction, 0..255){ it.getBlock(world) === Blocks.AIR }?.offset(direction.opposite)
	}
	
	private fun findMinPos(world: World, pos: BlockPos): BlockPos?{
		val bottomPos = find(world, pos, DOWN)
		
		val y = bottomPos?.y
		val x = find(world, bottomPos, WEST)?.x
		val z = find(world, bottomPos, NORTH)?.z
		
		return if (x == null || y == null || z == null) null else Pos(x, y, z)
	}
	
	private fun findMaxPos(world: World, pos: BlockPos): BlockPos?{
		val topPos = find(world, pos, UP)
		
		val y = topPos?.y
		val x = find(world, topPos, EAST)?.x
		val z = find(world, topPos, SOUTH)?.z
		
		return if (x == null || y == null || z == null) null else Pos(x, y, z)
	}
	
	// Visuals and physics
	
	override fun isBlockNormalCube(state: IBlockState) = false
	override fun isNormalCube(state: IBlockState) = false
	override fun isOpaqueCube(state: IBlockState) = false
	
	override fun canCollideCheck(state: IBlockState, hitIfLiquid: Boolean): Boolean{
		val player = HEE.proxy.getClientSidePlayer()
		return player == null || player.isSneaking || player.capabilities.isFlying
	}
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB?{
		val player = HEE.proxy.getClientSidePlayer()
		
		return if (player == null || !player.capabilities.isFlying)
			FULL_BLOCK_AABB
		else
			NULL_AABB
	}
	
	override fun canPlaceTorchOnTop(state: IBlockState, world: IBlockAccess, pos: BlockPos) = true
	override fun causesSuffocation(state: IBlockState) = false
	override fun getRenderLayer() = CUTOUT
}
