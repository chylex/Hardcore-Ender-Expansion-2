package chylex.hee.system.util
import chylex.hee.HEE
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World

// Client

fun SoundEvent.playClient(x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false){
	HEE.proxy.getClientSidePlayer()?.world?.let {
		it.playSound(x, y, z, this, category, volume, pitch, distanceDelay)
	}
}

fun SoundEvent.playClient(pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) =
	this.playClient(pos.x, pos.y, pos.z, category, volume, pitch, distanceDelay)

fun SoundEvent.playClient(pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) =
	this.playClient(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch, distanceDelay)

// Server

fun SoundEvent.playServer(world: World, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F){
	world.playSound(null, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playServer(world: World, pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	this.playServer(world, pos.x, pos.y, pos.z, category, volume, pitch)

fun SoundEvent.playServer(world: World, pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	this.playServer(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch)

// Universal (client plays the sound, server sends packets to all clients but the specified player)

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F){
	clientPlayer.world.playSound(clientPlayer, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, pos: Vec3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	clientPlayer.world.playSound(clientPlayer, pos.x, pos.y, pos.z, this, category, volume, pitch)

fun SoundEvent.playUniversal(clientPlayer: EntityPlayer, pos: Vec3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) =
	clientPlayer.world.playSound(clientPlayer, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, this, category, volume, pitch)
