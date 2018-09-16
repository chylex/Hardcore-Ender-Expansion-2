package chylex.hee.game.render.util
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class ColorTransitionQueue(private val defaultColor: HCL, private val transitionDuration: Float){
	private val colorQueue = ArrayList<HCL>(4)
	private var currentProgress = 0F
	private var lastUpdateTime = 0L
	
	init{
		colorQueue.add(defaultColor)
	}
	
	val last
		get() = colorQueue.last()
	
	fun reset(){
		colorQueue.clear()
		colorQueue.add(defaultColor)
		
		currentProgress = 0F
		lastUpdateTime = Minecraft.getSystemTime()
	}
	
	fun enqueue(color: HCL){
		colorQueue.add(color)
	}
	
	fun tryRemoveLast(){
		if (colorQueue.size > 2){
			colorQueue.removeAt(colorQueue.lastIndex)
		}
	}
	
	fun updateGetColor(): HCL{
		val currentTime = Minecraft.getSystemTime()
		val elapsedTime = currentTime - lastUpdateTime
		
		lastUpdateTime = currentTime
		
		if (colorQueue.size <= 1){
			return colorQueue[0]
		}
		
		currentProgress += elapsedTime / transitionDuration
		
		if (currentProgress >= 1F){
			currentProgress = 0F
			return colorQueue.removeAt(0)
		}
		else{
			val from = colorQueue[0]
			val to = colorQueue[1]
			
			val targetHue = if (to.chroma > 0F && from.chroma == 0F) to else from
			
			return HCL(
				hue = targetHue.hue,
				chroma = from.chroma + (to.chroma - from.chroma) * currentProgress,
				luminance = from.luminance + (to.luminance - from.luminance) * currentProgress
			)
		}
	}
}
