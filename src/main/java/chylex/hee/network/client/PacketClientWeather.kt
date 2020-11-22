package chylex.hee.network.client
import chylex.hee.game.entity.spawn
import chylex.hee.init.ModEntities
import chylex.hee.network.BaseClientPacket
import chylex.hee.network.client.PacketClientWeather.Types.TERRITORY_LIGHTNING_BOLT
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.vector.Vector3d

class PacketClientWeather() : BaseClientPacket(){
	enum class Types{
		TERRITORY_LIGHTNING_BOLT
	}
	
	constructor(type: Types, pos: Vector3d) : this(){
		this.type = type
		this.pos = pos
	}
	
	private var type: Types? = null
	private var pos: Vector3d? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeEnumValue(type!!)
		writeVec(pos!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		type = readEnumValue(Types::class.java)
		pos = readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP) = when(type){
		TERRITORY_LIGHTNING_BOLT -> ModEntities.TERRITORY_LIGHTNING_BOLT.spawn(player.world){ moveForced(pos!!) }
		else -> {}
	}
}
