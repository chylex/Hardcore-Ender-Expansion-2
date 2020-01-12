package chylex.hee.game.block.info
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.Material
import net.minecraft.block.material.MaterialColor
import net.minecraftforge.common.ToolType

class BlockBuilder(val material: Material, var color: MaterialColor, var sound: SoundType){
	constructor(original: BlockBuilder) : this(original.material, original.color, original.sound){
		harvestTool = original.harvestTool
		harvestHardness = original.harvestHardness
		explosionResistance = original.explosionResistance
		
		lightLevel = original.lightLevel
		lightOpacity = original.lightOpacity
		slipperiness = original.slipperiness
		
		noDrops = original.noDrops
	}
	
	var harvestTool: Pair<Int, ToolType?> = Pair(-1, null)
	var harvestHardness: Float = 0F
	var explosionResistance: Float = 0F
	
	var lightLevel: Int = 0
	var lightOpacity: Int? = null // UPDATE
	var slipperiness: Float = 0.6F
	
	var randomTicks: Boolean = false
	var noDrops: Boolean = false
	
	val isIndestructible: Boolean
		get() = harvestHardness == INDESTRUCTIBLE_HARDNESS
	
	val p: Block.Properties
		get() = Block.Properties.create(material, color).apply {
			val (level, tool) = harvestTool
			
			if (tool != null){
				harvestTool(tool)
				harvestLevel(level)
			}
			
			hardnessAndResistance(harvestHardness, explosionResistance)
			lightValue(lightLevel)
			slipperiness(slipperiness)
			sound(sound)
			
			if (!material.blocksMovement()){
				doesNotBlockMovement() // UPDATE
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
