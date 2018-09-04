package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

open class BlockSimple(builder: Builder) : Block(builder.material, builder.mapColor){
	init{
		setHarvestTool(builder.harvestTool)
		setHardness(builder.harvestHardness)
		setResistance(builder.explosionResistance) // multiplies the real value by 3
		
		lightValue = builder.lightLevel
		builder.lightOpacity?.let { lightOpacity = it }
		
		setDefaultSlipperiness(builder.slipperiness)
		
		soundType = builder.soundType
	}
	
	class Builder(val material: Material){
		var harvestTool: Pair<Int, String?> = Pair(-1, null)
		var harvestHardness: Float = 0F
		var explosionResistance: Float = 0F
		
		var lightLevel: Int = 0
		var lightOpacity: Int? = null
		
		var slipperiness: Float = 0.6F
		
		var mapColor: MapColor = material.materialMapColor
		var soundType: SoundType = SoundType.STONE
		
		val isIndestructible: Boolean
			get() = harvestHardness == -1F
		
		fun makeIndestructible(){
			harvestTool = Pair(-1, null)
			harvestHardness = -1F
			explosionResistance = 6000000F
		}
		
		fun clone(modify: Builder.() -> Unit): Builder{
			return Builder(material).apply {
				harvestTool = this@Builder.harvestTool
				harvestHardness = this@Builder.harvestHardness
				explosionResistance = this@Builder.explosionResistance
				
				lightLevel = this@Builder.lightLevel
				lightOpacity = this@Builder.lightOpacity
				
				slipperiness = this@Builder.slipperiness
				
				mapColor = this@Builder.mapColor
				soundType = this@Builder.soundType
			}.apply(modify)
		}
		
		companion object{
			fun Block.setHarvestTool(tool: Pair<Int, String?>){
				val toolType = tool.second
				
				if (toolType != null){
					this.setHarvestLevel(toolType, tool.first)
				}
			}
		}
	}
}
