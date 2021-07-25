package chylex.hee.game.entity.properties

import net.minecraft.entity.EntitySpawnPlacementRegistry.IPlacementPredicate
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.MobEntity
import net.minecraft.entity.monster.MonsterEntity
import net.minecraft.world.gen.Heightmap

data class EntitySpawnPlacement<T : MobEntity>(val placementType: PlacementType, val heightmapType: Heightmap.Type, val predicate: IPlacementPredicate<T>) {
	companion object {
		fun <T : MobEntity> passive()     = EntitySpawnPlacement<T>(PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MobEntity::canSpawnOn)
		fun <T : MonsterEntity> hostile() = EntitySpawnPlacement<T>(PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::canMonsterSpawnInLight)
	}
}
