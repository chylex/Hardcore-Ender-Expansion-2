package chylex.hee.game.block.builder

import chylex.hee.game.block.components.IBlockAddedComponent
import chylex.hee.game.block.components.IBlockClientEffectsComponent
import chylex.hee.game.block.components.IBlockCollideWithEntityComponent
import chylex.hee.game.block.components.IBlockDropsComponent
import chylex.hee.game.block.components.IBlockEntityComponent
import chylex.hee.game.block.components.IBlockExperienceComponent
import chylex.hee.game.block.components.IBlockExplodedComponent
import chylex.hee.game.block.components.IBlockHarvestabilityComponent
import chylex.hee.game.block.components.IBlockNameComponent
import chylex.hee.game.block.components.IBlockNeighborChanged
import chylex.hee.game.block.components.IBlockPlacementComponent
import chylex.hee.game.block.components.IBlockRandomTickComponent
import chylex.hee.game.block.components.IBlockScheduledTickComponent
import chylex.hee.game.block.components.IBlockShapeComponent
import chylex.hee.game.block.components.ICreatureSpawningOnBlockComponent
import chylex.hee.game.block.components.IFlammableBlockComponent
import chylex.hee.game.block.components.IPlayerUseBlockComponent
import chylex.hee.game.block.components.ISetBlockStateFromNeighbor
import chylex.hee.game.item.components.ITooltipComponent
import net.minecraft.block.BlockRenderType

class HeeBlockComponents {
	val states = HeeBlockStates.Builder()
	
	var name: IBlockNameComponent? = null
	var tooltip: ITooltipComponent? = null
	
	var shape: IBlockShapeComponent? = null
	var renderType: BlockRenderType? = null
	var ambientOcclusionValue: Float? = null
	var clientEffects: IBlockClientEffectsComponent? = null
	
	var drops: IBlockDropsComponent? = null
	var harvestability: IBlockHarvestabilityComponent? = null
	var experience: IBlockExperienceComponent? = null
	var flammability: IFlammableBlockComponent? = null
	
	var entity: IBlockEntityComponent? = null
	var placement: IBlockPlacementComponent? = null
	var onAdded: IBlockAddedComponent? = null
	var onNeighborChanged: IBlockNeighborChanged? = null
	var setStateFromNeighbor: ISetBlockStateFromNeighbor? = null
	
	var scheduledTick: IBlockScheduledTickComponent? = null
	var randomTick: IBlockRandomTickComponent? = null
	
	var playerUse: IPlayerUseBlockComponent? = null
	var onExploded: IBlockExplodedComponent? = null
	var onCreatureSpawning: ICreatureSpawningOnBlockComponent? = null
	var collideWithEntity: IBlockCollideWithEntityComponent? = null
	
	var isAir: Boolean? = null
	
	fun includeFrom(source: HeeBlockComponents) {
		source.name?.let { this.name = it }
		source.tooltip?.let { this.tooltip = it }
		
		source.shape?.let { this.shape = it }
		source.renderType?.let { this.renderType = it }
		source.ambientOcclusionValue?.let { this.ambientOcclusionValue = it }
		source.clientEffects?.let { this.clientEffects = it }
		
		source.drops?.let { this.drops = it }
		source.harvestability?.let { this.harvestability = it }
		source.experience?.let { this.experience = it }
		source.flammability?.let { this.flammability = it }
		
		source.entity?.let { this.entity = it }
		source.placement?.let { this.placement = it }
		source.onAdded?.let { this.onAdded = it }
		source.onNeighborChanged?.let { this.onNeighborChanged = it }
		source.setStateFromNeighbor?.let { this.setStateFromNeighbor = it }
		
		source.scheduledTick?.let { this.scheduledTick = it }
		source.randomTick?.let { this.randomTick = it }
		
		source.playerUse?.let { this.playerUse = it }
		source.onExploded?.let { this.onExploded = it }
		source.onCreatureSpawning?.let { this.onCreatureSpawning = it }
		source.collideWithEntity?.let { this.collideWithEntity = it }
		
		source.isAir?.let { this.isAir = it }
	}
}
