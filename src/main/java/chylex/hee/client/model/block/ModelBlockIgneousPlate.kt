package chylex.hee.client.model.block
import chylex.hee.client.render.util.beginBox
import chylex.hee.client.render.util.render
import net.minecraft.client.model.ModelBase
import net.minecraft.client.model.ModelRenderer
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@SideOnly(Side.CLIENT)
object ModelBlockIgneousPlate : ModelBase(){
	const val ANIMATION_PERIOD = PI
	
	private val outerBox: ModelRenderer
	private val innerBox: ModelRenderer
	
	init{
		textureWidth = 32
		textureHeight = 16
		
		outerBox = ModelRenderer(this).apply {
			beginBox.offset(12F,  4F, 0F).size( 2, 8, 2).tex(0, 6).add()
			beginBox.offset( 2F,  4F, 0F).size( 2, 8, 2).tex(8, 6).add()
			beginBox.offset( 2F,  2F, 0F).size(12, 2, 2).tex(0, 0).add()
			beginBox.offset( 2F, 12F, 0F).size(12, 2, 2).tex(0, 4).add()
		}
		
		innerBox = ModelRenderer(this).apply {
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
