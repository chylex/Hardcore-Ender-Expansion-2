package chylex.hee.game.entity.living.behavior
import chylex.hee.game.entity.living.EntityBossEnderEye
import chylex.hee.game.entity.living.behavior.EnderEyeAttack.Melee
import chylex.hee.system.util.directionTowards
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.lookPosVec
import chylex.hee.system.util.math.Quaternion
import chylex.hee.system.util.playServer
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.toPitch
import chylex.hee.system.util.toYaw
import net.minecraft.init.SoundEvents
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.SoundCategory
import net.minecraftforge.common.util.INBTSerializable
import kotlin.math.min

sealed class EnderEyePhase : INBTSerializable<NBTTagCompound>{
	open fun tick(entity: EntityBossEnderEye) = this
	
	override fun serializeNBT() = NBTTagCompound()
	override fun deserializeNBT(nbt: NBTTagCompound){}
	
	object Hibernated : EnderEyePhase()
	
	class OpenEye : EnderEyePhase(){
		private var timer: Byte = 35
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			--timer
			
			if (timer == 15.toByte()){
				for(nearby in entity.world.selectExistingEntities.allInRange(entity.posVec, 8.0)){
					if (nearby !== entity){
						entity.performBlastKnockback(nearby, 0.75F)
					}
				}
				
				SoundEvents.ENTITY_GENERIC_EXPLODE.playServer(entity.world, entity.posVec, SoundCategory.HOSTILE, volume = 0.5F) // TODO knockback fx
			}
			else if (timer < 0){
				return Floating(100) // TODO
			}
			
			return this
		}
		
		override fun serializeNBT() = NBTTagCompound().apply {
			setByte("Timer", timer)
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			timer = getByte("Timer")
		}
	}
	
	class Floating(remainingSpawnerPercentage: Int) : EnderEyePhase(){
		private var animatedSpawnerPercentage = -15F
		private var targetSpawnerPercentage = remainingSpawnerPercentage.toFloat()
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			if (animatedSpawnerPercentage < targetSpawnerPercentage){
				animatedSpawnerPercentage = min(animatedSpawnerPercentage + 0.55F, targetSpawnerPercentage)
			}
			
			val currentPercentage = animatedSpawnerPercentage.floorToInt().coerceAtLeast(0)
			val currentPercentageFloat = currentPercentage / 100F
			
			entity.motionY = 0.015 - (currentPercentageFloat * 0.013)
			entity.health = 150F + min(150F, 150F * 1.06F * currentPercentageFloat)
			
			val prevDemonLevel = entity.demonLevel
			val newDemonLevel = when(currentPercentage){
				100 -> 5 // TODO transform into demon eye
				in 78..99 -> 5
				in 57..77 -> 4
				in 37..56 -> 3
				in 18..36 -> 2
				in  1..17 -> 1
				else      -> 0
			}.toByte()
			
			if (newDemonLevel != prevDemonLevel){
				entity.updateDemonLevel(newDemonLevel)
				SoundEvents.BLOCK_ANVIL_PLACE.playServer(entity.world, entity.posVec, SoundCategory.HOSTILE, volume = 0.3F, pitch = 0.5F + (newDemonLevel * 0.15F))
				// TODO custom sound
			}
			
			if (animatedSpawnerPercentage >= targetSpawnerPercentage){
				entity.motionY = 0.0
				return Staring()
			}
			
			return this
		}
		
		override fun serializeNBT() = NBTTagCompound().apply {
			setFloat("Current", animatedSpawnerPercentage)
			setFloat("Target", targetSpawnerPercentage)
		}
		
		override fun deserializeNBT(nbt: NBTTagCompound) = with(nbt){
			animatedSpawnerPercentage = getFloat("Current")
			targetSpawnerPercentage = getFloat("Target")
		}
	}
	
	class Staring : EnderEyePhase(){
		private var timer = 55
		private var slerpProgress = 0F
		
		private lateinit var startRot: Quaternion
		private lateinit var targetRot: Quaternion
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			val target = entity.attackTarget ?: entity.forceFindNewTarget()
			
			if (target == null || --timer < 0){
				return Ready()
			}
			
			if (!::startRot.isInitialized){
				startRot = Quaternion.fromYawPitch(entity.rotationYaw, entity.rotationPitch)
			}
			
			if (timer < 50){
				targetRot = entity.lookPosVec.directionTowards(target.lookPosVec).let { Quaternion.fromYawPitch(it.toYaw(), it.toPitch()) }
				
				if (slerpProgress < 1F){
					slerpProgress += 0.025F
				}
				
				val next = startRot.slerp(targetRot, slerpProgress)
				entity.rotationYaw = next.rotationYaw
				entity.rotationPitch = next.rotationPitch
			}
			
			return this
		}
	}
	
	class Ready : EnderEyePhase(){
		private val defaultAttack = Melee()
		
		var currentAttack: EnderEyeAttack = defaultAttack
		
		override fun tick(entity: EntityBossEnderEye): EnderEyePhase{
			if (entity.isSleeping || !currentAttack.tick(entity)){
				currentAttack = defaultAttack
			}
			
			return this
		}
	}
}
