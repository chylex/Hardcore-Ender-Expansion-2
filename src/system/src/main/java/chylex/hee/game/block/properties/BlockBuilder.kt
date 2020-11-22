package chylex.hee.game.block.properties
import net.minecraft.block.AbstractBlock
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.ToolType

class BlockBuilder(val material: Material, var color: MaterialColor, var sound: SoundType){
	constructor(original: BlockBuilder) : this(original.material, original.color, original.sound){
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
	
	var harvestTool: Pair<Int, ToolType?> = Pair(-1, null)
	var harvestHardness: Float = 0F
	var explosionResistance: Float = 0F
	
	var lightLevel: Int = 0
	var slipperiness: Float = 0.6F
	
	var randomTicks: Boolean = false
	var noDrops: Boolean = false
	
	val p: AbstractBlock.Properties
		get() = AbstractBlock.Properties.create(material, color).apply {
			val (level, tool) = this@BlockBuilder.harvestTool
			
			if (tool != null){
				harvestTool(tool)
				harvestLevel(level)
			}
			
			hardnessAndResistance(harvestHardness, explosionResistance)
			setLightLevel { lightLevel }
			slipperiness(slipperiness)
			sound(sound)
			
			if (!isSolid){
				notSolid()
			}
			
			if (!material.blocksMovement()){
				doesNotBlockMovement()
				
				if (isSolid){
					throw UnsupportedOperationException("[BlockBuilder] cannot create a block that does not block movement and is solid at the same time")
				}
			}
			
			if (randomTicks){
				tickRandomly()
			}
			
			if (noDrops){
				noDrops()
			}
		}
	
	fun makeIndestructible(){
		harvestTool = Pair(-1, null)
		harvestHardness = INDESTRUCTIBLE_HARDNESS
		explosionResistance = INDESTRUCTIBLE_RESISTANCE
		noDrops = true
	}
	
	inline fun clone(modify: BlockBuilder.() -> Unit): BlockBuilder{
		return BlockBuilder(this).apply(modify)
	}
	
	companion object{
		const val INDESTRUCTIBLE_HARDNESS = -1F
		const val INDESTRUCTIBLE_RESISTANCE = 3600000F
	}
}
