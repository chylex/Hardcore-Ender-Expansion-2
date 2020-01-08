package chylex.hee.client.model.entity
import chylex.hee.client.render.util.beginBox
import chylex.hee.client.render.util.render
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import net.minecraft.client.renderer.entity.model.EntityModel
import net.minecraft.client.renderer.entity.model.RendererModel

@Sided(Side.CLIENT)
object ModelEntityTokenHolder : EntityModel<EntityTokenHolder>(){
	private val box: RendererModel
	
	init{
		textureWidth = 64
		textureHeight = 32
		
		box = RendererModel(this).apply {
			beginBox.offset(-8F, -8F, -8F).size(16, 16, 16).add()
		}
	}
	
	fun render(){
		box.render()
	}
}
