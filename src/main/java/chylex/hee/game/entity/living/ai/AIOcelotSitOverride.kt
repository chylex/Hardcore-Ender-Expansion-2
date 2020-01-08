package chylex.hee.game.entity.living.ai
import chylex.hee.HEE
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityCat
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import net.minecraft.entity.ai.goal.CatSitOnBlockGoal
import net.minecraft.entity.passive.CatEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldReader
import net.minecraftforge.event.entity.EntityJoinWorldEvent

@SubscribeAllEvents(modid = HEE.ID)
class AIOcelotSitOverride(ocelot: CatEntity, overridden: CatSitOnBlockGoal) : CatSitOnBlockGoal(ocelot, overridden.movementSpeed){
	interface IOcelotCanSitOn{
		fun canOcelotSitOn(world: IWorldReader, pos: BlockPos): Boolean
	}
	
	companion object{
		@JvmStatic
		@SubscribeEvent(EventPriority.LOWEST, receiveCanceled = true)
		fun onEntityJoinWorld(e: EntityJoinWorldEvent){
			val entity = e.entity
			
			if (entity is EntityCat && !entity.world.isRemote){
				val tasks = entity.goalSelector
				val entry = tasks.goals.find { entry -> entry.goal.let { it is CatSitOnBlockGoal && it !is AIOcelotSitOverride } }
				
				if (entry != null){
					tasks.goals.remove(entry)
					tasks.addGoal(entry.priority, AIOcelotSitOverride(entity, entry.goal as CatSitOnBlockGoal))
				}
			}
		}
	}
	
	override fun shouldMoveTo(world: IWorldReader, pos: BlockPos): Boolean{
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
