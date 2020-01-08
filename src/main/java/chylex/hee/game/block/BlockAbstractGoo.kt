package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidBase
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.BlockFlowingFluid
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.getLongOrNull
import chylex.hee.system.util.getOrCreateCompound
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.totalTime
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.World
import java.util.UUID
import java.util.function.Supplier

abstract class BlockAbstractGoo(
	private val fluid: FluidBase,
	material: Material
) : BlockFlowingFluid(Supplier { fluid.still }, Properties.create(material, fluid.mapColor).hardnessAndResistance(fluid.resistance).doesNotBlockMovement().noDrops()){
	protected companion object{
		private const val LAST_TIME_TAG = "Time"
		private const val TOTAL_TICKS_TAG = "Ticks"
		
		const val FLOW_DISTANCE = 5
	}
	
	// Initialization
	
	private var lastCollidingEntity = ThreadLocal<Pair<Long, UUID>?>()
	
	protected abstract val tickTrackingKey: String
	
	init{
		// UPDATE setQuantaPerBlock(5)
	}
	
	// Behavior
	
	final override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity){
		/*
		 * this prevents calling onInsideGoo/modifyMotion multiple times if the entity is touching 2 or more goo blocks
		 *
		 * because onEntityCollision is always called in succession for all blocks colliding with an entity,
		 * it is enough to compare if either the world time or the entity has changed since last call (on the same thread)
		 */
		
		val currentWorldTime = world.totalTime
		
		if (lastCollidingEntity.get()?.takeUnless { it.first != currentWorldTime || it.second != entity.uniqueID } == null){
			lastCollidingEntity.set(Pair(currentWorldTime, entity.uniqueID))
			
			// handling from Entity.doBlockCollisions
			val bb = entity.boundingBox
			val posMin = Pos(bb.minX - 0.001, bb.minY - 0.001, bb.minZ - 0.001)
			val posMax = Pos(bb.maxX + 0.001, bb.maxY + 0.001, bb.maxZ + 0.001)
			
			var lowestLevel = Int.MAX_VALUE
			
			for(testPos in posMin.allInBoxMutable(posMax)){
				val level = testPos.getState(world).takeIf { it.block === this }?.get(LEVEL) ?: continue
				
				if (level < lowestLevel){
					lowestLevel = level
				}
			}
			
			if (lowestLevel != Int.MAX_VALUE){
				if (!world.isRemote){
					onInsideGoo(entity)
				}
				
				if (!(entity is EntityPlayer && entity.abilities.isFlying)){
					modifyMotion(entity, lowestLevel)
				}
			}
		}
	}
	
	protected fun trackTick(entity: Entity, maxTicks: Int): Int{
		val world = entity.world
		val currentWorldTime = world.totalTime
		
		with(entity.heeTag.getOrCreateCompound(tickTrackingKey)){
			val lastWorldTime = getLongOrNull(LAST_TIME_TAG) ?: (currentWorldTime - 1)
			var totalTicks = getInt(TOTAL_TICKS_TAG)
			
			val ticksSinceLastUpdate = currentWorldTime - lastWorldTime
			
			if (ticksSinceLastUpdate > 1L){
				totalTicks = (totalTicks - (ticksSinceLastUpdate / 2).toInt()).coerceAtLeast(0)
			}
			
			if (totalTicks < maxTicks && world.rand.nextInt(10) != 0){
				++totalTicks
			}
			
			putLong(LAST_TIME_TAG, currentWorldTime)
			putInt(TOTAL_TICKS_TAG, totalTicks)
			
			return totalTicks
		}
	}
	
	abstract fun onInsideGoo(entity: Entity)
	abstract fun modifyMotion(entity: Entity, level: Int)
	
	override fun getMaterialColor(state: BlockState, world: IBlockReader, pos: BlockPos): MaterialColor{
		return fluid.mapColor
	}
	
	@Sided(Side.CLIENT)
	override fun getFogColor(state: BlockState, world: IWorldReader, pos: BlockPos, entity: Entity, originalColor: Vec3d, partialTicks: Float): Vec3d{
		return fluid.fogColor
	}
}
