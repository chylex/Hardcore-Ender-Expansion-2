package chylex.hee.game.mechanics.instability.dimension
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos

object DimensionInstabilityNull : IDimensionInstability{
	override fun triggerAction(amount: UShort, pos: BlockPos){}
	override fun triggerRelief(amount: UShort, pos: BlockPos){}
	
	override fun serializeNBT() = NBTTagCompound()
	override fun deserializeNBT(nbt: NBTTagCompound){}
}
