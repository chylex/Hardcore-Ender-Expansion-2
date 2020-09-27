package chylex.hee.game.entity.living
import chylex.hee.game.entity.posVec
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.playClient
import chylex.hee.init.ModEntities
import chylex.hee.init.ModSounds
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Facing4
import chylex.hee.system.migration.EntityAgeable
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityVillager
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextItem
import chylex.hee.system.serialization.TagCompound
import chylex.hee.system.serialization.heeTag
import chylex.hee.system.serialization.readTag
import chylex.hee.system.serialization.use
import chylex.hee.system.serialization.writeTag
import com.mojang.datafixers.Dynamic
import net.minecraft.entity.EntityType
import net.minecraft.entity.merchant.villager.VillagerData
import net.minecraft.entity.villager.IVillagerDataHolder
import net.minecraft.nbt.NBTDynamicOps
import net.minecraft.network.IPacket
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Hand
import net.minecraft.util.SoundCategory
import net.minecraft.world.World
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData
import net.minecraftforge.fml.network.NetworkHooks
import java.util.Random

class EntityMobVillagerDying(type: EntityType<EntityMobVillagerDying>, world: World) : EntityAgeable(type, world), IVillagerDataHolder, IEntityAdditionalSpawnData{
	constructor(world: World) : this(ModEntities.VILLAGER_DYING, world)
	
	private companion object{
		private const val VILLAGER_TAG = "Villager"
		
		private val DECAY_ADULT = DecayParticlePos(halfSize = 0.3F, heightMp = 1F)
		private val DECAY_CHILD = DecayParticlePos(halfSize = 0.3F, heightMp = 0.5F)
	}
	
	private class DecayParticlePos(private val halfSize: Float, private val heightMp: Float) : IOffset{
		override fun next(out: MutableOffsetPoint, rand: Random){
			if (rand.nextInt(5) == 0){
				out.x = rand.nextFloat(-halfSize, halfSize)
				out.y = rand.nextFloat(1F, 1.25F) * heightMp * (if (rand.nextBoolean()) -1 else 1)
				out.z = rand.nextFloat(-halfSize, halfSize)
			}
			else{
				val facing = rand.nextItem(Facing4)
				
				val offsetFull = halfSize + rand.nextFloat(0F, 0.3F)
				val offsetPerpendicular = rand.nextFloat(-halfSize, halfSize)
				
				out.x = (facing.xOffset * offsetFull) + (facing.zOffset * offsetPerpendicular)
				out.y = rand.nextFloat(-1.1F, 1.1F) * heightMp
				out.z = (facing.zOffset * offsetFull) + (facing.xOffset * offsetPerpendicular)
			}
		}
	}
	
	private var villager: VillagerData? = null
	
	init{
		isInvulnerable = true
		setNoGravity(true)
	}
	
	override fun registerAttributes(){
		super.registerAttributes()
		
		experienceValue = 0
	}
	
	fun copyVillagerDataFrom(villager: EntityVillager){
		setGrowingAge(villager.growingAge)
		this.villager = villager.villagerData
		
		renderYawOffset = villager.renderYawOffset
		rotationYawHead = villager.rotationYawHead
		limbSwing = villager.limbSwing
	}
	
	override fun getVillagerData(): VillagerData{
		return villager!!
	}
	
	override fun createSpawnPacket(): IPacket<*>{
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun writeSpawnData(buffer: PacketBuffer) = buffer.use {
		writeTag(villagerData.serialize(NBTDynamicOps.INSTANCE) as TagCompound)
		writeVarInt(deathTime)
		
		writeFloat(renderYawOffset)
		writeFloat(rotationYawHead)
		writeFloat(limbSwing)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		villager = VillagerData(Dynamic(NBTDynamicOps.INSTANCE, buffer.readTag()))
		deathTime = readVarInt()
		
		renderYawOffset = readFloat()
		rotationYawHead = readFloat()
		limbSwing = readFloat()
		
		prevRenderYawOffset = renderYawOffset
		prevRotationYawHead = rotationYawHead
	}
	
	override fun tick(){
		firstUpdate = false
		onDeathUpdate()
	}
	
	override fun onDeathUpdate(){
		if (world.isRemote && deathTime < 66){
			if (deathTime == 0){
				ModSounds.MOB_VILLAGER_TOTEM_DYING.playClient(posVec, SoundCategory.HOSTILE, volume = 1.25F)
			}
			
			val isChild = isChild
			
			ParticleSpawnerCustom(
				type = ParticleGrowingSpot,
				data = ParticleGrowingSpot.Data(color = RGB(rand.nextInt(20).toUByte()), lifespan = 71 - deathTime),
				pos = if (isChild) DECAY_CHILD else DECAY_ADULT
			).spawn(Point(this, heightMp = 0.5F, amount = if (isChild) 4 else 12), rand)
		}
		
		if (++deathTime == 71){
			remove()
		}
	}
	
	override fun remove(){
		if (world.isRemote && isAlive){
			ParticleSpawnerCustom(
				type = ParticleSmokeCustom,
				data = ParticleSmokeCustom.Data(scale = 1.66F),
				pos = InBox(this, 0.25F)
			).spawn(Point(this, heightMp = 0.5F, amount = 100), rand)
			
			ModSounds.MOB_VILLAGER_TOTEM_DEATH.playClient(posVec, SoundCategory.HOSTILE, volume = 2F, pitch = if (isChild) 1.6F else 1.1F)
		}
		
		super.remove()
	}
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		put(VILLAGER_TAG, villagerData.serialize(NBTDynamicOps.INSTANCE))
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		villager = VillagerData(Dynamic(NBTDynamicOps.INSTANCE, getCompound(VILLAGER_TAG)))
	}
	
	override fun processInteract(player: EntityPlayer, hand: Hand) = true
	override fun canBeLeashedTo(player: EntityPlayer) = false
	
	override fun createChild(ageable: EntityAgeable): EntityAgeable? = null
	override fun ageUp(growthSeconds: Int, updateForcedAge: Boolean){}
	
	override fun attackable() = false
	override fun canBeCollidedWith() = false
	override fun canBeHitWithPotion() = false
	
	override fun canDespawn(distanceToClosestPlayerSq: Double) = false
	override fun preventDespawn() = true
}
