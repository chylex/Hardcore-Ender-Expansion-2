package chylex.hee.game.world.generation.trigger

import chylex.hee.game.block.util.BED_OCCUPIED
import chylex.hee.game.block.util.BED_PART
import chylex.hee.game.block.util.ColoredBlocks
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import net.minecraft.item.DyeColor
import net.minecraft.state.properties.BedPart
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class BedStructureTrigger(private val facing: Direction, private val color: DyeColor) : IStructureTrigger {
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		val transformedFacing = transform(facing)
		val baseState = ColoredBlocks.BED.getValue(color).withFacing(transformedFacing).with(BED_OCCUPIED, false)
		
		@Suppress("UnnecessaryVariable")
		val footPos = pos
		val headPos = pos.offset(transformedFacing)
		
		world.setState(footPos, baseState.with(BED_PART, BedPart.FOOT))
		world.setState(headPos, baseState.with(BED_PART, BedPart.HEAD))
	}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {}
}
