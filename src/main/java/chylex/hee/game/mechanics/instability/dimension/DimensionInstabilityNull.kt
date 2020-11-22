package chylex.hee.game.mechanics.instability.dimension
import chylex.hee.system.serialization.TagCompound
import net.minecraft.util.math.BlockPos

object DimensionInstabilityNull : IDimensionInstability{
	override fun getLevel(pos: BlockPos) = 0
	override fun resetActionMultiplier(pos: BlockPos){}
	override fun triggerAction(amount: UShort, pos: BlockPos){}
	override fun triggerRelief(amount: UShort, pos: BlockPos){}
	
	override fun serializeNBT() = TagCompound()
	override fun deserializeNBT(nbt: TagCompound){}
}
