package chylex.hee.game.world.structure.trigger
import chylex.hee.HEE
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.getTile
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityLockableLoot
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class LootChestStructureTrigger(private val resource: ResourceLocation, private val seed: Long) : IStructureTrigger{
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform){}
	
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		val tile = pos.getTile<TileEntityLockableLoot>(world)
		
		if (tile == null){
			HEE.log.error("[LootChestStructureTrigger] failed adding loot table $resource, invalid tile entity type: ${pos.getTile<TileEntity>(world)?.javaClass?.simpleName}")
		}
		else{
			tile.setLootTable(resource, seed)
		}
	}
}
