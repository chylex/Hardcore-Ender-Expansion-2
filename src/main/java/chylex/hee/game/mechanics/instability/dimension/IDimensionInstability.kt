package chylex.hee.game.mechanics.instability.dimension

import chylex.hee.util.nbt.TagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

interface IDimensionInstability : INBTSerializable<TagCompound> {
	fun getLevel(pos: BlockPos): Int
	fun resetActionMultiplier(pos: BlockPos)
	fun triggerAction(amount: UShort, pos: BlockPos)
	fun triggerRelief(amount: UShort, pos: BlockPos)
}
