package chylex.hee.game.fx.util

import chylex.hee.client.util.MC
import chylex.hee.util.forge.Side
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.play.server.SPlaySoundEffectPacket
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3i
import net.minecraft.world.World
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.DistExecutor.SafeCallable

// Client

fun SoundEvent.playClient(x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) {
	DistExecutor.safeCallWhenOn(Side.CLIENT) {
		SafeCallable {
			MC.world?.playSound(x, y, z, this, category, volume, pitch, distanceDelay)
		}
	}
}

fun SoundEvent.playClient(pos: Vector3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) {
	this.playClient(pos.x, pos.y, pos.z, category, volume, pitch, distanceDelay)
}

fun SoundEvent.playClient(pos: Vector3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F, distanceDelay: Boolean = false) {
	this.playClient(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch, distanceDelay)
}

// Server (Auto)

fun SoundEvent.playServer(world: World, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	world.playSound(null, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playServer(world: World, pos: Vector3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	this.playServer(world, pos.x, pos.y, pos.z, category, volume, pitch)
}

fun SoundEvent.playServer(world: World, pos: Vector3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	this.playServer(world, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, category, volume, pitch)
}

// Server (Concrete)

fun SoundEvent.playPlayer(player: PlayerEntity, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	(player as ServerPlayerEntity).connection.sendPacket(SPlaySoundEffectPacket(this, category, x, y, z, volume, pitch))
}

fun SoundEvent.playPlayer(player: PlayerEntity, pos: Vector3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	this.playPlayer(player, pos.x, pos.y, pos.z, category, volume, pitch)
}

// Universal (Client plays the sound, server sends packets to all clients but the specified player)

fun SoundEvent.playUniversal(clientPlayer: PlayerEntity, x: Double, y: Double, z: Double, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	clientPlayer.world.playSound(clientPlayer, x, y, z, this, category, volume, pitch)
}

fun SoundEvent.playUniversal(clientPlayer: PlayerEntity, pos: Vector3d, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	clientPlayer.world.playSound(clientPlayer, pos.x, pos.y, pos.z, this, category, volume, pitch)
}

fun SoundEvent.playUniversal(clientPlayer: PlayerEntity, pos: Vector3i, category: SoundCategory, volume: Float = 1F, pitch: Float = 1F) {
	clientPlayer.world.playSound(clientPlayer, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, this, category, volume, pitch)
}
