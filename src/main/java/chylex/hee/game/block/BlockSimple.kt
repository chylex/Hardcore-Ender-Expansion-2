package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHardnessWithResistance
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import net.minecraft.block.Block
import net.minecraft.block.SoundType
import net.minecraft.block.material.MapColor
import net.minecraft.block.material.Material

open class BlockSimple(builder: Builder) : Block(builder.material, builder.mapColor){
	init{
		setHarvestTool(builder.harvestTool)
		setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance)
		enableStats = builder.miningStats
		
		slipperiness = builder.slipperiness
		
		lightValue = builder.lightLevel
		builder.lightOpacity?.let { lightOpacity = it }
		
		soundType = builder.soundType
	}
	
	class Builder(val material: Material){
		var harvestTool: Pair<Int, String?> = Pair(-1, null)
		var harvestHardness: Float = 0F
		var explosionResistance: Float = 0F
		var miningStats: Boolean = true
		
		var slipperiness: Float = 0.6F
		
		var lightLevel: Int = 0
		var lightOpacity: Int? = null
		
		var mapColor: MapColor = material.materialMapColor
		var soundType: SoundType = SoundType.STONE
		
		val isIndestructible: Boolean
			get() = harvestHardness == INDESTRUCTIBLE_HARDNESS
		
		fun makeIndestructible(){
			harvestTool = Pair(-1, null)
			harvestHardness = INDESTRUCTIBLE_HARDNESS
			explosionResistance = INDESTRUCTIBLE_RESISTANCE
			miningStats = false
		}
		
		fun clone(modify: Builder.() -> Unit): Builder{
			return Builder(material).apply {
				harvestTool = this@Builder.harvestTool
				harvestHardness = this@Builder.harvestHardness
				explosionResistance = this@Builder.explosionResistance
				miningStats = this@Builder.miningStats
				
				slipperiness = this@Builder.slipperiness
				
				lightLevel = this@Builder.lightLevel
				lightOpacity = this@Builder.lightOpacity
				
				mapColor = this@Builder.mapColor
				soundType = this@Builder.soundType
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
		}
	}
}
