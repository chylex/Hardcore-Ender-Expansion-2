package chylex.hee.game.entity.utils
import net.minecraft.entity.Entity
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World
import net.minecraft.world.WorldServer
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
		if (uuid == null){
			return null
		}
		
		if (entity == null && world is WorldServer){
			entity = world.getEntityFromUuid(uuid)
		}
		
		return entity
	}
	
	fun writeToNBT(tag: NBTTagCompound, key: String){
		uuid?.let { tag.setUniqueId(key, it) }
	}
	
	fun readFromNBT(tag: NBTTagCompound, key: String){
		if (tag.hasUniqueId(key)){
			set(tag.getUniqueId(key)!!) // UPDATE: marked as Nullable, but can never actually return null... verify this again
		}
		else{
			reset()
		}
	}
}
