package chylex.hee.game.entity.util
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getUUID
import chylex.hee.system.util.hasUUID
import chylex.hee.system.util.putUUID
import net.minecraft.entity.Entity
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.UUID

class SerializedEntity private constructor(private var uuid: UUID?, private var entity: Entity?){
	constructor() : this(null, null)
	constructor(entity: Entity) : this(entity.uniqueID, entity)
	
	fun reset(){
		this.uuid = null
		this.entity = null
	}
	
	fun set(uuid: UUID){
		this.uuid = uuid
		this.entity = null
	}
	
	fun set(entity: Entity){
		this.uuid = entity.uniqueID
		this.entity = entity
	}
	
	fun get(world: World): Entity?{
		val id = uuid ?: return null
		
		if (entity == null && world is ServerWorld){
			entity = world.getEntityByUuid(id)
		}
		
		return entity
	}
	
	fun writeToNBT(tag: TagCompound, key: String){
		uuid?.let { tag.putUUID(key, it) }
	}
	
	fun readFromNBT(tag: TagCompound, key: String){
		if (tag.hasUUID(key)){
			set(tag.getUUID(key))
		}
		else{
			reset()
		}
	}
}
