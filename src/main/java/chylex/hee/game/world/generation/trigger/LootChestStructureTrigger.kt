package chylex.hee.game.world.generation.trigger

import chylex.hee.HEE
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.game.world.util.getState
import net.minecraft.tileentity.LockableLootTileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class LootChestStructureTrigger(private val resource: ResourceLocation, private val seed: Long) : IStructureTrigger {
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
		val tile = pos.getState(world).createTileEntity(world)
		
		if (tile !is LockableLootTileEntity) {
			HEE.log.error("[LootChestStructureTrigger] failed adding loot table $resource, invalid tile entity type: ${tile?.javaClass?.simpleName}")
		}
		else {
			tile.setLootTable(resource, seed)
			TileEntityStructureTrigger.addTileSafe(world, pos, tile)
		}
	}
}
