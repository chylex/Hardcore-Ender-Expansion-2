package chylex.hee.game.item.util
import chylex.hee.game.particle.ParticleTeleport
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Line
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.client.PacketClientFX.IFXData
import chylex.hee.network.client.PacketClientFX.IFXHandler
import chylex.hee.system.util.add
import chylex.hee.system.util.center
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.offsetTowards
import chylex.hee.system.util.posVec
import chylex.hee.system.util.readCompactVec
import chylex.hee.system.util.readString
import chylex.hee.system.util.use
import chylex.hee.system.util.writeCompactVec
import chylex.hee.system.util.writeString
import io.netty.buffer.ByteBuf
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.SoundEvents
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import java.util.Random

class Teleporter(
	private val resetFall: Boolean,
	private val damageDealt: Float = 0F
){
	companion object{
		@JvmStatic
		val PARTICLE_TELEPORT = ParticleSpawnerCustom(
			type = ParticleTeleport,
			pos = InBox(0.2F),
			mot = InBox(0.035F),
			hideOnMinimalSetting = false
		)
		
		class FxTeleportData(private val startPoint: Vec3d, private val endPoint: Vec3d, private val soundCategory: SoundCategory, private val soundVolume: Float) : IFXData{
			override fun write(buffer: ByteBuf) = buffer.use {
				writeCompactVec(startPoint)
				writeCompactVec(endPoint)
				writeString(soundCategory.getName())
				writeByte((soundVolume * 10F).floorToInt())
			}
		}
		
		@JvmStatic
		val FX_TELEPORT = object : IFXHandler{
			override fun handle(buffer: ByteBuf, world: World, rand: Random) = buffer.use {
				val startPoint = readCompactVec()
				val endPoint = readCompactVec()
				
				val soundCategory = SoundCategory.getByName(readString())
				val soundVolume = readByte() / 10F
				
				PARTICLE_TELEPORT.spawn(Line(startPoint, endPoint, 0.5), rand)
				
				world.playSound(startPoint.x, startPoint.y, startPoint.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, soundCategory, soundVolume, 1F, false)
				world.playSound(endPoint.x, endPoint.y, endPoint.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, soundCategory, soundVolume, 1F, false)
			}
		}
		
		fun sendTeleportFX(world: World, startPoint: Vec3d, endPoint: Vec3d, soundCategory: SoundCategory, soundVolume: Float = 1F){
			val middlePoint = startPoint.offsetTowards(endPoint, 0.5)
			val traveledDistance = startPoint.distanceTo(endPoint)
			
			PacketClientFX(FX_TELEPORT, FxTeleportData(startPoint, endPoint, soundCategory, soundVolume)).sendToAllAround(world, middlePoint, (traveledDistance * 0.5) + 32.0)
		}
	}
	
	fun toBlock(entity: EntityLivingBase, position: BlockPos, soundCategory: SoundCategory): Boolean{
		return toLocation(entity, position.center.add(0.0, -0.49, 0.0), soundCategory)
	}
	
	fun toLocation(entity: EntityLivingBase, position: Vec3d, soundCategory: SoundCategory): Boolean{
		val event = EnderTeleportEvent(entity, position.x, position.y, position.z, damageDealt)
		
		if (MinecraftForge.EVENT_BUS.post(event)){
			return false
		}
		
		if (entity.isRiding){
			entity.dismountRidingEntity()
		}
		
		if (entity.isPlayerSleeping && entity is EntityPlayer){
			entity.wakeUpPlayer(true, true, false)
		}
		
		sendTeleportFX(entity.world, entity.posVec, Vec3d(event.targetX, event.targetY, event.targetZ), soundCategory)
		entity.setPositionAndUpdate(event.targetX, event.targetY, event.targetZ)
		
		if (resetFall){
			entity.fallDistance = 0F
		}
		
		return true
	}
}
