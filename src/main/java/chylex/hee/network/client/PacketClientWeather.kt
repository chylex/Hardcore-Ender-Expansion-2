package chylex.hee.network.client
import chylex.hee.game.entity.effect.EntityTerritoryLightningBolt
import chylex.hee.network.BaseClientPacket
import chylex.hee.network.client.PacketClientWeather.Types.TERRITORY_LIGHTNING_BOLT
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.EntityPlayerSP
import chylex.hee.system.serialization.readVec
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeVec
import net.minecraft.client.world.ClientWorld
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.Vec3d

class PacketClientWeather() : BaseClientPacket(){
	enum class Types{
		TERRITORY_LIGHTNING_BOLT
	}
	
	constructor(type: Types, pos: Vec3d) : this(){
		this.type = type
		this.pos = pos
	}
	
	private var type: Types? = null
	private var pos: Vec3d? = null
	
	override fun write(buffer: PacketBuffer) = buffer.use {
		writeEnumValue(type!!)
		writeVec(pos!!)
	}
	
	override fun read(buffer: PacketBuffer) = buffer.use {
		type = readEnumValue(Types::class.java)
		pos = readVec()
	}
	
	@Sided(Side.CLIENT)
	override fun handle(player: EntityPlayerSP){
		val entity = when(type){
			TERRITORY_LIGHTNING_BOLT -> EntityTerritoryLightningBolt(player.world, pos!!.x, pos!!.y, pos!!.z)
			else -> return
		}
		
		(player.world as ClientWorld).globalEntities.add(entity)
	}
}
