package chylex.hee.game.mechanics.table
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.game.render.util.HCL
import chylex.hee.game.render.util.IColor
import chylex.hee.game.render.util.RGB
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.setEnum
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

class PedestalStatusIndicator(private val pedestal: TileEntityTablePedestal) : INBTSerializable<NBTTagCompound>{
	interface IIndicatorColor{
		val color: IColor
	}
	
	// Categories
	
	enum class Contents(override val color: IColor) : IIndicatorColor{
		NONE(RGB(210u)),
		WITH_INPUT(HCL(70.0, 100F, 85F)),
		OUTPUTTED(HCL(230.0, 100F, 72F))
	}
	
	enum class Process(override val color: IColor) : IIndicatorColor{
		WORKING(HCL(114.0, 100F, 76F)),
		PAUSED(HCL(70.0, 100F, 85F)),
		BLOCKED(HCL(15.0, 100F, 64F))
	}
	
	// Data
	
	var contents by NotifyOnChange(Contents.NONE, ::onColorUpdated)
	var process by NotifyOnChange<Process?>(null, ::onColorUpdated)
	
	var currentColor: IColor by pedestal.SyncOnChange(Contents.NONE.color)
		private set
	
	private fun onColorUpdated(){
		currentColor = process?.color ?: contents.color // not resolving .color immediately confuses Kotlin..
		pedestal.markDirty()
	}
	
	// Serialization
	
	override fun serializeNBT() = NBTTagCompound().apply {
		setEnum("Contents", contents)
		setEnum("Process", process)
	}
	
	override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
		contents = getEnum<Contents>("Contents") ?: Contents.NONE
		process = getEnum<Process>("Process")
	}
}
