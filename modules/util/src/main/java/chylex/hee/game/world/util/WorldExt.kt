package chylex.hee.game.world.util

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData

// Difficulty

val IWorld.isPeaceful
	get() = this.difficulty == PEACEFUL

// World data

fun <T : WorldSavedData> ServerWorld.perDimensionData(name: String, constructor: () -> T): T {
	return this.savedData.getOrCreate(constructor, name)
}

// Entity spawning

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, setup: T.() -> Unit) {
	entityType.create(this)!!.apply(setup).let(this::addEntity)
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, x: Double, y: Double, z: Double, yaw: Float, pitch: Float, setup: T.() -> Unit) {
	this.spawn(entityType) { setLocationAndAngles(x, y, z, yaw, pitch); setup(this) }
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, x: Double, y: Double, z: Double, yaw: Float, setup: T.() -> Unit) {
	this.spawn(entityType, x, y, z, yaw, pitch = 0F, setup)
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, x: Double, y: Double, z: Double, setup: T.() -> Unit) {
	this.spawn(entityType, x, y, z, yaw = 0F, pitch = 0F, setup)
}

fun <T : Entity> World.spawn(entityType: EntityType<T>, x: Double, y: Double, z: Double, yaw: Float = 0F, pitch: Float = 0F) {
	this.spawn(entityType) { setLocationAndAngles(x, y, z, yaw, pitch) }
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, pos: Vector3d, yaw: Float, pitch: Float, setup: T.() -> Unit) {
	this.spawn(entityType, pos.x, pos.y, pos.z, yaw, pitch, setup)
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, pos: Vector3d, yaw: Float, setup: T.() -> Unit) {
	this.spawn(entityType, pos.x, pos.y, pos.z, yaw, pitch = 0F, setup)
}

inline fun <T : Entity> World.spawn(entityType: EntityType<T>, pos: Vector3d, setup: T.() -> Unit) {
	this.spawn(entityType, pos.x, pos.y, pos.z, yaw = 0F, pitch = 0F, setup)
}

fun <T : Entity> World.spawn(entityType: EntityType<T>, pos: Vector3d, yaw: Float = 0F, pitch: Float = 0F) {
	this.spawn(entityType, pos.x, pos.y, pos.z, yaw, pitch)
}
