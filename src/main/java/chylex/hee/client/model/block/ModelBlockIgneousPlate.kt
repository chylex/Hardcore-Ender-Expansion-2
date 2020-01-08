package chylex.hee.client.model.block
import chylex.hee.client.render.util.beginBox
import chylex.hee.client.render.util.render
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.entity.model.RendererModel
import net.minecraft.client.renderer.model.Model
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Sided(Side.CLIENT)
object ModelBlockIgneousPlate : Model(){
	const val ANIMATION_PERIOD = PI
	
	private val outerBox: RendererModel
	private val innerBox: RendererModel
	
	init{
		textureWidth = 32
		textureHeight = 16
		
		outerBox = RendererModel(this).apply {
			beginBox.offset(12F,  4F, 0F).size( 2, 8, 2).tex(0, 6).add()
			beginBox.offset( 2F,  4F, 0F).size( 2, 8, 2).tex(8, 6).add()
			beginBox.offset( 2F,  2F, 0F).size(12, 2, 2).tex(0, 0).add()
			beginBox.offset( 2F, 12F, 0F).size(12, 2, 2).tex(0, 4).add()
		}
		
		innerBox = RendererModel(this).apply {
			beginBox.offset(4F, 4F, 0.5F).size(8, 8, 1).tex(14, 7).add()
		}
	}
	
	fun renderOuterBox(){
		outerBox.render()
	}
	
	fun renderInnerBox(animation: Double){
		innerBox.offsetZ = -abs(sin(-animation)).toFloat() * 0.0925F
		innerBox.render()
	}
}
