package chylex.hee.game.mechanics.scorching
import chylex.hee.HEE
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.item.repair.ICustomRepairBehavior
import chylex.hee.game.item.repair.RepairInstance
import chylex.hee.game.particle.ParticleFlameCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.Hand.MAIN_HAND
import chylex.hee.system.migration.forge.EventPriority
import chylex.hee.system.migration.forge.SubscribeAllEvents
import chylex.hee.system.migration.forge.SubscribeEvent
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.isNotEmpty
import net.minecraft.entity.Entity
import net.minecraft.item.IItemTier
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.event.entity.player.CriticalHitEvent
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent
import java.util.Random

@SubscribeAllEvents(modid = HEE.ID)
object ScorchingHelper{
	private val PARTICLE_MINING = ParticleSpawnerCustom(
		type = ParticleFlameCustom,
		data = ParticleFlameCustom.Data(maxAge = 6),
		pos = InBox(0.7F)
	)
	
	private fun PARTICLE_HITTING(target: Entity) = ParticleSpawnerCustom(
		type = ParticleFlameCustom,
		data = ParticleFlameCustom.Data(maxAge = 4),
		pos = Constant(0.2F, UP) + InBox(target, 0.4F)
	)
	
	val FX_BLOCK_BREAK = object : FxBlockHandler(){
		override fun handle(pos: BlockPos, world: World, rand: Random){
			PARTICLE_MINING.spawn(Point(pos, 15), rand)
		}
	}
	
	val FX_ENTITY_HIT = object : FxEntityHandler(){
		override fun handle(entity: Entity, rand: Random){
			PARTICLE_HITTING(entity).spawn(Point(entity, heightMp = 0.5F, amount = 20), rand)
		}
	}
	
	// Helpers
	
	private fun getHeldScorchingTool(player: EntityPlayer): IScorchingItem?{
		return player.getHeldItem(MAIN_HAND).item as? IScorchingItem
	}
	
	// Events
	
	@JvmStatic
	@SubscribeEvent(EventPriority.LOWEST)
	fun onBreakSpeed(e: BreakSpeed){
		val world = e.entity.world
		
		if (world.isRemote && getHeldScorchingTool(e.player)?.canMine(e.state) == true){
			PARTICLE_MINING.spawn(Point(e.pos, 5), world.rand)
		}
	}
	
	@JvmStatic
	@SubscribeEvent(EventPriority.LOW)
	fun onHarvestDrops(e: HarvestDropsEvent){
		if (e.harvester?.let(::getHeldScorchingTool)?.canMine(e.state) == true){ // TODO not checking drops.isNotEmpty to support Vines, is that a problem?
			val fortuneStack = ScorchingFortune.createSmeltedStack(e.world.world, e.state.block, e.harvester.rng)
			
			if (fortuneStack.isNotEmpty){
				e.drops.clear()
				e.drops.add(fortuneStack)
				e.dropChance = 1F
			}
		}
	}
	
	@JvmStatic
	@SubscribeEvent(EventPriority.LOW)
	fun onCriticalHit(e: CriticalHitEvent){
		getHeldScorchingTool(e.player)?.onHit(e)
	}
	
	// Overrides
	
	fun onGetIsRepairable(tool: IScorchingItem, toRepair: ItemStack, repairWith: ItemStack): Boolean{
		return repairWith.item === ModItems.INFERNIUM || tool.material.repairMaterial.test(repairWith)
	}
	
	class Repair(val material: IItemTier) : ICustomRepairBehavior{
		override fun onRepairUpdate(instance: RepairInstance) = with(instance){
			if (material.repairMaterial.test(ingredient)){
				repairFully()
			}
			else{
				repairPercent(22)
			}
			
			repairCost = repairCost * 2 + 1
		}
	}
}
