package chylex.hee.game.block.properties

import net.minecraft.block.AbstractBlock.Properties
import net.minecraftforge.common.ToolType

@Suppress("DataClassPrivateConstructor")
data class BlockHarvestTool private constructor(val tier: Int, val toolType: ToolType?, val requiresTool: Boolean) {
	fun applyTo(properties: Properties): Properties {
		return properties
			.harvestLevel(tier)
			.let { if (toolType != null) it.harvestTool(toolType) else it }
			.let { if (requiresTool) it.setRequiresTool() else it }
	}
	
	companion object {
		val NONE = BlockHarvestTool(-1, null, requiresTool = false)
		
		fun required(tier: Int, toolType: ToolType) = BlockHarvestTool(tier, toolType, requiresTool = true)
		fun optional(tier: Int, toolType: ToolType) = BlockHarvestTool(tier, toolType, requiresTool = false)
	}
}
