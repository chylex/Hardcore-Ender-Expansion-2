package chylex.hee.system.util
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap.Type.WORLD_SURFACE
import net.minecraft.world.server.ServerWorld
import net.minecraft.world.storage.WorldSavedData

// Renaming

val World.totalTime
	get() = this.gameTime

val IWorld.isPeaceful
	get() = this.difficulty == PEACEFUL

// Porting

fun World.getTopSolidOrLiquidBlock(pos: BlockPos): BlockPos{
	return world.getHeight(WORLD_SURFACE, pos) // UPDATE test
}

// World data

fun <T : WorldSavedData> ServerWorld.perDimensionData(name: String, constructor: () -> T): T{
	return this.savedData.getOrCreate(constructor, name)
}
