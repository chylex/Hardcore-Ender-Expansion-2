package chylex.hee.client.model

import net.minecraft.client.renderer.model.ModelRenderer
import net.minecraft.client.renderer.model.ModelRenderer.ModelBox

class ModelBoxBuilder(private val model: ModelRenderer) {
	private var x = 0F
	private var y = 0F
	private var z = 0F
	
	private var w = 0F
	private var h = 0F
	private var d = 0F
	
	private var u: Int? = null
	private var v: Int? = null
	
	private var mirror = false
	
	fun offset(x: Float, y: Float, z: Float): ModelBoxBuilder {
		this.x = x
		this.y = y
		this.z = z
		return this
	}
	
	fun size(w: Float, h: Float, d: Float): ModelBoxBuilder {
		this.w = w
		this.h = h
		this.d = d
		return this
	}
	
	fun size(w: Int, h: Int, d: Int): ModelBoxBuilder {
		return this.size(w.toFloat(), h.toFloat(), d.toFloat())
	}
	
	fun tex(u: Int, v: Int): ModelBoxBuilder {
		this.u = u
		this.v = v
		return this
	}
	
	fun mirror(): ModelBoxBuilder {
		this.mirror = true
		return this
	}
	
	fun add() {
		val u = u
		val v = v
		
		if (u == null || v == null) {
			model.addBox(x, y, z, w, h, d, mirror)
		}
		else {
			model.cubeList.add(ModelBox(u, v, x, y, z, w, h, d, 0F, 0F, 0F, mirror, model.textureWidth, model.textureHeight))
		}
	}
}
