package chylex.hee.game.world.structure.trigger
import chylex.hee.HEE
import chylex.hee.game.world.getState
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.TileEntityLockableLoot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class LootChestStructureTrigger(private val resource: ResourceLocation, private val seed: Long) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform){
		val tile = pos.getState(world).createTileEntity(world)
		
		if (tile !is TileEntityLockableLoot){
			HEE.log.error("[LootChestStructureTrigger] failed adding loot table $resource, invalid tile entity type: ${tile?.javaClass?.simpleName}")
		}
		else{
			tile.setLootTable(resource, seed)
			TileEntityStructureTrigger.addTileSafe(world, pos, tile)
		}
	}
}
