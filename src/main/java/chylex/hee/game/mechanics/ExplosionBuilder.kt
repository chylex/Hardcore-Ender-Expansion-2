package chylex.hee.game.mechanics
import chylex.hee.system.util.getState
import chylex.hee.system.util.isAir
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.playServer
import chylex.hee.system.util.setBlock
import chylex.hee.system.util.square
import net.minecraft.block.material.Material
import net.minecraft.enchantment.EnchantmentProtection
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.util.DamageSource
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import net.minecraft.world.World
import net.minecraftforge.event.ForgeEventFactory
import java.util.Random
import kotlin.math.floor

class ExplosionBuilder{
	var destroyBlocks = true
	var blockDropRate: Float? = null
	var blockDropFortune: Int = 0
	
	var spawnFire = false
	var blockFireChance = 3
	
	var damageEntities = true
	var knockbackEntities = true
	
	fun trigger(world: World, source: Entity? = null, x: Double, y: Double, z: Double, strength: Float){
		if (world.isRemote){
			return
		}
		
		val explosion = CustomExplosion(world, source, x, y, z, strength, this)
		
		if (ForgeEventFactory.onExplosionStart(world, explosion)){
			return
		}
		
		explosion.doExplosionA()
		explosion.doExplosionB(false)
		
		val destroyedBlocks = if (destroyBlocks) explosion.affectedBlockPositions else emptyList()
		val playerKnockback = explosion.playerKnockbackMap
		
		for(player in world.playerEntities){
			if (player.getDistanceSq(x, y, z) < 4096){
				@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") // UPDATE
				(player as EntityPlayerMP).connection.sendPacket(SPacketExplosion(x, y, z, strength, destroyedBlocks, playerKnockback[player]))
			}
		}
	}
	
	private class CustomExplosion(
		private val world: World,
		private val source: Entity?,
		private val centerX: Double,
		private val centerY: Double,
		private val centerZ: Double,
		private val strength: Float,
		private val builder: ExplosionBuilder
	) : Explosion(world, @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS") source, centerX, centerY, centerZ, strength, false, false){ // UPDATE
		private val rand = Random()
		
		private fun determineAffectedBlocks(){
			val affectedBlocks = mutableSetOf<BlockPos>()
			
			for(pX in 0..15){
				for(pY in 0..15){
					for(pZ in 0..15){
						if (pX == 0 || pX == 15 || pY == 0 || pY == 15 || pZ == 0 || pZ == 15){
							val offset = Vec3d(
								(pX / 15.0) * 2.0 - 1.0,
								(pY / 15.0) * 2.0 - 1.0,
								(pZ / 15.0) * 2.0 - 1.0
							).normalize()
							
							var remainingPower = strength * rand.nextFloat(0.7F, 1.3F)
							var testX = centerX
							var testY = centerY
							var testZ = centerZ
							
							while(remainingPower > 0F){
								val pos = BlockPos(testX, testY, testZ)
								val state = pos.getState(world)
								
								if (state.material !== Material.AIR){
									val explosionResistance = source?.getExplosionResistance(this, world, pos, state) ?: state.block.getExplosionResistance(world, pos, null, this)
									remainingPower -= (explosionResistance + 0.3F) * 0.3F
								}
								
								if (remainingPower > 0F && (source == null || source.canExplosionDestroyBlock(this, this.world, pos, state, remainingPower))){
									affectedBlocks.add(pos)
								}
								
								testX += offset.x * 0.3
								testY += offset.y * 0.3
								testZ += offset.z * 0.3
								
								remainingPower -= 0.225F
							}
						}
					}
				}
			}
			
			affectedBlockPositions.addAll(affectedBlocks)
		}
		
		private fun determineAffectedEntitiesAndKnockback(){
			val damageEntities = builder.damageEntities
			val knockbackEntities = builder.knockbackEntities
			
			if (!damageEntities && !knockbackEntities){
				return
			}
			
			val centerVec = position
			val doubleStrength = strength * 2.0
			
			val affectedArea = AxisAlignedBB(
				floor(centerX - doubleStrength - 1.0),
				floor(centerY - doubleStrength - 1.0),
				floor(centerZ - doubleStrength - 1.0),
				floor(centerX + doubleStrength + 1.0),
				floor(centerY + doubleStrength + 1.0),
				floor(centerZ + doubleStrength + 1.0)
			)
			
			val entitiesInArea = world.getEntitiesWithinAABBExcludingEntity(source, affectedArea)
			ForgeEventFactory.onExplosionDetonate(world, this, entitiesInArea, doubleStrength)
			
			for(entity in entitiesInArea){
				if (entity.isImmuneToExplosions){
					continue
				}
				
				if (entity is EntityPlayer){
					if (entity.isSpectator || (entity.isCreative && entity.capabilities.isFlying)){
						continue
					}
				}
				
				val distanceScaled = entity.getDistance(centerX, centerY, centerZ) / doubleStrength
				
				if (distanceScaled > 1){
					continue
				}
				
				val blastPower = (1 - distanceScaled) * world.getBlockDensity(centerVec, entity.entityBoundingBox)
				
				if (damageEntities){
					val finalDamage = 1F + ((square(blastPower) + blastPower) * strength * 7).toInt()
					entity.attackEntityFrom(DamageSource.causeExplosionDamage(this), finalDamage)
				}
				
				if (knockbackEntities){
					val knockbackPower = (entity as? EntityLivingBase)?.let { EnchantmentProtection.getBlastDamageReduction(it, blastPower) } ?: blastPower
					val knockbackVec = Vec3d(entity.posX, entity.posY + entity.eyeHeight, entity.posZ).subtract(centerVec).normalize().scale(knockbackPower)
					
					entity.motionX += knockbackVec.x
					entity.motionY += knockbackVec.y
					entity.motionZ += knockbackVec.z
					
					if (entity is EntityPlayer){
						playerKnockbackMap[entity] = knockbackVec // vanilla uses blastPower instead of knockbackPower here, bug?
					}
				}
			}
		}
		
		private fun destroyAffectedBlocks(){
			if (builder.destroyBlocks){
				val blockDropRate = builder.blockDropRate ?: 1F / strength
				val blockDropFortune = builder.blockDropFortune
				
				for(pos in affectedBlockPositions){
					val state = pos.getState(world)
					
					if (state.material !== Material.AIR){
						val block = state.block
						
						if (block.canDropFromExplosion(this)){
							block.dropBlockAsItemWithChance(world, pos, state, blockDropRate, blockDropFortune)
						}
						
						block.onBlockExploded(this.world, pos, this)
					}
				}
			}
			
			if (builder.spawnFire){
				val fireChance = builder.blockFireChance
				
				if (fireChance > 0){
					val candidates = if (builder.destroyBlocks) affectedBlockPositions.asSequence() else affectedBlockPositions.asSequence().map(BlockPos::up)
					
					for(pos in candidates){
						if (pos.isAir(world) && pos.down().getState(world).isFullBlock && rand.nextInt(fireChance) == 0){
							pos.setBlock(world, Blocks.FIRE)
						}
					}
				}
			}
		}
		
		// Overrides
		
		override fun doExplosionA(){
			determineAffectedBlocks()
			determineAffectedEntitiesAndKnockback()
		}
		
		override fun doExplosionB(spawnParticles: Boolean){
			SoundEvents.ENTITY_GENERIC_EXPLODE.playServer(world, centerX, centerY, centerZ, SoundCategory.BLOCKS, volume = 4F, pitch = rand.nextFloat(0.56F, 0.84F))
			destroyAffectedBlocks()
		}
	}
}
