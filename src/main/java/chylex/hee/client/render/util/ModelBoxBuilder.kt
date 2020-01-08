package chylex.hee.client.render.util
import net.minecraft.client.renderer.entity.model.RendererModel
import net.minecraft.client.renderer.model.ModelBox

class ModelBoxBuilder(private val model: RendererModel){
	private var x = 0F
	private var y = 0F
	private var z = 0F
	
	private var w = 0
	private var h = 0
	private var d = 0
	
	private var u: Int? = null
	private var v: Int? = null
	
	private var mirror = false
	
	fun offset(x: Float, y: Float, z: Float): ModelBoxBuilder{
		this.x = x
		this.y = y
		this.z = z
		return this
	}
	
	fun size(w: Int, h: Int, d: Int): ModelBoxBuilder{
		this.w = w
		this.h = h
		this.d = d
		return this
	}
	
	fun tex(u: Int, v: Int): ModelBoxBuilder{
		this.u = u
		this.v = v
		return this
	}
	
	fun mirror(): ModelBoxBuilder{
		this.mirror = true
		return this
	}
	
	fun add(){
		val u = u
		val v = v
		
		if (u == null || v == null){
			model.addBox(x, y, z, w, h, d, mirror)
		}
		else{
			model.cubeList.add(ModelBox(model, u, v, x, y, z, w, h, d, 0F, mirror))
		}
	}
}
