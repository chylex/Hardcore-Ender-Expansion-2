package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidBase
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.system.util.FLAG_NOTIFY_NEIGHBORS
import chylex.hee.system.util.FLAG_RENDER_IMMEDIATE
import chylex.hee.system.util.FLAG_SYNC_CLIENT
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.setBlock
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagInt
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.stats.StatList
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult.Type.BLOCK
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.entity.player.FillBucketEvent
import net.minecraftforge.fluids.BlockFluidClassic
import net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.util.Random
import java.util.UUID

abstract class BlockAbstractGoo(private val fluid: FluidBase, material: Material) : BlockFluidClassic(fluid, material){
	protected companion object{
		const val PERSISTENT_EFFECT_DURATION_TICKS = 8
		const val IGNORE_COLLISION_TICK = -1
		
		@JvmStatic
		protected fun addGooEffect(entity: EntityLivingBase, type: Potion, durationTicks: Int, level: Int = 0){
			val existingEffect = entity.getActivePotionEffect(type)
			
			if (existingEffect == null || (level >= existingEffect.amplifier && durationTicks > existingEffect.duration + 30)){
				entity.addPotionEffect(PotionEffect(type, durationTicks, level, true, true))
			}
		}
		
		protected abstract class CollisionTickerBase(private var lastWorldTime: Long, private var maxTotalTicks: Int) : INBTSerializable<NBTTagInt>{
			private var totalTicks = 0
			
			fun tick(currentWorldTime: Long, rand: Random): Int{
				val ticksSinceLastUpdate = currentWorldTime - lastWorldTime
				
				if (ticksSinceLastUpdate == 0L){
					return IGNORE_COLLISION_TICK
				}
				else if (ticksSinceLastUpdate > 1L){
					totalTicks = (totalTicks - (ticksSinceLastUpdate / 2).toInt()).coerceAtLeast(0)
				}
				
				lastWorldTime = currentWorldTime
				
				if (totalTicks < maxTotalTicks && rand.nextInt(10) != 0){
					++totalTicks
				}
				
				return totalTicks
			}
			
			override fun serializeNBT(): NBTTagInt{
				return NBTTagInt(totalTicks)
			}
			
			override fun deserializeNBT(nbt: NBTTagInt){
				totalTicks = nbt.int
			}
		}
	}
	
	// Initialization
	
	private var lastCollidingEntity = ThreadLocal<Pair<Long, UUID>?>()
	
	protected abstract val filledBucket: Item
	
	init{
		enableStats = false
		tickRate = 18
		
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
	
	override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (!(entity is EntityPlayer && entity.capabilities.isFlying)){
			
			/*
			 * this prevents calling modifyEntityMotion multiple times if the entity is touching 2 or more goo blocks
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
					modifyEntityMotion(entity, lowestLevel)
				}
			}
		}
	}
	
	abstract fun modifyEntityMotion(entity: Entity, level: Int)
	
	// Colors
	
	@SideOnly(Side.CLIENT)
	override fun getFogColor(world: World, pos: BlockPos, state: IBlockState, entity: Entity, originalColor: Vec3d, partialTicks: Float): Vec3d{
		return fluid.fogColor
	}
	
	override fun getMapColor(state: IBlockState, world: IBlockAccess, pos: BlockPos): MapColor{
		return fluid.mapColor
	}
}
