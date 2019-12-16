package chylex.hee.game.world.territory
import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.Damage.Companion.TITLE_WITHER
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.DEAL_CREATIVE
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.IGNORE_INVINCIBILITY
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.posVec
import chylex.hee.system.util.remapRange
import chylex.hee.system.util.square
import chylex.hee.system.util.totalTime
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.DamageSource
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingDamageEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@SubscribeAllEvents(modid = HEE.ID)
object TerritoryVoid{
	const val OUTSIDE_VOID_FACTOR = -1F
	const val INSTANT_DEATH_FACTOR = 3F
	
	private fun intersectEllipsoidEdge(center: Vec3d, point: Vec3d, xzRadius: Float, yRadius: Float): Vec3d?{
		val xzRadSq = square(xzRadius)
		val yRadSq = square(yRadius)
		
		val dir = center.directionTowards(point)
		
		val a = (square(dir.x) / xzRadSq) + (square(dir.y) / yRadSq) + (square(dir.z) / xzRadSq)
		val d = 4 * a // (-4) * a * (-1)
		
		if (d < 0.0){
			return null
		}
		
		val dSqrt = sqrt(d)
		val hit1 = -dSqrt / (2 * a)
		val hit2 =  dSqrt / (2 * a)
		
		return center.add(dir.scale(max(hit1, hit2)))
	}
	
	/**
	 * Returns a value between -1 and infinity, representing signed distance from the edge of the territory's void, where negative values are safely inside the territory.
	 */
	private fun getVoidFactorInternal(point: Vec3d): Float{
		val instance = TerritoryInstance.fromPos(point.x.floorToInt(), point.z.floorToInt())
		
		if (instance == null){
			return OUTSIDE_VOID_FACTOR
		}
		
		val territory = instance.territory
		val environment = territory.desc.environment
		
		val xzRadius = territory.chunks * 8F * environment.voidRadiusMpXZ
		val yRadius = territory.height.let { it.last - it.first } * 0.5F * environment.voidRadiusMpY
		
		val center = instance.centerPoint.add(environment.voidCenterOffset)
		val edge = intersectEllipsoidEdge(center, point, xzRadius, yRadius)
		
		if (edge == null){
			return OUTSIDE_VOID_FACTOR
		}
		
		val distance = point.distanceTo(edge) * (if (point.squareDistanceTo(center) < edge.squareDistanceTo(center)) -1 else 1)
		val factor = distance.toFloat() / 64F
		
		return max(OUTSIDE_VOID_FACTOR, factor)
	}
	
	fun getVoidFactor(world: World, point: Vec3d): Float{
		if (world.provider.dimension != HEE.DIM){
			return OUTSIDE_VOID_FACTOR
		}
		
		return getVoidFactorInternal(point)
	}
	
	fun getVoidFactor(entity: Entity): Float{
		if (entity.dimension != HEE.DIM){
			return OUTSIDE_VOID_FACTOR
		}
		
		return getVoidFactorInternal(entity.posVec)
	}
	
	// Tick handling
	
	private const val PLAYER_NEXT_DAMAGE_TIME_TAG = "VoidNextDamageTime"
	
	private val FACTOR_DAMAGE_REMAP_FROM = (0.5F)..(3.0F)
	private val DAMAGE = Damage(DEAL_CREATIVE, IGNORE_INVINCIBILITY())
	
	fun onWorldTick(world: World){
		if (world.totalTime % 3L != 0L){
			return
		}
		
		val entities = world.loadedEntityList
		
		for(index in entities.lastIndex downTo 0){ // killing entity will remove it from the list
			val entity = entities[index]
			val factor = getVoidFactorInternal(entity.posVec)
			
			if (factor < 0F){
				continue
			}
			
			if (factor >= INSTANT_DEATH_FACTOR){
				DAMAGE.dealTo(Float.MAX_VALUE, entity, TITLE_WITHER)
				continue
			}
			
			if (entity !is EntityLivingBase){
				continue
			}
			
			with(entity.heeTag){
				val currentTime = world.totalTime
				val nextDamageTime = getLong(PLAYER_NEXT_DAMAGE_TIME_TAG)
				
				if (currentTime >= nextDamageTime){
					val amount = remapRange(factor, FACTOR_DAMAGE_REMAP_FROM, (2F)..(6F)).ceilToInt().toFloat()
					
					if (DAMAGE.dealTo(amount, entity, TITLE_WITHER)){
						val cooldown = min(30, remapRange(factor, FACTOR_DAMAGE_REMAP_FROM, (30F)..(6F)).floorToInt())
						setLong(PLAYER_NEXT_DAMAGE_TIME_TAG, currentTime + cooldown)
					}
				}
			}
		}
	}
	
	// Event handling
	
	@JvmStatic
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDamage(e: LivingDamageEvent){
		if (e.source === DamageSource.OUT_OF_WORLD && e.entity.let { it is EntityLivingBase && it.dimension == HEE.DIM }){
			e.isCanceled = true
		}
	}
	
	// Debug
	
	@Sided(Side.CLIENT)
	private fun debugEllipsoidEdge(){
		val player = MC.player ?: return
		val instance = TerritoryInstance.fromPos(player) ?: return
		val rand = player.rng
		
		val environment = instance.territory.desc.environment
		val center = instance.centerPoint
		
		val xzRadius = instance.territory.chunks * 8F * environment.voidRadiusMpXZ
		val yRadius = instance.territory.height.let { it.last - it.first } * 0.5F * environment.voidRadiusMpY
		
		val spawner = ParticleSpawnerCustom(
			type = ParticleFadingSpot,
			data = ParticleFadingSpot.Data(color = RGB(128u), lifespan = 40, scale = 0.2F),
			maxRange = 128.0
		)
		
		repeat(1000){
			val pos = player.posVec.add(rand.nextFloat(-64.0, 64.0), rand.nextFloat(-64.0, 64.0), rand.nextFloat(-64.0, 64.0))
			val edge = intersectEllipsoidEdge(center, pos, xzRadius, yRadius)
			
			if (edge != null){
				spawner.spawn(Point(edge, 1), rand)
			}
		}
	}
}
