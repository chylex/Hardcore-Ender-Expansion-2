package chylex.hee.game.world
import chylex.hee.HEE
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.EntityPlayerMP
import net.minecraft.network.play.server.SPlaySoundEffectPacket
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

// Client

fun SoundEvent.playClient(x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false){
	HEE.proxy.getClientSidePlayer()?.world?.playSound(x, y, z, this, category, volume, pitch, distanceDelay)
}

fun SoundEvent.playClient(pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) =
	this.playClient(pos.x, pos.y, pos.z, category, volume, pitch, distanceDelay)

fun SoundEvent.playClient(pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) =
	this.playClient(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch, distanceDelay)

// Server (Auto)

fun SoundEvent.playServer(world: World, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F){
	world.playSound(null, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playServer(world: World, pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	this.playServer(world, pos.x, pos.y, pos.z, category, volume, pitch)

fun SoundEvent.playServer(world: World, pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	this.playServer(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch)

// Server (Concrete)

fun SoundEvent.playPlayer(player: EntityPlayer, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F){
	(player as EntityPlayerMP).connection.sendPacket(SPlaySoundEffectPacket(this, category, x, y, z, volume, pitch))
}

// Universal (client plays the sound, server sends packets to all clients but the specified player)

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F){
	clientPlayer.world.playSound(clientPlayer, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	clientPlayer.world.playSound(clientPlayer, pos.x, pos.y, pos.z, this, category, volume, pitch)

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	clientPlayer.world.playSound(clientPlayer, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, this, category, volume, pitch)
