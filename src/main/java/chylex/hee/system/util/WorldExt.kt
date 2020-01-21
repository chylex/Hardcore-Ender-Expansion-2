package chylex.hee.system.util
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData

// Renaming

val World.totalTime
	get() = this.gameTime

val IWorld.isPeaceful
	get() = this.difficulty == PEACEFUL

// World data

fun <T : WorldSavedData> ServerWorld.perDimensionData(name: String, constructor: () -> T): T{
	return this.savedData.getOrCreate(constructor, name)
}
