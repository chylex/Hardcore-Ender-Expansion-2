package chylex.hee.game.block

import chylex.hee.HEE
import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.ICreatureSpawningOnBlockComponent
import chylex.hee.game.block.properties.BlockHardness
import chylex.hee.game.block.properties.BlockHarvestTool
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.ENERGY_SHRINE_GLOBAL
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.entity.util.selectEntities
import chylex.hee.game.item.util.Tool.Level.WOOD
import chylex.hee.game.item.util.Tool.Type.PICKAXE
import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrinePieces
import chylex.hee.init.ModTags
import chylex.hee.util.forge.EventResult
import chylex.hee.util.math.center
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.IEntityReader

object BlockGloomrock : HeeBlockBuilder() {
	init {
		material = Materials.SOLID
		color = MaterialColor.BLACK
		sound = SoundType.STONE
		
		tool = BlockHarvestTool.required(WOOD, PICKAXE)
		hardness = BlockHardness(hardness = 1.6F, resistance = 4.2F)
		
		tags.add(ModTags.GLOOMROCK_PARTICLES)
		
		components.onCreatureSpawning = object : ICreatureSpawningOnBlockComponent {
			override fun canSpawn(world: IBlockReader, pos: BlockPos, placementType: PlacementType?, entityType: EntityType<*>?): EventResult {
				if (world !is IEntityReader) {
					HEE.log.warn("[BlockGloomrock] attempted to check spawn on a world != IEntityReader (${world.javaClass})")
					return EventResult.DENY
				}
				
				val center = pos.center
				val size = EnergyShrinePieces.STRUCTURE_SIZE
				
				val trigger = world
					.selectEntities
					.inBox<EntityTechnicalTrigger>(size.toCenteredBoundingBox(center))
					.find { it.triggerType == ENERGY_SHRINE_GLOBAL }
				
				return EventResult(trigger == null || !size.toCenteredBoundingBox(trigger.posVec).contains(center))
			}
		}
	}
	
	val BRICKS = HeeBlockBuilder {
		includeFrom(BlockGloomrock)
		hardness = BlockHardness(hardness = 2.8F, resistance = 6F)
	}
	
	val SMOOTH = HeeBlockBuilder {
		includeFrom(BlockGloomrock)
		localization = LocalizationStrategy.MoveToBeginning(wordCount = 1)
		hardness = BlockHardness(hardness = 2F, resistance = 4.8F)
	}
	
	val SMOOTH_COLORED = HeeBlockBuilder {
		includeFrom(SMOOTH)
		localization = LocalizationStrategy.MoveToBeginning(LocalizationStrategy.DeleteWords("Smooth"), wordCount = 1)
	}
}
