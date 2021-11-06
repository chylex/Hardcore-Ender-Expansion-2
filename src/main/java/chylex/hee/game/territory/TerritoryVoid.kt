package chylex.hee.game.territory

import chylex.hee.HEE
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockAbstractPortal
import chylex.hee.game.block.interfaces.getHeeInterface
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_WITHER
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DEAL_CREATIVE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.IGNORE_INVINCIBILITY
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.particle.ParticleFadingSpot
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.territory.storage.VoidData
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.isEndDimension
import chylex.hee.game.world.isInEndDimension
import chylex.hee.game.world.util.getBlock
import chylex.hee.system.heeTag
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.EventPriority
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Pos
import chylex.hee.util.math.ceilToInt
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.range
import chylex.hee.util.math.remapRange
import chylex.hee.util.math.square
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.DamageSource
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.entity.living.LivingDamageEvent
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.streams.toList

@SubscribeAllEvents(modid = HEE.ID)
object TerritoryVoid {
	const val OUTSIDE_VOID_FACTOR = -1F
	const val INSTANT_DEATH_FACTOR = 3F
	
	const val RARE_TERRITORY_START_CORRUPTION_FACTOR = -0.5F
	const val RARE_TERRITORY_MAX_CORRUPTION_FACTOR = 2F
	
	val CLIENT_VOID_DATA = VoidData {}
	
	var debug = false
	
	private fun intersectEllipsoidEdge(center: Vector3d, point: Vector3d, xzRadius: Float, yRadius: Float): Vector3d? {
		val xzRadSq = square(xzRadius)
		val yRadSq = square(yRadius)
		
		val dir = center.directionTowards(point)
		
		val a = (square(dir.x) / xzRadSq) + (square(dir.y) / yRadSq) + (square(dir.z) / xzRadSq)
		val d = 4 * a // (-4) * a * (-1)
		
		if (d < 0.0) {
			return null
		}
		
		val dSqrt = sqrt(d)
		val hit1 = -dSqrt / (2 * a)
		val hit2 = +dSqrt / (2 * a)
		
		return center.add(dir.scale(max(hit1, hit2)))
	}
	
	/**
	 * Returns a value between -1 and infinity, representing signed distance from the edge of the territory's void, where negative values are safely inside the territory.
	 */
	private fun getVoidFactorInternal(world: World, point: Vector3d): Float {
		val instance = TerritoryInstance.fromPos(point.x.floorToInt(), point.z.floorToInt())
		
		if (instance == null) {
			return OUTSIDE_VOID_FACTOR
		}
		
		val territory = instance.territory
		val environment = territory.desc.environment
		
		val xzRadius = territory.chunks * 8F * environment.voidRadiusMpXZ
		val yRadius = territory.height.let { it.last - it.first } * 0.5F * environment.voidRadiusMpY
		
		val center = instance.centerPoint.add(environment.voidCenterOffset)
		val edge = intersectEllipsoidEdge(center, point, xzRadius, yRadius)
		
		val outsideVoidFactor = if (world.isRemote)
			CLIENT_VOID_DATA.voidFactor
		else
			instance.getStorageComponent<VoidData>()?.voidFactor ?: OUTSIDE_VOID_FACTOR
		
		if (edge == null) {
			return outsideVoidFactor
		}
		
		val distance = point.distanceTo(edge) * (if (point.squareDistanceTo(center) < edge.squareDistanceTo(center)) -1 else 1)
		val factor = distance.toFloat() / 64F
		
		return max(outsideVoidFactor, factor)
	}
	
	fun getVoidFactor(world: World, point: Vector3d): Float {
		if (!world.isEndDimension) {
			return OUTSIDE_VOID_FACTOR
		}
		
		return getVoidFactorInternal(world, point)
	}
	
	fun getVoidFactor(entity: Entity): Float {
		if (!entity.isInEndDimension) {
			return OUTSIDE_VOID_FACTOR
		}
		
		return getVoidFactorInternal(entity.world, entity.posVec)
	}
	
	// Tick handling
	
	private const val PLAYER_NEXT_DAMAGE_TIME_TAG = "VoidNextDamageTime"
	
	private val FACTOR_DAMAGE_REMAP_FROM = range(0.5F, 3F)
	private val DAMAGE = Damage(DEAL_CREATIVE, IGNORE_INVINCIBILITY())
	
	fun onWorldTick(world: ServerWorld) {
		if (world.gameTime % 3L != 0L || debug) {
			return
		}
		
		for (entity in world.entities.toList()) {
			val factor = getVoidFactorInternal(world, entity.posVec)
			
			if (factor < 0F) {
				continue
			}
			
			if (factor >= INSTANT_DEATH_FACTOR) {
				DAMAGE.dealTo(Float.MAX_VALUE, entity, TITLE_WITHER)
				continue
			}
			
			if (entity !is LivingEntity || Pos(entity).getBlock(world).getHeeInterface<BlockAbstractPortal.IInnerPortalBlock>() != null) { // protecting entities inside portals should help with server lag frustrations
				continue
			}
			
			with(entity.heeTag) {
				val currentTime = world.gameTime
				val nextDamageTime = getLong(PLAYER_NEXT_DAMAGE_TIME_TAG)
				
				if (currentTime >= nextDamageTime) {
					val amount = remapRange(factor, FACTOR_DAMAGE_REMAP_FROM, range(2F, 6F)).ceilToInt().toFloat()
					
					if (DAMAGE.dealTo(amount, entity, TITLE_WITHER)) {
						val cooldown = min(30, remapRange(factor, FACTOR_DAMAGE_REMAP_FROM, range(30F, 6F)).floorToInt())
						putLong(PLAYER_NEXT_DAMAGE_TIME_TAG, currentTime + cooldown)
					}
				}
			}
		}
	}
	
	// Event handling
	
	@SubscribeEvent(EventPriority.HIGHEST)
	fun onPlayerDamage(e: LivingDamageEvent) {
		if (e.source === DamageSource.OUT_OF_WORLD && e.amount < Float.MAX_VALUE && e.entity.let { it is LivingEntity && it.isInEndDimension }) {
			e.isCanceled = true // delegate out of world damage to custom void damage handling
		}
	}
	
	// Debug
	
	@Sided(Side.CLIENT)
	private fun debugEllipsoidEdge() {
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
		
		repeat(1000) {
			val pos = player.posVec.add(rand.nextFloat(-64.0, 64.0), rand.nextFloat(-64.0, 64.0), rand.nextFloat(-64.0, 64.0))
			val edge = intersectEllipsoidEdge(center, pos, xzRadius, yRadius)
			
			if (edge != null) {
				spawner.spawn(Point(edge, 1), rand)
			}
		}
	}
}
