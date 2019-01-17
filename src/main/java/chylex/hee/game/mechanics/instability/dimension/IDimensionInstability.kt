package chylex.hee.game.mechanics.instability.dimension
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

interface IDimensionInstability : INBTSerializable<NBTTagCompound>{
	fun resetActionMultiplier(pos: BlockPos)
	fun triggerAction(amount: UShort, pos: BlockPos)
	fun triggerRelief(amount: UShort, pos: BlockPos)
}
