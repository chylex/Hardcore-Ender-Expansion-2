package chylex.hee.game.world.feature.basic
import chylex.hee.game.block.BlockVoidPortalInner.Companion.TYPE
import chylex.hee.game.block.BlockVoidPortalInner.Type.HUB
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_ACTIVE
import chylex.hee.game.block.BlockVoidPortalInner.Type.RETURN_INACTIVE
import chylex.hee.game.world.generation.IBlockPlacer
import chylex.hee.game.world.generation.SegmentedWorld
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos

sealed class PortalGenerator(private val frameState: Block, private val innerState: IBlockState){
	object EndPortal : PortalGenerator(ModBlocks.END_PORTAL_FRAME, ModBlocks.END_PORTAL_INNER.defaultState)
	object VoidPortalHub : PortalGenerator(ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER.with(TYPE, HUB))
	object VoidPortalReturnActive : PortalGenerator(ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER.with(TYPE, RETURN_ACTIVE))
	object VoidPortalReturnInactive : PortalGenerator(ModBlocks.VOID_PORTAL_FRAME, ModBlocks.VOID_PORTAL_INNER.with(TYPE, RETURN_INACTIVE))
	
	fun place(world: SegmentedWorld, center: BlockPos, radius: Int = 1, outline: IBlockPlacer? = null, base: Block? = null){
		for(pos in center.allInCenteredBox(radius, 0, radius)){
			world.addTrigger(pos, TileEntityStructureTrigger(innerState, TagCompound()))
		}
		
		val frame = radius + 1
		
		world.placeCube(center.add(-radius, 0, -frame), center.add(radius, 0, -frame), Single(frameState))
		world.placeCube(center.add(-radius, 0,  frame), center.add(radius, 0,  frame), Single(frameState))
		world.placeCube(center.add(-frame, 0, -radius), center.add(-frame, 0, radius), Single(frameState))
		world.placeCube(center.add( frame, 0, -radius), center.add( frame, 0, radius), Single(frameState))
		
		val edge = radius + 2
		
		if (outline != null){
			for(frameOffset in -frame..frame){
				outline.place(world, center.add(frameOffset, 0, -edge))
				outline.place(world, center.add(frameOffset, 0,  edge))
				outline.place(world, center.add(-edge, 0, frameOffset))
				outline.place(world, center.add( edge, 0, frameOffset))
			}
			
			outline.place(world, center.add(-frame, 0, -frame))
			outline.place(world, center.add( frame, 0, -frame))
			outline.place(world, center.add(-frame, 0,  frame))
			outline.place(world, center.add( frame, 0,  frame))
		}
		
		if (base != null){
			world.placeCube(center.add(-edge, -1, -edge), center.add(edge, -1, edge), Single(base))
		}
	}
}
