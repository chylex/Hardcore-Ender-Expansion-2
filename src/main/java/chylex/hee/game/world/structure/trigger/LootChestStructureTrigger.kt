package chylex.hee.game.world.structure.trigger
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.getTile
import net.minecraft.tileentity.TileEntityLockableLoot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LootChestStructureTrigger(private val resource: ResourceLocation, private val seed: Long) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		pos.getTile<TileEntityLockableLoot>(world)?.setLootTable(resource, seed)
	}
}
