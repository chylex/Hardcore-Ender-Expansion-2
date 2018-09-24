package chylex.hee.game.entity.item
import chylex.hee.system.util.cloneFrom
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.item.ItemStack
import net.minecraft.world.World

open class EntityItemNoBob : EntityItem{
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(world: World, stack: ItemStack, replacee: Entity) : super(world, replacee.posX, replacee.posY, replacee.posZ, stack){
		this.cloneFrom(replacee)
	}
}
