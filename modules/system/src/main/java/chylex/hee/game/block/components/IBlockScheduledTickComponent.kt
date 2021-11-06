package chylex.hee.game.block.components

import chylex.hee.game.block.properties.TickSchedule
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

interface IBlockScheduledTickComponent {
	/**
	 * Return the amount of ticks before the first [onTick] call after the block is added to the world.
	 */
	fun onAdded(state: BlockState, world: World, pos: BlockPos, rand: Random): TickSchedule
	
	/**
	 * Return the amount of ticks before the next [onTick] call.
	 */
	fun onTick(state: BlockState, world: World, pos: BlockPos, rand: Random): TickSchedule
}
