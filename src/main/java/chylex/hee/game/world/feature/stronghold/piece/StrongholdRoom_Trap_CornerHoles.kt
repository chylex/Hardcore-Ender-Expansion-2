package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.entity.living.EntityMobSilverfish
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.STRONGHOLD_TRAP_CORNER_HOLES
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.system.migration.Difficulty.PEACEFUL
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectVulnerableEntities
import chylex.hee.system.util.use
import net.minecraft.world.World
import java.util.Random
import kotlin.math.min

class StrongholdRoom_Trap_CornerHoles(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	class Trigger : ITriggerHandler{
		private companion object{
			private const val SPAWNS_LEFT_TAG = "SpawnsLeft"
		}
		
		private var spawnsLeft = -1
		
		override fun check(world: World): Boolean{
			return !world.isRemote && world.difficulty != PEACEFUL
		}
		
		override fun update(entity: EntityTechnicalTrigger){
			val world = entity.world
			val rand = world.rand
			
			if (spawnsLeft == -1){
				val area = entity.boundingBox.grow(3.5, 0.0, 3.5).expand(0.0, 2.0, 0.0)
				
				if (world.selectVulnerableEntities.inBox<EntityPlayer>(area).isEmpty()){
					return
				}
				else{
					spawnsLeft = rand.nextInt(5, 7) + ((world.difficulty.id - 1) * 2)
				}
			}
			
			val targetArea = entity.boundingBox.grow(6.0, 0.0, 6.0).expand(0.0, 4.0, 0.0)
			val targets = world.selectVulnerableEntities.inBox<EntityPlayer>(targetArea)
			
			repeat(min(spawnsLeft, rand.nextInt(1, 3))){
				val (x, y, z) = entity.posVec.add(
					4.5 * (if (rand.nextBoolean()) 1 else -1),
					2.0,
					4.5 * (if (rand.nextBoolean()) 1 else -1)
				)
				
				EntityMobSilverfish(world).apply {
					setLocationAndAngles(x, y, z, rand.nextFloat(0F, 360F), 0F)
					delayHideInBlockAI(20 * 30)
					world.addEntity(this)
					
					fallDistance = 1.5F
					attackTarget = rand.nextItemOrNull(targets)
				}
				
				--spawnsLeft
			}
			
			if (spawnsLeft == 0){
				entity.remove()
			}
		}
		
		override fun nextTimer(rand: Random): Int{
			return if (spawnsLeft == -1)
				10
			else
				4 + (3 * rand.nextInt(0, 2))
		}
		
		override fun serializeNBT() = TagCompound().apply {
			putShort(SPAWNS_LEFT_TAG, spawnsLeft.toShort())
		}
		
		override fun deserializeNBT(nbt: TagCompound) = nbt.use {
			spawnsLeft = getShort(SPAWNS_LEFT_TAG).toInt()
		}
	}
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger(STRONGHOLD_TRAP_CORNER_HOLES))
	}
}
