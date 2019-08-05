package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidBase
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.util.FLAG_NOTIFY_NEIGHBORS
import chylex.hee.system.util.FLAG_RENDER_IMMEDIATE
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.get
import chylex.hee.system.util.getLongOrNull
import chylex.hee.system.util.getOrCreateCompound
import chylex.hee.system.util.getState
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.setBlock
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.stats.StatList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.BlockFluidClassic
import net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.UUID

abstract class BlockAbstractGoo(private val fluid: FluidBase, material: Material) : BlockFluidClassic(fluid, material){
	private companion object{
		private const val LAST_TIME_TAG = "Time"
		private const val TOTAL_TICKS_TAG = "Ticks"
	}
	
	// Initialization
	
	private var lastCollidingEntity = ThreadLocal<Pair<Long, UUID>?>()
	
	abstract val filledBucket: Item
	protected abstract val tickTrackingKey: String
	
	init{
		enableStats = false
		
		@Suppress("LeakingThis")
		setQuantaPerBlock(5)
		
		@Suppress("LeakingThis")
		MinecraftForge.EVENT_BUS.register(this)
	}
	
	// Behavior
	
	@SubscribeEvent
	fun onFillBucket(e: FillBucketEvent){
		val target = e.target
		
		if (target == null || target.typeOfHit != BLOCK){
			return
		}
		
		val world = e.world
		val player = e.entityPlayer
		val pos = target.blockPos
		
		if (!BlockEditor.canEdit(pos, e.entityPlayer, e.emptyBucket) || !world.isBlockModifiable(player, pos)){
			return
		}
		
		val state = pos.getState(world)
		
		if (state.block !== this || state[LEVEL] != 0){
			return
		}
		
		player.addStat(StatList.getObjectUseStats(e.emptyBucket.item)!!)
		// TODO sound effect?
		
		pos.setBlock(world, Blocks.AIR, FLAG_NOTIFY_NEIGHBORS or FLAG_SYNC_CLIENT or FLAG_RENDER_IMMEDIATE)
		e.filledBucket = ItemStack(filledBucket)
		e.result = ALLOW
	}
	
	final override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		/*
		 * this prevents calling onInsideGoo/modifyMotion multiple times if the entity is touching 2 or more goo blocks
		 *
		 * because onEntityCollision is always called in succession for all blocks colliding with an entity,
		 * it is enough to compare if either the world time or the entity has changed since last call (on the same thread)
		 */
		
		val currentWorldTime = world.totalWorldTime
		
		if (lastCollidingEntity.get()?.takeUnless { it.first != currentWorldTime || it.second != entity.uniqueID } == null){
			lastCollidingEntity.set(Pair(currentWorldTime, entity.uniqueID))
			
			// handling from Entity.doBlockCollisions
			val bb = entity.entityBoundingBox
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
				
				if (!(entity is EntityPlayer && entity.capabilities.isFlying)){
					modifyMotion(entity, lowestLevel)
				}
			}
		}
	}
	
	protected fun trackTick(entity: Entity, maxTicks: Int): Int{
		val world = entity.world
		val currentWorldTime = world.totalWorldTime
		
		with(entity.heeTag.getOrCreateCompound(tickTrackingKey)){
			val lastWorldTime = getLongOrNull(LAST_TIME_TAG) ?: (currentWorldTime - 1)
			var totalTicks = getInteger(TOTAL_TICKS_TAG)
			
			val ticksSinceLastUpdate = currentWorldTime - lastWorldTime
			
			if (ticksSinceLastUpdate > 1L){
				totalTicks = (totalTicks - (ticksSinceLastUpdate / 2).toInt()).coerceAtLeast(0)
			}
			
			if (totalTicks < maxTicks && world.rand.nextInt(10) != 0){
				++totalTicks
			}
			
			setLong(LAST_TIME_TAG, currentWorldTime)
			setInteger(TOTAL_TICKS_TAG, totalTicks)
			
			return totalTicks
		}
	}
	
	abstract fun onInsideGoo(entity: Entity)
	abstract fun modifyMotion(entity: Entity, level: Int)
	
	// Colors
	
	@SideOnly(Side.CLIENT)
	override fun getFogColor(world: World, pos: BlockPos, state: IBlockState, entity: Entity, originalColor: Vec3d, partialTicks: Float): Vec3d{
		return fluid.fogColor
	}
	
	override fun getMapColor(state: IBlockState, world: IBlockAccess, pos: BlockPos): MapColor{
		return fluid.mapColor
	}
}
