package chylex.hee.game.mechanics.table
import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor
import chylex.hee.system.util.color.IntColor.Companion.HCL
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.delegate.NotifyOnChange
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.setEnum
import net.minecraftforge.common.util.INBTSerializable

class PedestalStatusIndicator(private val pedestal: TileEntityTablePedestal) : INBTSerializable<TagCompound>{
	interface IIndicatorColor{
		val color: IntColor
	}
	
	// Categories
	
	enum class Contents(override val color: IntColor) : IIndicatorColor{
		NONE(RGB(161, 151, 145)),
		WITH_INPUT(HCL(70.0, 100F, 85F)),
		OUTPUTTED(HCL(230.0, 100F, 72F))
	}
	
	enum class Process(override val color: IntColor) : IIndicatorColor{
		WORKING(HCL(114.0, 100F, 76F)),
		PAUSED(HCL(70.0, 100F, 85F)),
		BLOCKED(HCL(15.0, 100F, 64F)),
		DEDICATED_OUTPUT(HCL(286.0, 100F, 82F)),
		SUPPORTING_ITEM(RGB(255u))
	}
	
	// Data
	
	var contents by NotifyOnChange(Contents.NONE, ::onColorUpdated)
	var process by NotifyOnChange<Process?>(null, ::onColorUpdated)
	
	var currentColor: IntColor by pedestal.SyncOnChange(Contents.NONE.color)
		private set
	
	private fun onColorUpdated(){
		currentColor = process?.color ?: contents.color // not resolving .color immediately confuses Kotlin..
		pedestal.markDirty()
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		setEnum("Contents", contents)
		setEnum("Process", process)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = with(nbt){
		contents = getEnum<Contents>("Contents") ?: Contents.NONE
		process = getEnum<Process>("Process")
	}
}
