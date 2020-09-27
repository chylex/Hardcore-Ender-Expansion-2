package chylex.hee.client.color
import chylex.hee.client.MC
import chylex.hee.system.color.HCL
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.math.offsetTowards

@Sided(Side.CLIENT)
class ColorTransition(private val defaultColor: HCL, private val transitionDuration: Float){
	private var currentFrom: HCL = defaultColor
	private var currentTo: HCL? = null
	private var currentProgress = 0F
	
	private val transitionQueue = ArrayList<HCL>(4)
	
	private var lastUpdateTime = 0L
	
	val currentTargetColor
		get() = currentTo ?: currentFrom
	
	val lastColorInQueue
		get() = transitionQueue.lastOrNull()
	
	fun resetAll(){
		resetQueue()
		
		currentFrom = defaultColor
		currentTo = null
		currentProgress = 0F
		
		lastUpdateTime = MC.systemTime
	}
	
	fun resetQueue(){
		transitionQueue.clear()
	}
	
	fun enqueue(color: HCL){
		if (currentTo == null){
			currentTo = color
		}
		else{
			transitionQueue.add(color)
		}
	}
	
	fun updateGetColor(): HCL{
		val currentTime = MC.systemTime
		val elapsedTime = currentTime - lastUpdateTime
		
		lastUpdateTime = currentTime
		
		val transitionTo = currentTo
		
		if (transitionTo == null){
			return currentFrom
		}
		
		currentProgress += elapsedTime / transitionDuration
		
		if (currentProgress >= 1F){
			currentProgress = 0F
			
			currentFrom = transitionTo
			currentTo = if (transitionQueue.isEmpty()) null else transitionQueue.removeAt(0)
			
			return currentFrom
		}
		else{
			val hueSource = if (transitionTo.chroma > 0F && currentFrom.chroma == 0F) transitionTo else currentFrom
			
			return HCL(
				hue = hueSource.hue,
				chroma = offsetTowards(currentFrom.chroma, transitionTo.chroma, currentProgress),
				luminance = offsetTowards(currentFrom.luminance, transitionTo.luminance, currentProgress)
			)
		}
	}
}
