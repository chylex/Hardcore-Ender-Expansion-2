package chylex.hee.game.block.entity
import chylex.hee.game.block.entity.TileEntityBasePortalController.ForegroundRenderState.Invisible

class TileEntityVoidPortalStorage : TileEntityBasePortalController(){
	override val serverRenderState = Invisible
	override val clientAnimationFadeInSpeed = 0F
	override val clientAnimationFadeOutSpeed = 0F
}
