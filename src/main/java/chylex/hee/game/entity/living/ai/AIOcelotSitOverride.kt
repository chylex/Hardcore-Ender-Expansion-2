package chylex.hee.game.entity.living.ai
import chylex.hee.HEE
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import net.minecraft.entity.ai.EntityAIOcelotSit
import net.minecraft.entity.passive.EntityOcelot
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent

@SubscribeAllEvents(modid = HEE.ID)
class AIOcelotSitOverride(ocelot: EntityOcelot, overridden: EntityAIOcelotSit) : EntityAIOcelotSit(ocelot, overridden.movementSpeed){
	interface IOcelotCanSitOn{
		fun canOcelotSitOn(world: World, pos: BlockPos): Boolean
	}
	
	companion object{
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST, receiveCanceled = true)
		fun onEntityJoinWorld(e: EntityJoinWorldEvent){
			val entity = e.entity
			
			if (entity is EntityOcelot && !entity.world.isRemote){
				val tasks = entity.tasks
				val entry = tasks.taskEntries.find { entry -> entry.action.let { it is EntityAIOcelotSit && it !is AIOcelotSitOverride } }
				
				if (entry != null){
					tasks.taskEntries.remove(entry)
					tasks.addTask(entry.priority, AIOcelotSitOverride(entity, entry.action as EntityAIOcelotSit))
				}
			}
		}
	}
	
	override fun shouldMoveTo(world: World, pos: BlockPos): Boolean{
		if (super.shouldMoveTo(world, pos)){
			return true
		}
		
		if (!pos.up().isAir(world)){
			return false
		}
		
		val block = pos.getBlock(world)
		return block is IOcelotCanSitOn && block.canOcelotSitOn(world, pos)
	}
}
