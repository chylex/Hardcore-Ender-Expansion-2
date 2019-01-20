package chylex.hee.game.mechanics.table.process
import chylex.hee.game.mechanics.energy.IEnergyQuantity.Units
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable
import org.apache.commons.lang3.math.Fraction

interface ITableProcess : INBTSerializable<NBTTagCompound>{
	val pedestals: Array<BlockPos>
	
	val energyPerTick: Units
	val dustPerTick: Fraction
	
	fun initialize()
	fun revalidate(): Boolean
	fun tick(context: ITableContext)
	fun dispose()
	
	companion object{
		val NO_DUST = Fraction.ZERO!!
	}
}
