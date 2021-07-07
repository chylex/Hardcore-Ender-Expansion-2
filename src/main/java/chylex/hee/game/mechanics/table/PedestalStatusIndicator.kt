package chylex.hee.game.mechanics.table

import chylex.hee.game.block.entity.TileEntityTablePedestal
import chylex.hee.util.color.HCL
import chylex.hee.util.color.IntColor
import chylex.hee.util.color.RGB
import chylex.hee.util.delegate.NotifyOnChange
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.putEnum
import chylex.hee.util.nbt.use
import net.minecraftforge.common.util.INBTSerializable

class PedestalStatusIndicator(private val pedestal: TileEntityTablePedestal) : INBTSerializable<TagCompound> {
	interface IIndicatorColor {
		val color: IntColor
	}
	
	private companion object {
		private const val CONTENTS_TAG = "Contents"
		private const val PROCESS_TAG = "Process"
	}
	
	// Categories
	
	enum class Contents(override val color: IntColor) : IIndicatorColor {
		NONE(RGB(161, 151, 145)),
		WITH_INPUT(HCL(70.0, 100F, 85F)),
		OUTPUTTED(HCL(114.0, 100F, 76F))
	}
	
	enum class Process(override val color: IntColor) : IIndicatorColor {
		PAUSED(HCL(70.0, 100F, 85F)),
		WORKING(HCL(230.0, 100F, 72F)),
		BLOCKED(HCL(15.0, 100F, 64F)),
		DEDICATED_OUTPUT(HCL(286.0, 100F, 82F)),
		SUPPORTING_ITEM(RGB(255u))
	}
	
	// Data
	
	var contents by NotifyOnChange(Contents.NONE, ::onColorUpdated)
	var process by NotifyOnChange<Process?>(null, ::onColorUpdated)
	
	var currentColor: IntColor by pedestal.SyncOnChange(Contents.NONE.color)
		private set
	
	private fun onColorUpdated() {
		currentColor = process?.color ?: contents.color // not resolving .color immediately confuses Kotlin..
		pedestal.markDirty()
	}
	
	// Serialization
	
	override fun serializeNBT() = TagCompound().apply {
		putEnum(CONTENTS_TAG, contents)
		putEnum(PROCESS_TAG, process)
	}
	
	override fun deserializeNBT(nbt: TagCompound) = nbt.use {
		contents = getEnum<Contents>(CONTENTS_TAG) ?: Contents.NONE
		process = getEnum<Process>(PROCESS_TAG)
	}
}
