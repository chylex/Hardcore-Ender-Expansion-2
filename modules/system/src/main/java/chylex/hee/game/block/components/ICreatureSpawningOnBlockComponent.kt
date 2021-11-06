package chylex.hee.game.block.components

import chylex.hee.util.forge.EventResult
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader

fun interface ICreatureSpawningOnBlockComponent {
	fun canSpawn(world: IBlockReader, pos: BlockPos, placementType: PlacementType?, entityType: EntityType<*>?): EventResult
}
