package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBase.Context.STORAGE
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.setPos
import net.minecraft.entity.Entity
import net.minecraft.util.ITickable
import net.minecraft.util.math.BlockPos

abstract class TileEntityBaseSpawner: TileEntityBase(), ITickable{
	protected val isTainted // TODO
		get() = lastPos != null && lastPos != pos
	
	private var lastPos: BlockPos? = null
	
	val clientEntity by lazy { createClientEntity() }
	val clientRotation = LerpedFloat(0F)
	
	protected abstract val clientRotationSpeed: Float
	protected abstract fun createClientEntity(): Entity
	
	override fun firstTick(){
		if (lastPos == null){
			lastPos = pos
			markDirty()
		}
		
		if (world.isRemote){
			clientRotation.update(world.rand.nextFloat(0F, 360F))
		}
	}
	
	final override fun update(){
		if (world.isRemote){
			clientRotation.update(clientRotation.currentValue + clientRotationSpeed)
			tickClient()
		}
		else{
			tickServer()
		}
	}
	
	protected abstract fun tickClient()
	protected abstract fun tickServer()
	
	override fun writeNBT(nbt: TagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			lastPos?.let { setPos("LastPos", it) }
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = with(nbt){
		if (context == STORAGE){
			lastPos = getPosOrNull("LastPos")
		}
	}
}
