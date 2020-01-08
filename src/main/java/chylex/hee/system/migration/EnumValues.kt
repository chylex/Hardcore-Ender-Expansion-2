package chylex.hee.system.migration
import net.minecraft.util.ActionResultType
import net.minecraft.util.Direction
import net.minecraft.util.Hand
import net.minecraft.world.Difficulty

object Facing{
	val DOWN  get() = Direction.DOWN
	val UP    get() = Direction.UP
	val NORTH get() = Direction.NORTH
	val SOUTH get() = Direction.SOUTH
	val WEST  get() = Direction.WEST
	val EAST  get() = Direction.EAST
	
	val AXIS_X get() = Direction.Axis.X
	val AXIS_Y get() = Direction.Axis.Y
	val AXIS_Z get() = Direction.Axis.Z
}

object Difficulty{
	val PEACEFUL get() = Difficulty.PEACEFUL
	val EASY     get() = Difficulty.EASY
	val NORMAL   get() = Difficulty.NORMAL
	val HARD     get() = Difficulty.HARD
}

object Hand{
	val MAIN_HAND get() = Hand.MAIN_HAND
	val OFF_HAND  get() = Hand.OFF_HAND
}

object ActionResult{
	val SUCCESS get() = ActionResultType.SUCCESS
	val PASS    get() = ActionResultType.PASS
	val FAIL    get() = ActionResultType.FAIL
}
