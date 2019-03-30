package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.system.util.getTile
import chylex.hee.system.util.setState
import net.minecraft.block.BlockBed
import net.minecraft.block.BlockBed.EnumPartType.FOOT
import net.minecraft.block.BlockBed.EnumPartType.HEAD
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.tileentity.TileEntityBed
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class BedStructureTrigger(private val facing: EnumFacing, private val color: EnumDyeColor) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, rotation: Rotation){
		val rotatedFacing = rotation.rotate(facing)
		val baseState = Blocks.BED.defaultState.withProperty(BlockBed.FACING, rotatedFacing).withProperty(BlockBed.OCCUPIED, false)
		
		val footPos = pos
		val headPos = footPos.offset(rotatedFacing)
		
		footPos.setState(world, baseState.withProperty(BlockBed.PART, FOOT))
		headPos.setState(world, baseState.withProperty(BlockBed.PART, HEAD))
		
		footPos.getTile<TileEntityBed>(world)?.color = color
		headPos.getTile<TileEntityBed>(world)?.color = color
	}
}
