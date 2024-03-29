package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.ENERGY_SHRINE_GLOBAL
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectEntities
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.util.math.center
import net.minecraft.block.BlockState
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEntityReader

open class BlockGloomrock(builder: BlockBuilder) : HeeBlock(builder) {
	override fun canCreatureSpawn(state: BlockState, world: IBlockReader, pos: BlockPos, type: PlacementType?, entityType: EntityType<*>?): Boolean {
		if (world !is IEntityReader) {
			HEE.log.warn("[BlockGloomrock] attempted to check spawn on a world != IEntityReader (${world.javaClass})")
			return false
		}
		
		val center = pos.center
		val size = EnergyShrinePieces.STRUCTURE_SIZE
		
		val trigger = world
			.selectEntities
			.inBox<EntityTechnicalTrigger>(size.toCenteredBoundingBox(center))
			.find { it.triggerType == ENERGY_SHRINE_GLOBAL }
		
		return trigger == null || !size.toCenteredBoundingBox(trigger.posVec).contains(center)
	}
}
