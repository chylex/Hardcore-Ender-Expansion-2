package chylex.hee.game.world.explosion

import chylex.hee.game.entity.util.lookPosVec
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.world.util.getFluidState
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.isAir
import chylex.hee.game.world.util.isFullBlock
import chylex.hee.game.world.util.setBlock
import chylex.hee.util.math.Vec
import chylex.hee.util.math.Vec3
import chylex.hee.util.math.center
import chylex.hee.util.math.component1
import chylex.hee.util.math.component2
import chylex.hee.util.math.component3
import chylex.hee.util.math.directionTowards
import chylex.hee.util.math.square
import chylex.hee.util.random.nextFloat
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.enchantment.ProtectionEnchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameters
import net.minecraft.network.play.server.SExplosionPacket
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.event.ForgeEventFactory
import java.util.Random
import kotlin.math.floor
import kotlin.math.max

class ExplosionBuilder {
	var destroyBlocks = true
	var blockDropRateMultiplier: Float? = null
	var blockDropFortune: Int = 0
	
	var spawnFire = false
	var blockFireChance = 3
	
	var damageEntities = true
	var knockbackMultiplier = Vec3.ONE
	
	fun trigger(world: World, source: Entity? = null, x: Double, y: Double, z: Double, strength: Float) {
		if (world.isRemote) {
			return
		}
		
		val explosion = CustomExplosion(world, source, x, y, z, strength, this)
		
		if (ForgeEventFactory.onExplosionStart(world, explosion)) {
			return
		}
		
		explosion.doExplosionA()
		explosion.doExplosionB(false)
		
		val destroyedBlocks = if (destroyBlocks) explosion.affectedBlockPositions else emptyList()
		val playerKnockback = explosion.playerKnockbackMap
		
		for (player in world.players) {
			if (player.getDistanceSq(x, y, z) < square(64)) {
				@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
				(player as ServerPlayerEntity).connection.sendPacket(SExplosionPacket(x, y, z, strength, destroyedBlocks, playerKnockback[player]))
			}
		}
	}
	
	fun clone(explosion: Explosion, source: Entity? = explosion.explosivePlacedBy, strength: Float = explosion.size) {
		val (x, y, z) = explosion.position
		trigger(explosion.world, source, x, y, z, strength)
	}
	
	private class CustomExplosion(
		world: World,
		private val source: Entity?,
		private val centerX: Double,
		private val centerY: Double,
		private val centerZ: Double,
		strength: Float,
		private val builder: ExplosionBuilder,
	) : Explosion(world, source, centerX, centerY, centerZ, strength, false, Mode.NONE /* don't care */) {
		private val rand = Random()
		
		private fun determineAffectedBlocks() {
			val affectedBlocks = mutableSetOf<BlockPos>()
			
			for (pX in 0..15) for (pY in 0..15) for (pZ in 0..15) {
				if (pX == 0 || pX == 15 || pY == 0 || pY == 15 || pZ == 0 || pZ == 15) {
					val offset = Vec(
						(pX / 15.0) * 2.0 - 1.0,
						(pY / 15.0) * 2.0 - 1.0,
						(pZ / 15.0) * 2.0 - 1.0
					).normalize()
					
					var remainingPower = size * rand.nextFloat(0.7F, 1.3F)
					var testX = centerX
					var testY = centerY
					var testZ = centerZ
					
					while (remainingPower > 0F) {
						val pos = BlockPos(testX, testY, testZ)
						val state = pos.getState(world)
						val fluid = pos.getFluidState(world)
						
						if (!state.isAir(world, pos) || !fluid.isEmpty) {
							val blockResistance = max(
								state.getExplosionResistance(world, pos, this),
								fluid.getExplosionResistance(world, pos, this)
							)
							
							val finalResistance = source?.getExplosionResistance(this, world, pos, state, fluid, blockResistance) ?: blockResistance
							remainingPower -= (finalResistance + 0.3F) * 0.3F
						}
						
						if (remainingPower > 0F && (source == null || source.canExplosionDestroyBlock(this, this.world, pos, state, remainingPower))) {
							affectedBlocks.add(pos)
						}
						
						testX += offset.x * 0.3
						testY += offset.y * 0.3
						testZ += offset.z * 0.3
						
						remainingPower -= 0.225F
					}
				}
			}
			
			affectedBlockPositions.addAll(affectedBlocks)
		}
		
		private fun determineAffectedEntitiesAndKnockback() {
			val damageEntities = builder.damageEntities
			val knockbackEntities = builder.knockbackMultiplier != Vec3.ZERO
			
			if (!damageEntities && !knockbackEntities) {
				return
			}
			
			val centerVec = position
			val doubleSize = size * 2.0
			
			val affectedArea = AxisAlignedBB(
				floor(centerX - doubleSize - 1.0),
				floor(centerY - doubleSize - 1.0),
				floor(centerZ - doubleSize - 1.0),
				floor(centerX + doubleSize + 1.0),
				floor(centerY + doubleSize + 1.0),
				floor(centerZ + doubleSize + 1.0)
			)
			
			val entitiesInArea = world.getEntitiesWithinAABBExcludingEntity(source, affectedArea)
			ForgeEventFactory.onExplosionDetonate(world, this, entitiesInArea, doubleSize)
			
			for (entity in entitiesInArea) {
				if (entity.isImmuneToExplosions) {
					continue
				}
				
				if (entity is PlayerEntity) {
					if (entity.isSpectator || (entity.isCreative && entity.abilities.isFlying)) {
						continue
					}
				}
				
				val lookPosVec = entity.lookPosVec
				val distanceScaled = lookPosVec.distanceTo(centerVec) / doubleSize
				
				if (distanceScaled > 1) {
					continue
				}
				
				val blastPower = (1 - distanceScaled) * getBlockDensity(centerVec, entity)
				
				if (damageEntities) {
					val finalDamage = 1F + ((square(blastPower) + blastPower) * size * 7).toInt()
					entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), finalDamage)
				}
				
				if (knockbackEntities) {
					val knockbackPower = (entity as? LivingEntity)?.let { ProtectionEnchantment.getBlastDamageReduction(it, blastPower) } ?: blastPower
					val knockbackVec = centerVec.directionTowards(lookPosVec).mul(builder.knockbackMultiplier.scale(knockbackPower))
					
					entity.motion = entity.motion.add(knockbackVec)
					
					if (entity is PlayerEntity) {
						playerKnockbackMap[entity] = knockbackVec // vanilla uses blastPower instead of knockbackPower here, bug?
					}
				}
			}
		}
		
		private fun destroyAffectedBlocks() {
			if (builder.destroyBlocks) {
				val modifiedRadius = builder.blockDropRateMultiplier?.let { size / it } ?: size
				
				val miningTool = ItemStack(Blocks.TNT)
				val fortuneLevel = builder.blockDropFortune
				
				if (fortuneLevel > 0) {
					EnchantmentHelper.setEnchantments(mapOf(Enchantments.FORTUNE to fortuneLevel), miningTool)
				}
				
				for (pos in affectedBlockPositions) {
					val state = pos.getState(world)
					
					if (!state.isAir(world, pos)) {
						if (world is ServerWorld && state.canDropFromExplosion(world, pos, this)) {
							val tile = if (state.hasTileEntity())
								world.getTileEntity(pos)
							else
								null
							
							LootContext.Builder(world)
								.withRandom(world.rand)
								.withParameter(LootParameters.ORIGIN, pos.center)
								.withParameter(LootParameters.TOOL, miningTool)
								.withParameter(LootParameters.EXPLOSION_RADIUS, modifiedRadius)
								.withNullableParameter(LootParameters.BLOCK_ENTITY, tile)
								.let(state::getDrops)
								.forEach { Block.spawnAsEntity(world, pos, it) }
						}
						
						state.onBlockExploded(world, pos, this)
					}
				}
			}
			
			if (builder.spawnFire) {
				val fireChance = builder.blockFireChance
				
				if (fireChance > 0) {
					val candidates = if (builder.destroyBlocks) affectedBlockPositions else affectedBlockPositions.map(BlockPos::up)
					
					for (pos in candidates) {
						if (pos.isAir(world) && pos.down().isFullBlock(world) && rand.nextInt(fireChance) == 0) {
							pos.setBlock(world, Blocks.FIRE)
						}
					}
				}
			}
		}
		
		// Overrides
		
		override fun doExplosionA() {
			determineAffectedBlocks()
			determineAffectedEntitiesAndKnockback()
		}
		
		override fun doExplosionB(spawnParticles: Boolean) {
			SoundEvents.ENTITY_GENERIC_EXPLODE.playServer(world, centerX, centerY, centerZ, SoundCategory.BLOCKS, volume = 4F, pitch = rand.nextFloat(0.56F, 0.84F))
			destroyAffectedBlocks()
		}
	}
}
