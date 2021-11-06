package chylex.hee.game.block.properties

import net.minecraft.block.AbstractBlock.Properties

data class BlockHardness(val hardness: Float, val resistance: Float) {
	constructor(hardnessAndResistance: Float) : this(hardnessAndResistance, hardnessAndResistance)
	
	fun applyTo(properties: Properties): Properties {
		return properties.hardnessAndResistance(hardness, resistance)
	}
}
