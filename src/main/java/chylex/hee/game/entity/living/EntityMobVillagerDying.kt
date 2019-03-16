package chylex.hee.game.entity.living
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.common.registry.VillagerRegistry
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession
import java.util.Random

class EntityMobVillagerDying(world: World) : EntityAgeable(world), IEntityAdditionalSpawnData{
	private object DecayParticlePos : IOffset{
		private const val halfSize = 0.25F
		
		override fun next(out: MutableOffsetPoint, rand: Random){
			if (rand.nextInt(5) == 0){
				out.x = rand.nextFloat(-halfSize, halfSize)
				out.y = rand.nextFloat(1F, 1.25F) * (if (rand.nextBoolean()) -1 else 1)
				out.z = rand.nextFloat(-halfSize, halfSize)
			}
			else{
				val facing = rand.nextItem(Facing4)
				
				val offsetFull = halfSize + rand.nextFloat(0F, 0.3F)
				val offsetPerpendicular = rand.nextFloat(-halfSize, halfSize)
				
				out.x = (facing.xOffset * offsetFull) + (facing.zOffset * offsetPerpendicular)
				out.y = rand.nextFloat(-1.1F, 1.1F)
				out.z = (facing.zOffset * offsetFull) + (facing.xOffset * offsetPerpendicular)
			}
		}
	}
	
	var profession: VillagerProfession? = null
		private set
	
	init{
		setSize(0.6F, 1.95F)
		setEntityInvulnerable(true)
		setNoGravity(true)
	}
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		experienceValue = 0
	}
	
	fun copyVillagerDataFrom(villager: EntityVillager){
		setGrowingAge(villager.growingAge)
		profession = villager.professionForge
		
		renderYawOffset = villager.renderYawOffset
		rotationYawHead = villager.rotationYawHead
		limbSwing = villager.limbSwing
	}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeInt(VillagerRegistry.getId(profession))
		
		writeFloat(renderYawOffset)
		writeFloat(rotationYawHead)
		writeFloat(limbSwing)
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		profession = VillagerRegistry.getById(readInt()) ?: VillagerRegistry.FARMER
		
		renderYawOffset = readFloat()
		rotationYawHead = readFloat()
		limbSwing = readFloat()
		
		prevRenderYawOffset = renderYawOffset
		prevRotationYawHead = rotationYawHead
	}
	
	override fun onUpdate(){
		firstUpdate = false
		onDeathUpdate()
	}
	
	override fun onDeathUpdate(){
		if (world.isRemote && deathTime < 60){
			ParticleSpawnerCustom(
				type = ParticleGrowingSpot,
				data = ParticleGrowingSpot.Data(color = RGB(rand.nextInt(20).toUByte()), lifespan = 65 - deathTime),
				pos = DecayParticlePos
			).spawn(Point(this, heightMp = 0.5F, amount = 10), rand)
		}
		
		if (++deathTime == 65){
			setDead()
		}
	}
	
	override fun setDead(){
		if (world.isRemote && !isDead){
			ParticleSpawnerCustom(
				type = ParticleSmokeCustom,
				data = ParticleSmokeCustom.Data(scale = 1.66F),
				pos = InBox(this, 0.25F)
			).spawn(Point(this, heightMp = 0.5F, amount = 100), rand)
			
			SoundEvents.ENTITY_VILLAGER_DEATH.playClient(posVec, SoundCategory.HOSTILE, volume = 1.5F, pitch = 1.5F) // TODO new sound fx
		}
		
		super.setDead()
	}
	
	override fun processInteract(player: EntityPlayer, hand: EnumHand) = true
	override fun canBeLeashedTo(player: EntityPlayer) = false
	
	override fun createChild(ageable: EntityAgeable): EntityAgeable? = null
	override fun ageUp(growthSeconds: Int, updateForcedAge: Boolean){}
	
	override fun attackable() = false
	override fun canBeCollidedWith() = false
	override fun canBeHitWithPotion() = false
	override fun canDespawn() = false
}
