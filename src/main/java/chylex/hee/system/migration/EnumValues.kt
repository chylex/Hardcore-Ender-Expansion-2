package chylex.hee.system.migration
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.world.EnumDifficulty

object Facing{
	val DOWN  get() = EnumFacing.DOWN
	val UP    get() = EnumFacing.UP
	val NORTH get() = EnumFacing.NORTH
	val SOUTH get() = EnumFacing.SOUTH
	val WEST  get() = EnumFacing.WEST
	val EAST  get() = EnumFacing.EAST
	
	val AXIS_X get() = EnumFacing.Axis.X
	val AXIS_Y get() = EnumFacing.Axis.Y
	val AXIS_Z get() = EnumFacing.Axis.Z
}

object Difficulty{
	val PEACEFUL get() = EnumDifficulty.PEACEFUL
	val EASY     get() = EnumDifficulty.EASY
	val NORMAL   get() = EnumDifficulty.NORMAL
	val HARD     get() = EnumDifficulty.HARD
}

object Hand{
	val MAIN_HAND get() = EnumHand.MAIN_HAND
	val OFF_HAND  get() = EnumHand.OFF_HAND
}

object ActionResult{
	val SUCCESS get() = EnumActionResult.SUCCESS
	val PASS    get() = EnumActionResult.PASS
	val FAIL    get() = EnumActionResult.FAIL
}
