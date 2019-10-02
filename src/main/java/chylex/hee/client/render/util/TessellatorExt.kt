package chylex.hee.client.render.util
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.VertexFormat

val TESSELLATOR: Tessellator = Tessellator.getInstance()

inline fun Tessellator.draw(mode: Int, format: VertexFormat, procedure: BufferBuilder.() -> Unit){
	this.buffer.begin(mode, format)
	this.buffer.apply(procedure)
	this.draw()
}
