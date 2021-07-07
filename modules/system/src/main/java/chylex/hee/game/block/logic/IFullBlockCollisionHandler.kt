package chylex.hee.game.block.logic

import chylex.hee.HEE
import chylex.hee.game.entity.util.selectAllEntities
import chylex.hee.game.world.util.getBlock
import chylex.hee.util.forge.SubscribeAllEvents
import chylex.hee.util.forge.SubscribeEvent
import chylex.hee.util.math.Pos
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.TickEvent.Phase
import net.minecraftforge.event.TickEvent.WorldTickEvent

interface IFullBlockCollisionHandler {
	fun onEntityCollisionAbove(world: World, pos: BlockPos, entity: Entity)
	
	@SubscribeAllEvents(modid = HEE.ID)
	object EventHandler {
		@SubscribeEvent
		fun onWorldTick(e: WorldTickEvent) {
			if (e.phase == Phase.END) {
				val world = e.world
				
				for (entity in world.selectAllEntities) {
					if (entity.isOnGround) {
						val pos = Pos(entity.posX, entity.posY - 0.01, entity.posZ)
						val block = pos.getBlock(world)
						
						if (block is IFullBlockCollisionHandler) {
							block.onEntityCollisionAbove(world, pos, entity)
						}
					}
				}
			}
		}
	}
}
