package chylex.hee.game.block.properties

import net.minecraft.block.AbstractBlock
import net.minecraft.block.AbstractBlock.IPositionPredicate
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.ToolType

class BlockBuilder(val material: Material, var color: MaterialColor, var sound: SoundType) {
	constructor(original: BlockBuilder) : this(original.material, original.color, original.sound) {
		isSolid = original.isSolid
		
		harvestTool = original.harvestTool
		harvestHardness = original.harvestHardness
		explosionResistance = original.explosionResistance
		
		lightLevel = original.lightLevel
		slipperiness = original.slipperiness
		
		randomTicks = original.randomTicks
		noDrops = original.noDrops
	}
	
	var isSolid = true
	var isOpaque: Boolean? = null
	var suffocates: Boolean? = null
	var blocksVision: Boolean? = null
	
	var requiresTool: Boolean = false
	var harvestTool: Pair<Int, ToolType?> = Pair(-1, null)
	var harvestHardness: Float = 0F
	var explosionResistance: Float = 0F
	
	var lightLevel: Int = 0
	var slipperiness: Float = 0.6F
	
	var randomTicks: Boolean = false
	var noDrops: Boolean = false
	
	val p: AbstractBlock.Properties
		get() = AbstractBlock.Properties.create(material, color).also { props ->
			val (level, tool) = harvestTool
			
			if (tool != null) {
				props.harvestTool(tool)
				props.harvestLevel(level)
			}
			
			if (requiresTool) {
				props.setRequiresTool()
			}
			
			props.hardnessAndResistance(harvestHardness, explosionResistance)
			props.setLightLevel { lightLevel }
			props.slipperiness(slipperiness)
			props.sound(sound)
			
			if (!isSolid) {
				props.notSolid()
			}
			
			isOpaque?.let { props.setOpaque(always(it)) }
			suffocates?.let { props.setSuffocates(always(it)) }
			blocksVision?.let { props.setBlocksVision(always(it)) }
			
			if (!material.blocksMovement()) {
				props.doesNotBlockMovement()
				
				if (isSolid) {
					throw UnsupportedOperationException("[BlockBuilder] cannot create a block that does not block movement and is solid at the same time")
				}
			}
			
			if (randomTicks) {
				props.tickRandomly()
			}
			
			if (noDrops) {
				props.noDrops()
			}
		}
	
	fun makeIndestructible() {
		harvestTool = Pair(-1, null)
		harvestHardness = INDESTRUCTIBLE_HARDNESS
		explosionResistance = INDESTRUCTIBLE_RESISTANCE
		noDrops = true
	}
	
	inline fun clone(modify: BlockBuilder.() -> Unit): BlockBuilder {
		return BlockBuilder(this).apply(modify)
	}
	
	companion object {
		const val INDESTRUCTIBLE_HARDNESS = -1F
		const val INDESTRUCTIBLE_RESISTANCE = 3600000F
		
		private fun always(value: Boolean): IPositionPredicate {
			return IPositionPredicate { _, _, _ -> value }
		}
	}
}
