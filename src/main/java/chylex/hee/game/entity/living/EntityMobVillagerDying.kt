package chylex.hee.game.entity.living
import chylex.hee.game.particle.ParticleGrowingSpot
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IOffset.MutableOffsetPoint
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.vanilla.EntityAgeable
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.migration.vanilla.EntityVillager
import chylex.hee.system.migration.vanilla.Sounds
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.playClient
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readTag
import chylex.hee.system.util.use
import chylex.hee.system.util.writeTag
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
	var villager: VillagerData? = null
		private set
	
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
		writeFloat(renderYawOffset)
		writeFloat(rotationYawHead)
		writeFloat(limbSwing)
	}
	
	override fun readSpawnData(buffer: PacketBuffer) = buffer.use {
		villager = VillagerData(Dynamic(NBTDynamicOps.INSTANCE, buffer.readTag()))
		
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
		if (world.isRemote && deathTime < 60){
			ParticleSpawnerCustom(
				type = ParticleGrowingSpot,
				data = ParticleGrowingSpot.Data(color = RGB(rand.nextInt(20).toUByte()), lifespan = 65 - deathTime),
				pos = DecayParticlePos
			).spawn(Point(this, heightMp = 0.5F, amount = 10), rand)
		}
		
		if (++deathTime == 65){
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
			
			Sounds.ENTITY_VILLAGER_DEATH.playClient(posVec, SoundCategory.HOSTILE, volume = 1.5F, pitch = 1.5F) // TODO new sound fx
		}
		
		super.remove()
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
