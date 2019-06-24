package chylex.hee.game.block.info
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

class BlockBuilder(val material: Material, var color: MapColor, var sound: SoundType){
	var harvestTool: Pair<Int, String?> = Pair(-1, null)
	var harvestHardness: Float = 0F
	var explosionResistance: Float = 0F
	var miningStats: Boolean = true
	
	var lightLevel: Int = 0
	var lightOpacity: Int? = null
	var slipperiness: Float = 0.6F
	
	val isIndestructible: Boolean
		get() = harvestHardness == INDESTRUCTIBLE_HARDNESS
	
	fun makeIndestructible(){
		harvestTool = Pair(-1, null)
		harvestHardness = INDESTRUCTIBLE_HARDNESS
		explosionResistance = INDESTRUCTIBLE_RESISTANCE
		miningStats = false
	}
	
	fun clone(modify: BlockBuilder.() -> Unit): BlockBuilder{
		return BlockBuilder(material, color, sound).apply {
			harvestTool = this@BlockBuilder.harvestTool
			harvestHardness = this@BlockBuilder.harvestHardness
			explosionResistance = this@BlockBuilder.explosionResistance
			miningStats = this@BlockBuilder.miningStats
			
			lightLevel = this@BlockBuilder.lightLevel
			lightOpacity = this@BlockBuilder.lightOpacity
			slipperiness = this@BlockBuilder.slipperiness
		}.apply(modify)
	}
	
	companion object{
		const val INDESTRUCTIBLE_HARDNESS = -1F
		const val INDESTRUCTIBLE_RESISTANCE = 6000000F
		
		fun Block.setHarvestTool(tool: Pair<Int, String?>){
			val toolType = tool.second
			
			if (toolType != null){
				this.setHarvestLevel(toolType, tool.first)
			}
		}
		
		fun Block.setHardnessWithResistance(harvestHardness: Float, explosionResistance: Float, multiplier: Float = 1F){
			if (harvestHardness == INDESTRUCTIBLE_HARDNESS){
				this.setHardness(harvestHardness)
			}
			else{
				this.setHardness(harvestHardness * multiplier)
			}
			
			if (explosionResistance == INDESTRUCTIBLE_RESISTANCE){ // UPDATE: check if setResistance still multiplies the provided value by 3
				this.setResistance(explosionResistance)
			}
			else{
				this.setResistance(explosionResistance * multiplier)
			}
		}
		
		fun Block.setupBlockProperties(builder: BlockBuilder, replaceMaterialAndColor: Boolean){ // UPDATE: temporary AT workarounds before 1.13 block builder
			if (replaceMaterialAndColor){
				this.material = builder.material
				this.translucent = !this.material.blocksLight()
				
				this.blockMapColor = builder.color
			}
			
			setHarvestTool(builder.harvestTool)
			setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance)
			this.enableStats = builder.miningStats
			
			this.lightValue = builder.lightLevel
			builder.lightOpacity?.let { this.lightOpacity = it }
			this.slipperiness = builder.slipperiness
			this.blockSoundType = builder.sound
		}
	}
}
