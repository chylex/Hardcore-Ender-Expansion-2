package chylex.hee.util.color

import net.minecraft.network.PacketBuffer
import net.minecraft.network.datasync.IDataSerializer

object ColorDataSerializer : IDataSerializer<IntColor> {
	override fun write(buf: PacketBuffer, value: IntColor) {
		buf.writeInt(value.i)
	}
	
	override fun read(buf: PacketBuffer): IntColor {
		return IntColor(buf.readInt())
	}
	
	override fun copyValue(value: IntColor): IntColor {
		return value
	}
}
