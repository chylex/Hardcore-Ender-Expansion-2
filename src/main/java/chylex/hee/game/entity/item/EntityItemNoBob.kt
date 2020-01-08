package chylex.hee.game.entity.item
import chylex.hee.init.ModEntities
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.ItemStack
import net.minecraft.world.World

open class EntityItemNoBob : EntityItemBase{
	@Suppress("unused")
	constructor(type: EntityType<out EntityItemNoBob>, world: World) : super(type, world)
	constructor(type: EntityType<out EntityItemNoBob>, world: World, stack: ItemStack, replacee: Entity) : super(type, world, stack, replacee)
	constructor(world: World, stack: ItemStack, replacee: Entity) : this(ModEntities.ITEM_NO_BOB, world, stack, replacee)
}
