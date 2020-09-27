package chylex.hee.game.entity
import net.minecraft.entity.Entity
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.network.datasync.IDataSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class EntityData<T, U : Entity>(private val key: DataParameter<T>) : ReadWriteProperty<U, T>{
	companion object{
		inline fun <reified U : Entity, T> register(serializer: IDataSerializer<T>): DataParameter<T>{
			return EntityDataManager.createKey(U::class.java, serializer)
		}
	}
	
	override fun getValue(thisRef: U, property: KProperty<*>): T{
		return thisRef.dataManager[key]
	}
	
	override fun setValue(thisRef: U, property: KProperty<*>, value: T){
		if (!thisRef.world.isRemote){
			thisRef.dataManager[key] = value
		}
	}
}
