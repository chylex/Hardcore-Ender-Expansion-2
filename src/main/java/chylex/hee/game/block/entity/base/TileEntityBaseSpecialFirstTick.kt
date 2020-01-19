package chylex.hee.game.block.entity.base
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.tileentity.TileEntityType

abstract class TileEntityBaseSpecialFirstTick(type: TileEntityType<out TileEntityBase>) : TileEntityBase(type), ITickableTileEntity{
	private var triggeredFirstTick = false
	
	protected open fun firstTick(){} // must be called from tick() because onLoad() completely breaks world access
	
	override fun tick(){
		if (!triggeredFirstTick){
			firstTick()
			triggeredFirstTick = true
		}
	}
}
