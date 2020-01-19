package chylex.hee.game.block.entity.base
import chylex.hee.game.block.entity.base.TileEntityBase.Context.STORAGE
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getPosOrNull
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.putPos
import chylex.hee.system.util.use
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.math.BlockPos

abstract class TileEntityBaseSpawner(type: TileEntityType<out TileEntityBaseSpawner>) : TileEntityBaseSpecialFirstTick(type){
	private companion object{
		private const val LAST_POS_TAG = "LastPos"
	}
	
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
		
		if (wrld.isRemote){
			clientRotation.update(wrld.rand.nextFloat(0F, 360F))
		}
	}
	
	final override fun tick(){
		super.tick()
		
		if (wrld.isRemote){
			clientRotation.update(clientRotation.currentValue + clientRotationSpeed)
			tickClient()
		}
		else{
			tickServer()
		}
	}
	
	protected abstract fun tickClient()
	protected abstract fun tickServer()
	
	override fun writeNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == STORAGE){
			lastPos?.let { putPos(LAST_POS_TAG, it) }
		}
	}
	
	override fun readNBT(nbt: TagCompound, context: Context) = nbt.use {
		if (context == STORAGE){
			lastPos = getPosOrNull(LAST_POS_TAG)
		}
	}
}
