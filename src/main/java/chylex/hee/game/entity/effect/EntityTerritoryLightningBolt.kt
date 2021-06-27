package chylex.hee.game.entity.effect

import chylex.hee.game.world.playPlayer
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.init.ModEntities
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.random.nextFloat
import chylex.hee.system.serialization.TagCompound
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
	
	fun spawnInTerritory() {
		world.addEntity(this)
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
