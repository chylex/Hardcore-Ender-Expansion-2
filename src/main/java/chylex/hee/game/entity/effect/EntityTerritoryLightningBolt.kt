package chylex.hee.game.entity.effect

import chylex.hee.game.entity.IHeeEntityType
import chylex.hee.game.entity.properties.EntitySize
import chylex.hee.game.entity.properties.EntityTrackerInfo
import chylex.hee.game.fx.util.playPlayer
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.init.ModEntities
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.random.nextFloat
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.network.IPacket
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvents
import net.minecraft.world.World
import net.minecraftforge.fml.network.NetworkHooks

class EntityTerritoryLightningBolt(type: EntityType<*>, world: World) : Entity(type, world) {
	constructor(world: World, x: Double, y: Double, z: Double) : this(ModEntities.TERRITORY_LIGHTNING_BOLT, world) {
		setLocationAndAngles(x, y, z, 0F, 0F)
	}
	
	object Type : IHeeEntityType<EntityTerritoryLightningBolt> {
		override val size
			get() = EntitySize(0F)
		
		override val tracker
			get() = EntityTrackerInfo(trackingRange = 16, updateInterval = 3, receiveVelocityUpdates = false)
		
		override val disableSerialization
			get() = true
	}
	
	private var lightningState = 2
	
	var boltVertex = rand.nextLong()
		private set
	
	private var boltLivingTime = rand.nextInt(3) + 1
	
	init {
		ignoreFrustumCheck = true
	}
	
	override fun registerData() {}
	
	override fun createSpawnPacket(): IPacket<*> {
		return NetworkHooks.getEntitySpawningPacket(this)
	}
	
	override fun tick() {
		super.tick()
		
		if (lightningState == 2 && !world.isRemote) {
			TerritoryInstance.fromPos(this)?.players?.forEach {
				SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER.playPlayer(it, posX, posY, posZ, SoundCategory.WEATHER, volume = 10000F, pitch = rand.nextFloat(0.8F, 1F))
				SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT.playPlayer(it, posX, posY, posZ, SoundCategory.WEATHER, volume = 2F, pitch = rand.nextFloat(0.5F, 0.7F))
			}
		}
		
		--lightningState
		
		if (lightningState < 0) {
			if (boltLivingTime == 0) {
				remove()
			}
			else if (lightningState < -rand.nextInt(10)) {
				--boltLivingTime
				lightningState = 1
				boltVertex = rand.nextLong()
			}
		}
		
		if (lightningState >= 0 && world.isRemote) {
			world.setTimeLightningFlash(2)
		}
	}
	
	override fun getSoundCategory(): SoundCategory {
		return SoundCategory.WEATHER
	}
	
	@Sided(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean {
		return true
	}
	
	override fun writeAdditional(nbt: TagCompound) {}
	override fun readAdditional(nbt: TagCompound) {}
}
