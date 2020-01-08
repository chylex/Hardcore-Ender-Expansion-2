package chylex.hee.game.world.structure.trigger
import chylex.hee.game.block.util.ColoredBlocks
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.migration.vanilla.BlockBed
import chylex.hee.system.util.withFacing
import net.minecraft.item.DyeColor
import net.minecraft.state.properties.BedPart
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BedStructureTrigger(private val facing: Direction, private val color: DyeColor) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){
		val transformedFacing = transform(facing)
		val baseState = ColoredBlocks.BED.getValue(color).withFacing(transformedFacing).with(BlockBed.OCCUPIED, false)
		
		@Suppress("UnnecessaryVariable")
		val footPos = pos
		val headPos = pos.offset(transformedFacing)
		
		world.setState(footPos, baseState.with(BlockBed.PART, BedPart.FOOT))
		world.setState(headPos, baseState.with(BlockBed.PART, BedPart.HEAD))
	}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){}
}
